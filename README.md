# Starbug.com

This is the repo for the starbug.com web site.

This shows how to run the starbug.com content in a container as a
stand alone an [Nginx letsencrypt reverse proxy](#Nginx-letsencrypt), [Nginx
self signed reverse proxy](#nginx-self-signed) or an [Apache httpd
daemon](#apache).

In addition to being a good idea, the TLS transport is necessary to
allow the client to safely send the user's geolocation data for use
with the starbug.com astronomy applications, like finding the [sun's
position](https://aai.starbug.com/solar_azimuth_map) relative to the
user's location at a give time.

The stand alone Apache version does not include wsgi at this time,
just the static content.  I have of how to run everything under Apache
discussion of that in the [Astronomy/www
repo](https://github.com/lrmcfarland/Astronomy/tree/master/www), but
not as a TLS endpoint (yet).


# To Build

To use nginx with real certificate authority (ca) for starbug.com, use

[Dockerfile.nginx.letsencrypt](https://github.com/lrmcfarland/starbug.com/blob/docker-compose/Dockerfile.nginx.letsencrypt)

```
docker build -f Dockerfile.nginx.letsencrypt -t tls.starbug.com .
```

For testing there is a self signed certificate version here

[Dockerfile.nginx.selfsigned](https://github.com/lrmcfarland/starbug.com/blob/docker-compose/Dockerfile.nginx.selfsigned)

# To Deploy

## With docker compose

### letsencrypt

TODO


### self signed

```
docker-compose -f ssl.starbug.com-compose.yaml up -d

```



## With out docker compose


```

# one time setup
# storage:        docker volume create starbuglogs
#                 docker volume create letsencrypt
#                 docker volume create wellknown
#                 docker volume create starbugbackup
#
# create network: docker network create starbugnet
#
# to build:       docker build -f Dockerfile.nginx.letsencrypt -t ca.starbug.com .
#
# to run:         docker run --name ca.starbug.com-00 --net starbugnet \
                    --mount source=letsencrypt,target=/etc/letsencrypt \
		    --mount source=wellknown,target=/opt/starbug.com/www/.well-known \
		    --mount source=starbuglogs,target=/opt/starbug.com/logs \
		    --mount source=starbugbackup,target=/opt/starbug.com/backup \
		    -v /var/run/docker.sock:/tmp/docker.sock -d -p 80:80 -p 443:443 ca.starbug.com

# to bash:        docker exec -it ca.starbug.com-00 bash

```



# Logs


This container handles rotating the logs of the other containers in the
shared persistant storage with these cronjobs TODO link

To force a rotation

```
   # logrotate -d /etc/logrotate.conf
```

# Letsencrypt renewal

Cronjobs are used for maintaining the
[letsencrypt](https://letsencrypt.org) certs with regular certbot
renewals and rotating the logs. The configuration for these are
located in [conf](./conf) and setup in [Dockerfile.nginx.letsencrypt](./Dockerfile.nginx.letsencrypt)
and [Dockerfile.nginx.selfsigned](./Dockerfile.nginx.selfsigned)


To test

```

   # certbot renew --dry-run

    Saving debug log to /var/log/letsencrypt/letsencrypt.log
    ** DRY RUN: simulating 'certbot renew' close to cert expiry
    **          (The test certificates below have not been saved.)

    No renewals were attempted.
    ** DRY RUN: simulating 'certbot renew' close to cert expiry
    **          (The test certificates above have not been saved.)
    root@01462450a452:/opt/starbug.com/logs/nginx# more /var/log/letsencrypt/letsencrypt.log
    2018-01-14 21:57:45,615:DEBUG:certbot.main:Root logging level set at 20
    2018-01-14 21:57:45,616:INFO:certbot.main:Saving debug log to /var/log/letsencrypt/letsencrypt.log
    2018-01-14 21:57:45,617:DEBUG:certbot.main:certbot version: 0.10.2
    2018-01-14 21:57:45,617:DEBUG:certbot.main:Arguments: ['--dry-run']
    2018-01-14 21:57:45,617:DEBUG:certbot.main:Discovered plugins: PluginsRegistry(PluginEntryPoint#webroot,PluginEntryPoint#null,PluginEntryPoint#manual,PluginEntryPoint#standalone)
    2018-01-14 21:57:45,618:DEBUG:certbot.renewal:no renewal failures

```

# To Install

Clone this repo from GitHub and run docker to build the containers.

This part is still small enough to be done "by hand", but
I am working on using a deployment tool like Kuberneties
or OpenShift to automate that too.

## one time setup

### storage

To support letsencrypt renewals I added two docker volumes for
persistent storage: well-known for the challenge result mounted on
/opt/starbug/www/.well-known and letsencrypt configuration on
/etc/letsencrypt. These are the default locations for cerbot to
operate. I also created a starbugbackup volume for backups.

```
    docker volume create starbuglogs
    docker volume create letsencrypt
    docker volume create wellknown
    docker volume create starbugbackup

```

### create a network

The containers communicate on docker's internal network

```
   docker network create starbugnet

```


# To Build

There are several docker files to build different configurations.
[Dockerfile.nginx.letsencrypt](Dockerfile.nginx.letsencrypt) is
the deployment version.
There is also [a self signed cert
example](Dockerfile.nginx.selfsigned) for testing and [an Apache
one](Dockerfile.nginx.selfsigned) that only serves static starbug.com
content.
Human readable comments for how to build and run the
containers are also in the Dockerfiles but the basic idea is

```
    docker build -f Dockerfile.nginx.letsencrypt -t tls.starbug.com .
```



# To Run

TODO this is where an orchestration tool will really help

```
    docker run --name tls.starbug.com_00 --net starbugnet \
    --mount source=letsencrypt,target=/etc/letsencrypt \
    --mount source=wellknown,target=/opt/starbug.com/www/.well-known \
    --mount source=starbuglogs,target=/opt/starbug.com/logs \
    --mount source=starbugbackup,target=/opt/starbug.com/backup  \
    -v /var/run/docker.sock:/tmp/docker.sock -d -p 80:80 -p 443:443 tls.starbug.com
```



# To Debug


## Check docker container

Use the docker command line to check status and logs

```
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                      PORTS                    NAMES
da3514c07466        ss.starbug.com      "./nginx-start.sh"       44 seconds ago      Exited (1) 43 seconds ago                            ss.starbug.com_00
e459a39f53f3        aai_gunicorn        "bash ./bin/aai-guni…"   5 days ago          Up 5 days                   0.0.0.0:8080->8080/tcp   aai_gunicorn_00


$ docker logs ss.starbug.com_00
Starting periodic command scheduler: cron.
2019/02/03 20:27:43 [emerg] 13#13: host not found in upstream "obsui-gunicorn-00" in /etc/nginx/conf.d/starbug.nginx.ss.conf:101
nginx: [emerg] host not found in upstream "obsui-gunicorn-00" in /etc/nginx/conf.d/starbug.nginx.ss.conf:101


```


## Check starbug logs


Open an interactive shell on the contianer

```

ubuntu@ip-172-31-15-156:~$ docker exec -it ca.starbug.com-00 bash
root@93469c54f8c2:~# cd /opt/starbug.com/logs/


root@93469c54f8c2:/opt/starbug.com/logs# ls
aai-access.log		       gobsui-error.log


root@93469c54f8c2:/opt/starbug.com/logs# tail -100 nginx-access.log

35.229.29.110 - - [03/Feb/2019:11:11:08 +0000] "GET /Kayaking/grand_canyon.html HTTP/1.0" 200 492 "-" "ZoominfoBot (zoominfobot at zoominfo dot com)"
35.229.29.110 - - [03/Feb/2019:11:12:13 +0000] "GET /Kayaking/waterfall.html HTTP/1.0" 200 528 "-" "ZoominfoBot (zoominfobot at zoominfo dot com)"
35.229.29.110 - - [03/Feb/2019:11:16:50 +0000] "GET /robots.txt HTTP/1.0" 404 233 "-" "ZoominfoBot (zoominfobot at zoominfo dot com)"
35.229.29.110 - - [03/Feb/2019:11:16:50 +0000] "GET / HTTP/1.0" 200 4124 "-" "ZoominfoBot (zoominfobot at zoominfo dot com)"
35.229.29.110 - - [03/Feb/2019:11:21:01 +0000] "GET /Astronomy/jodrell_bank.html HTTP/1.0" 200 519 "-" "ZoominfoBot (zoominfobot at zoominfo dot com)"

```


## self signed 

To test with self signed cert certs you can hack /etc/hosts to 
intercepts DNS locally.

### edit /etc/hosts

```
##
127.0.0.1       localhost
255.255.255.255 broadcasthost
::1             localhost

# nginx testing
0.0.0.0 starbug.com
0.0.0.0 www.starbug.com
0.0.0.0 aai.starbug.com
```


# To Clean Up

```
to delete all containers:  docker rm $(docker ps -a -q)
to delete all images:      docker rmi $(docker images -q)
to delete dangling images: docker rmi $(docker images -q -f dangling=true)
```



