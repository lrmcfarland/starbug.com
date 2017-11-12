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

### to build

From hub.docker.com [httpd docker image](https://hub.docker.com/_/httpd/)

```
$ docker build -f Dockerfile.httpd -t httpd.starbug.com .
Sending build context to Docker daemon  71.11MB
Step 1/5 : FROM httpd
latest: Pulling from library/httpd
85b1f47fba49: Pull complete
45bea5eb3b59: Pull complete
d360abbf616c: Pull complete
91c7cdd03f84: Pull complete
30623dd230a8: Pull complete
cc21a2e04dd3: Pull complete
f789cd8382be: Pull complete
Digest: sha256:8ac08d0fdc49f2dc83bf5dab36c029ffe7776f846617335225d2796c74a247b4
Status: Downloaded newer image for httpd:latest
 ---> 74ad7f48867f
Step 2/5 : LABEL maintainer "lrm@starbug.com"
 ---> Running in 7cc78955f881
 ---> fc8c0176396a
Removing intermediate container 7cc78955f881
Step 3/5 : LABEL service "Starbug.com static content"
 ---> Running in 8750e95c292f
 ---> df137cd2f822
Removing intermediate container 8750e95c292f
Step 4/5 : COPY ./public_html/ /usr/local/apache2/htdocs/
 ---> 949846a4edc5
Step 5/5 : COPY ./conf/httpd.conf /usr/local/apache2/conf/httpd.conf
 ---> 1451aaf85888
Successfully built 1451aaf85888
Successfully tagged httpd.starbug.com:latest
```

### to run


```
$ docker run -dit --name apache-00.starbug.com -p 80:80 httpd.starbug.com
aaebfd367c290baf655c095bd668da4f82cdf3fab7380db76aca9365c849dac9


$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                    NAMES
aaebfd367c29        httpd.starbug.com   "httpd-foreground"       58 seconds ago      Up 57 seconds       0.0.0.0:80->80/tcp       apache-00.starbug.com
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

### Create the self signed certs

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
Sending build context to Docker daemon  71.11MB
Step 1/6 : FROM nginx
 ---> 40960efd7b8f
Step 2/6 : COPY ./public_html/ /opt/starbug.com/www/public_html/
 ---> 51d47edae4c8
Step 3/6 : COPY ./conf/nginx.conf /etc/nginx/nginx.conf
 ---> 3c7f72465c53
Step 4/6 : COPY ./conf/aai-nginx.selfsigned.conf /etc/nginx/conf.d/aai-nginx-00.conf
 ---> 12e93810a47f
Step 5/6 : COPY ./ssl/certs/ /etc/ssl/certs/
 ---> 1a4aeb78202d
Step 6/6 : COPY ./ssl/private/ /etc/ssl/private/
 ---> 66525f85c1ff
Successfully built 66525f85c1ff
Successfully tagged nginx-ss.starbug.com:latest
```

### to run

This expects an instance of the aai-gunicorn image to be running as aai-gunicorn-00 on the nginx-proxy network. See my [Astronomy repo](https://github.com/lrmcfarland/Astronomy) for details.


```
$ docker network create nginx-proxy


$ docker run --net nginx-proxy --name nginx-ss-00.starbug.com -v /var/run/docker.sock:/tmp/docker.sock -d -p 80:80 -p 443:443 nginx-ss.starbug.com
ba70d94eede3f39be84d82d98e42c54ef200db94246cd5547e2735e5c9c6cf34


$ docker ps
CONTAINER ID        IMAGE                  COMMAND                  CREATED             STATUS              PORTS                                      NAMES
ba70d94eede3        nginx-ss.starbug.com   "nginx -g 'daemon ..."   21 seconds ago      Up 20 seconds       0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp   nginx-ss-00.starbug.com
c882e657781d        aai-gunicorn           "bash gunicorn.sh ..."   About an hour ago   Up About an hour    0.0.0.0:8080->8080/tcp                     aai-gunicorn-00
```

http should no point to the static starbug.com content and https to astronomical algorithms implemented.

### to debug

#### logs

```
$ docker logs nginx-ss-00.starbug.com
2017/11/12 07:16:22 [warn] 1#1: "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
nginx: [warn] "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
172.18.0.1 - - [12/Nov/2017:07:17:23 +0000] "GET / HTTP/1.1" 200 3284 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:32 +0000] "GET / HTTP/2.0" 200 5073 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:33 +0000] "GET / HTTP/2.0" 200 5073 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:41 +0000] "GET /sun_position_ajax HTTP/2.0" 200 8281 "https://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
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
