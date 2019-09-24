# Starbug.com

This is the repo for the starbug.com web site.

This shows how to run the starbug.com content in a container as a
stand alone an [Nginx](#Nginx-letsencrypt) [Transport Layer Security
(TLS)](https://en.wikipedia.org/wiki/Transport_Layer_Security) enabled
server and as a [Nginx self-signed server](#nginx-self-signed).  The
self-signed version can be used to bootstrap the the the TLS version.
In addition to being a good idea, the TLS endpoint is necessary to
allow the client to safely send the user's geolocation data for use
with the starbug.com astronomy applications, like finding the [sun's
position](https://aai.starbug.com/solar_azimuth_map) relative to the
user's location at the given time.


# To Build

A docker image of TLS version is built from [Dockerfile.nginx.letsencrypt](https://github.com/lrmcfarland/starbug.com/blob/master/Dockerfile.nginx.letsencrypt) like this:

```
docker build -f Dockerfile.nginx.letsencrypt -t tls.starbug.com .
```

The self-signed docker file is built from [Dockerfile.nginx.selfsigned](https://github.com/lrmcfarland/starbug.com/blob/master/Dockerfile.nginx.selfsigned) like this:

```
docker build -f Dockerfile.nginx.selfsigned -t ssl.starbug.com .
```

You will need to create you own self-signed certificates.
This Dockerfile will look for them in ./ssl/selfsigned.




# To Deploy

Once the images are built they can be deployed to a system running
docker using the docker compose scripts
[tls.starbug.com-compose.yaml](https://github.com/lrmcfarland/starbug.com/blob/master/tls.starbug.com-compose.yaml)
and
[ssl.starbug.com-compose.yaml](https://github.com/lrmcfarland/starbug.com/blob/master/ssl.starbug.com-compose.yaml)


# To Initialize


## Storage

On a new deployment you will need to create these volumes to have
persistent storage for configuration data like TLS certificates
and logs.

```
docker volume create --name=letsencrypt
docker volume create --name=starbugbackup
docker volume create -—name=starbugconfig
docker volume create -—name=starbugdata
docker volume create -—name=starbuglogs
docker volume create -—name=wellknown
```

## configuration

Once the storage is created it will need to be populated with the AAI
and OBSUI configuration files.  The docker
[tls.starbug.com-compose.yaml](https://github.com/lrmcfarland/starbug.com/blob/master/tls.starbug.com-compose.yaml)
expects the deployed version of the [AAI
config](https://github.com/lrmcfarland/AAI/blob/master/www/config/aai-flask-testing-config.py)
and the [OBSUI
config](https://github.com/lrmcfarland/starbugdb/blob/master/www/config/obsui-flask-testing-config.py)
to be in /opt/starbug.com/config/ The servers will default to their
test values if these are not present.


## certbot

The [Dockerfile.nginx.letsencrypt](https://github.com/lrmcfarland/starbug.com/blob/master/Dockerfile.nginx.letsencrypt)
adds EFF [certbot](https://certbot.eff.org/) to the standard Nginx
container. At this time it is on [Debian9
(stretch)](https://certbot.eff.org/lets-encrypt/debianstretch-nginx).
I found I could not include the stretch-backports in the instructions
(updated already?)  but this worked with out it.

Login to the container

```
docker exec -rt tls.starbug.com_00 bash

```

Run certbot

```
$ docker exec -it tls.starbug.com_00 bash

# cetbot --nginx

```

This will install the certs on the letsencrypt persistent storage.

The Dockerfile also sets up the [cerbot renew cron job](https://github.com/lrmcfarland/starbug.com/blob/master/conf/letsencrypt-renew.cron).


# To Run from the command line

To start the container from the command line

## Self-signed

From the comments at the top of [Dockerfile.nginx.letsencrypt](https://github.com/lrmcfarland/starbug.com/blob/master/Dockerfile.nginx.letsencrypt)

```
    docker run --name ssl.starbug.com_00 --net starbugnet \
    --mount source=starbuglogs,target=/opt/starbug.com/logs/nginx
    -v /var/run/docker.sock:/tmp/docker.sock -d -p 80:80 -p 443:443 ssl.starbug.com
```

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


## Letsencrypt

This will need to have DNS setup to route the challenge requests to
the wellknown location mounted here

```
    docker run --name tls.starbug.com_00 --net starbugnet \
    --mount source=letsencrypt,target=/etc/letsencrypt \
    --mount source=wellknown,target=/opt/starbug.com/www/.well-known \
    --mount source=starbuglogs,target=/opt/starbug.com/logs \
    --mount source=starbugbackup,target=/opt/starbug.com/backup  \
    -v /var/run/docker.sock:/tmp/docker.sock -d -p 80:80 -p 443:443 tls.starbug.com
```

# Logs


This container handles rotating the logs of the other containers in the
shared persistent storage with these cronjobs TODO link

To force a rotation

```
logrotate -d /etc/logrotate.conf
```

# To Debug


## Check docker container

Use the docker command line to check status and logs

```
$ docker ps -a
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS                      PORTS                    NAMES
da3514c07466        ss.starbug.com      "./nginx-start.sh"       44 seconds ago      Exited (1) 43 seconds ago                            ss.starbug.com_00
e459a39f53f3        aai_gunicorn        "bash ./bin/aai-guni…"   5 days ago          Up 5 days                   0.0.0.0:8080->8080/tcp   aai_gunicorn_00

```

## Check starbug logs


```
$ docker logs ss.starbug.com_00
Starting periodic command scheduler: cron.
2019/02/03 20:27:43 [emerg] 13#13: host not found in upstream "obsui-gunicorn-00" in /etc/nginx/conf.d/starbug.nginx.ss.conf:101
nginx: [emerg] host not found in upstream "obsui-gunicorn-00" in /etc/nginx/conf.d/starbug.nginx.ss.conf:101


```

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
