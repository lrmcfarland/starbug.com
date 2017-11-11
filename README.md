# Starbug.com

This is the static content for the starbug.com website.

# dockerize

This shows how to run the starbug.com content in a container as a
stand alone Apache httpd daemon or as a Nginx reverse proxy.

The stand alone Apache version does not include wsgi at this time,
just the static content.  I have of how to run everything under Apache
discussion of that in the [Astronomy/www
repo](https://github.com/lrmcfarland/Astronomy/tree/master/www), but
not as a TLS endpoint (yet).

The nginx reverse proxy expects the AAI application to be running
in a separate container. It will act as a TLS termination point
so the browser's location can be sent.


## Apache

From hub.docker.com [httpd docker image](https://hub.docker.com/_/httpd/)

### to build

```

$ docker build -f Dockerfile.httpd -t httpd.starbug.com .
Sending build context to Docker daemon   71.1MB
Step 1/5 : FROM httpd
 ---> 74ad7f48867f
Step 2/5 : LABEL maintainer "lrm@starbug.com"
 ---> Running in 46d46e5e60ff
 ---> 2e24ca3fb451
Removing intermediate container 46d46e5e60ff
Step 3/5 : LABEL service "Starbug.com static content"
 ---> Running in 02c7c4a15bc1
 ---> 9edb18ff6659
Removing intermediate container 02c7c4a15bc1
Step 4/5 : COPY ./public_html/ /usr/local/apache2/htdocs/
 ---> 4cde86cccc51
Step 5/5 : COPY ./httpd.conf /usr/local/apache2/conf/httpd.conf
 ---> bb74880db3a6
Successfully built bb74880db3a6
Successfully tagged httpd.starbug.com:latest


```

### to run


```
$ docker run -dit --name apache-00.starbug.com -p 80:80 httpd.starbug.com
f57e0cf77316776d5bb424c63aa8cf54a287c26fdac292c8c1931cbc762d293c
[lrm@lrmz-iMac starbug.com (dockerize)]$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
f57e0cf77316        httpd.starbug.com   "httpd-foreground"       3 seconds ago       Up 2 seconds        0.0.0.0:80->80/tcp       apache-00.starbug.com

```

### to debug

#### logs

```

$ docker logs apache-00.starbug.com
AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
[Fri Nov 10 23:27:56.933235 2017] [mpm_event:notice] [pid 1:tid 140681374685056] AH00489: Apache/2.4.29 (Unix) configured -- resuming normal operations
[Fri Nov 10 23:27:56.933780 2017] [core:notice] [pid 1:tid 140681374685056] AH00094: Command line: 'httpd -D FOREGROUND'

```


#### interactive shell

```
docker run -it --entrypoint /bin/bash httpd.starbug.com

root@b6ee76f568c8:/usr/local/apache2# ls
bin  build  cgi-bin  conf  error  htdocs  icons  include  logs	modules

```


## Nginx self signed

This will use a self signed cert for testing. It roughly follows the
example given
[here](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-16-04)

### Create the certs

```
$ openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./ssl/private/nginx-selfsigned.key -out ./ssl/certs/nginx-selfsigned.crt
Generating a 2048 bit RSA private key
...........................+++
...........................................+++
unable to write 'random state'
writing new private key to './ssl/private/nginx-selfsigned.key'
-----
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) [AU]:US
State or Province Name (full name) [Some-State]:CA
Locality Name (eg, city) []:MtView
Organization Name (eg, company) [Internet Widgits Pty Ltd]:starbug.com
Organizational Unit Name (eg, section) []:aai
Common Name (e.g. server FQDN or YOUR name) []:starbug.com
Email Address []:lrm@starbug.com


$ openssl dhparam -out /Users/lrm/src/AAI/starbug.com/ssl/certs/dhparam.pem 2048
Generating DH parameters, 2048 bit long safe prime, generator 2
This is going to take a long time
...............................................+....

```



### to build

```
$ docker build -f Dockerfile.nginx.selfsigned -t nginx-ss.starbug.com .
Sending build context to Docker daemon  71.12MB
Step 1/6 : FROM nginx
 ---> 40960efd7b8f
Step 2/6 : COPY ./public_html/ /opt/starbug.com/www/public_html/
 ---> ca27628aa02e
Step 3/6 : COPY ./nginx.conf /etc/nginx/nginx.conf
 ---> 92a176e81d9c
Step 4/6 : COPY ./aai-nginx.selfsigned.conf /etc/nginx/conf.d/aai-nginx-00.conf
 ---> 22b1166cbfc6
Step 5/6 : COPY ./ssl/certs/ /etc/ssl/certs/
 ---> 440552dd626b
Step 6/6 : COPY ./ssl/private/ /etc/ssl/private/
 ---> 5323de737280
Successfully built 5323de737280
Successfully tagged nginx-ss.starbug.com:latest
```

### to run

This expects an instance of the aai-gunicorn image to be running as aai-gunicorn-00. See my [Astronomy repo](https://github.com/lrmcfarland/Astronomy) for details.

```
$ docker run --name nginx-ss-00.starbug.com --link aai-gunicorn-00:aai-gunicorn-00 -d -p 80:80 -p 443:443 nginx-ss.starbug.com
7772e4220fdf96e05c0b970be294df2ecc83feda81107bf1efc2c39e16d9b689

$ docker ps
CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS              PORTS                                      NAMES
7772e4220fdf        nginx-ss.starbug.com   "nginx -g 'daemon ..."   38 seconds ago      Up 37 seconds       0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp   nginx-ss-00.starbug.com
d5c4f165919b        aai-gunicorn           "bash gunicorn.sh ..."   4 hours ago         Up 4 hours          0.0.0.0:8080->8080/tcp                     aai-gunicorn-00
```

### to debug

#### logs

```
$ docker logs nginx-ss-00.starbug.com
2017/11/11 05:23:20 [warn] 1#1: "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
nginx: [warn] "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
172.17.0.1 - - [11/Nov/2017:05:25:23 +0000] "GET / HTTP/1.1" 200 3284 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:05:25:23 +0000] "GET /styles.css HTTP/1.1" 200 248 "http://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:05:25:23 +0000] "GET /picts/HollyHopDrive.jpg HTTP/1.1" 200 3817 "http://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:05:25:23 +0000] "GET /picts/theDish_small.jpg HTTP/1.1" 200 103173 "http://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:05:25:25 +0000] "GET /apple-touch-icon-precomposed.png HTTP/1.1" 404 169 "-" "Safari/12604.3.5.1.1 CFNetwork/811.7.2 Darwin/16.7.0 (x86_64)"
2017/11/11 05:25:25 [error] 5#5: *3 open() "/opt/starbug.com/www/public_html/apple-touch-icon-precomposed.png" failed (2: No such file or directory), client: 172.17.0.1, server: www.starbug.com, request: "GET /apple-touch-icon-precomposed.png HTTP/1.1", host: "0.0.0.0"
2017/11/11 05:25:25 [error] 5#5: *3 open() "/opt/starbug.com/www/public_html/apple-touch-icon.png" failed (2: No such file or directory), client: 172.17.0.1, server: www.starbug.com, request: "GET /apple-touch-icon.png HTTP/1.1", host: "0.0.0.0"
172.17.0.1 - - [11/Nov/2017:05:25:25 +0000] "GET /apple-touch-icon.png HTTP/1.1" 404 169 "-" "Safari/12604.3.5.1.1 CFNetwork/811.7.2 Darwin/16.7.0 (x86_64)"

```


#### interactive shell

```
$ docker run -it --entrypoint /bin/bash nginx-ss.starbug.com

root@be7eef9b4e6d:/# ls
bin  boot  dev	etc  home  lib	lib64  media  mnt  opt	proc  root  run  sbin  srv  sys  tmp  usr  var
root@be7eef9b4e6d:/# pwd
/
root@be7eef9b4e6d:/# cd /etc/nginx
root@be7eef9b4e6d:/etc/nginx# ls
conf.d	fastcgi_params	koi-utf  koi-win  mime.types  modules  nginx.conf  scgi_params	uwsgi_params  win-utf

```


## to clean up

```
to delete all containers:  docker rm $(docker ps -a -q)
to delete all images:      docker rmi $(docker images -q)
to delete dangling images: docker rmi $(docker images -q -f dangling=true)
```
