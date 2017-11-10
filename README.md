# Starbug.com

This is the static content for the starbug.com website.

# dockerize

This shows how to run the starbug.com content in a container as a
stand alone Apache httpd daemon and how to mount it as a volume for
Nginx to mount as a reverse proxy.
The nginx proxy is described here TODO.


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


### to clean up

```
to delete all containers:  docker rm $(docker ps -a -q)
to delete all images:      docker rmi $(docker images -q)
to delete dangling images: docker rmi $(docker images -q -f dangling=true)
```



## Nginx

to put in a container volume as static content for nginx


