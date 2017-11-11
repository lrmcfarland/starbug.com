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


## Nginx

This will use letencrypt to set up TLS security.


### to build

```
$ docker build -f Dockerfile.nginx -t nginx.starbug.com .
Sending build context to Docker daemon   71.1MB
Step 1/4 : FROM nginx
 ---> 40960efd7b8f
Step 2/4 : COPY ./public_html/ /opt/starbug.com/www/public_html/
 ---> 363218cd7f62
Step 3/4 : COPY ./nginx.conf /etc/nginx/nginx.conf
 ---> cc22dda33783
Step 4/4 : COPY ./aai-nginx.conf /etc/nginx/conf.d/aai-nginx-rp0.conf
 ---> 9608d3fd276f
Successfully built 9608d3fd276f
Successfully tagged nginx.starbug.com:latest
```

### to run

This expects an instance of the aai-gunicorn image to be running as aai-gunicorn-00


```
$ docker run --name nginx-00.starbug.com --link aai-gunicorn-00:aai-gunicorn-00 -d -p 80:80 -p 443:443 nginx.starbug.com
8427bf13ed80fd5aa424fcf0c1a8597c447214c3b193c9576e497830b4c29e04


$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                      NAMES
8427bf13ed80        nginx.starbug.com   "nginx -g 'daemon ..."   3 seconds ago       Up 2 seconds        0.0.0.0:80->80/tcp, 0.0.0.0:443->443/tcp   nginx-00.starbug.com
299fcb2613f0        aai-gunicorn        "bash gunicorn.sh ..."   About an hour ago   Up About an hour    0.0.0.0:8080->8080/tcp                     aai-gunicorn-00

```

### to debug

#### logs

```
$ docker logs nginx-00.starbug.com

172.17.0.1 - - [11/Nov/2017:00:32:11 +0000] "GET /Orbits/orbits.html HTTP/1.1" 200 1774 "http://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:00:32:11 +0000] "GET /styles.css HTTP/1.1" 304 0 "http://0.0.0.0/Orbits/orbits.html" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:00:32:12 +0000] "GET /Orbits/orbits.mov HTTP/1.1" 206 2 "http://0.0.0.0/Orbits/orbits.html" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.17.0.1 - - [11/Nov/2017:00:32:12 +0000] "GET /Orbits/orbits.mov HTTP/1.1" 206 8031910 "http://0.0.0.0/Orbits/orbits.html" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"

```


#### interactive shell

```
$ docker run -it --entrypoint /bin/bash nginx.starbug.com

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



