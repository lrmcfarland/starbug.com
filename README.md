# Starbug.com

This is the static content for the starbug.com website.

This shows how to run the starbug.com content in a container as a
stand alone an [Nginx letsencrypt reverse proxy](#Nginx-letsencrypt), [Nginx
self signed reverse proxy](#nginx-self-signed) or an [Apache httpd
daemon](#apache).

The nginx reverse proxy expects the AAI application to be running
in a separate container. It will act as a TLS termination point
so the browser's location can be sent. Both real CAs from letsencrypt
and self signed CAs are demonstrated below.

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


## Nginx letsencrypt

Create a real cert with [letsencrypt](https://letsencrypt.org)


### Get a cert with webroot



TODO certbot renewal dry run renewal fails to write files, but is that the right behavior?



After I got the initial certs using the dns challenge, I realized I
could not automate that renewal with cron and switched to using the
html web root challenge.  To support this I added two new docker
volumes for persistent storage: well-known for the challenge result
mounted on /opt/starbug/www/.well-known and letsencrypt on
/etc/letsencrypt. These are the default locations for cerbot to
operate. To back them up I also created a starbugbackup volume.

Note: change are local to the docker server. Don't forget pushing
your container image will not overwrite the persistent storage.


```

root@b45f6bdb96ce:/etc/letsencrypt# certbot certonly --webroot --agree-tos --email lrm@starbug.com -w /opt/starbug.com/www/.well-known/acme-challenge -d starbug.com,www.starbug.com,aai.starbug.com,db.starbug.com
Saving debug log to /var/log/letsencrypt/letsencrypt.log
Cert not yet due for renewal

You have an existing certificate that has exactly the same domains or certificate name you requested and isn't close to expiry.
(ref: /etc/letsencrypt/renewal/starbug.com.conf)

What would you like to do?
-------------------------------------------------------------------------------
1: Keep the existing certificate for now
2: Renew & replace the cert (limit ~5 per 7 days)
-------------------------------------------------------------------------------
Select the appropriate number [1-2] then [enter] (press 'c' to cancel): 2
Renewing an existing certificate
Performing the following challenges:
http-01 challenge for starbug.com
http-01 challenge for www.starbug.com
http-01 challenge for aai.starbug.com
http-01 challenge for db.starbug.com
Using the webroot path /opt/starbug.com/www/.well-known/acme-challenge for all unmatched domains.
Waiting for verification...
Cleaning up challenges
Generating key (2048 bits): /etc/letsencrypt/keys/0001_key-certbot.pem
Creating CSR: /etc/letsencrypt/csr/0001_csr-certbot.pem

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at
   /etc/letsencrypt/live/starbug.com/fullchain.pem. Your cert will
   expire on 2018-09-01. To obtain a new or tweaked version of this
   certificate in the future, simply run certbot again. To
   non-interactively renew *all* of your certificates, run "certbot
   renew"
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le



```


### Get a cert manually (superseded by webroot above)

Use letsencrypt's [certbot](https://certbot.eff.org) to register the
initial key. For my setup I picked the dns-01 challenge because I
could set it up before my server was running (as with the http-01 challenge).
This is time consuming to renew manually because of the wait for the
change to propagate.


```
$ certbot certonly --preferred-challenges dns --manual -d starbug.com -d www.starbug.com -d aai.starbug.com --config-dir ./config --work-dir ./work --logs-dir ./logs
Saving debug log to /Users/lrm/src/AAI/starbug.com/ssl/letsencrypt/logs/letsencrypt.log
Plugins selected: Authenticator manual, Installer None
Enter email address (used for urgent renewal and security notices) (Enter 'c' to
cancel): lrm@starbug.com

-------------------------------------------------------------------------------
Please read the Terms of Service at
https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf. You must
agree in order to register with the ACME server at
https://acme-v01.api.letsencrypt.org/directory
-------------------------------------------------------------------------------
(A)gree/(C)ancel: A

-------------------------------------------------------------------------------
Would you be willing to share your email address with the Electronic Frontier
Foundation, a founding partner of the Let's Encrypt project and the non-profit
organization that develops Certbot? We'd like to send you email about EFF and
our work to encrypt the web, protect its users and defend digital rights.
-------------------------------------------------------------------------------
(Y)es/(N)o: Y
Obtaining a new certificate
Performing the following challenges:
dns-01 challenge for starbug.com
dns-01 challenge for www.starbug.com
dns-01 challenge for aai.starbug.com

-------------------------------------------------------------------------------
NOTE: The IP of this machine will be publicly logged as having requested this
certificate. If you're running certbot in manual mode on a machine that is not
your server, please ensure you're okay with that.

Are you OK with your IP being logged?
-------------------------------------------------------------------------------
(Y)es/(N)o: Y

-------------------------------------------------------------------------------
Please deploy a DNS TXT record under the name
_acme-challenge.starbug.com with the following value:

...

Before continuing, verify the record is deployed.
-------------------------------------------------------------------------------
Press Enter to Continue

```


In another shell watch the record with dig to find when it is ready propagate (almost an
hour per change).


```

$ dig -t txt _acme-challenge.starbug.com

; <<>> DiG 9.8.3-P1 <<>> -t txt _acme-challenge.starbug.com
;; global options: +cmd
;; Got answer:
;; ->>HEADER<<- opcode: QUERY, status: NOERROR, id: 33734
;; flags: qr rd ra; QUERY: 1, ANSWER: 1, AUTHORITY: 0, ADDITIONAL: 0

;; QUESTION SECTION:
;_acme-challenge.starbug.com.	IN	TXT

;; ANSWER SECTION:
_acme-challenge.starbug.com. 7200 IN	TXT	"ssvpJYNtBAMDCpwLZAm017LeWULX_bY1cV4AhqcPybo"

;; Query time: 122 msec
;; SERVER: 2601:647:4580:87bd:e51:1ff:fee2:6660#53(2601:647:4580:87bd:e51:1ff:fee2:6660)
;; WHEN: Sat Nov 25 14:40:56 2017
;; MSG SIZE  rcvd: 101


```

continue until all three records have cleared.

```

Press Enter to Continue
Waiting for verification...
Cleaning up challenges
Non-standard path(s), might not work with crontab installed by your operating system package manager

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /Users/lrm/src/AAI/starbug.com/ssl/letsencrypt/config/live/starbug.com/fullchain.pem
   Your key file has been saved at:
   /Users/lrm/src/AAI/starbug.com/ssl/letsencrypt/config/live/starbug.com/privkey.pem
   Your cert will expire on 2018-02-23. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot
   again. To non-interactively renew *all* of your certificates, run
   "certbot renew"
 - Your account credentials have been saved in your Certbot
   configuration directory at
   /Users/lrm/src/AAI/starbug.com/ssl/letsencrypt/config. You should
   make a secure backup of this folder now. This configuration
   directory will also contain certificates and private keys obtained
   by Certbot so making regular backups of this folder is ideal.
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le


```



## Nginx self signed

This will use a self signed cert for testing. It roughly follows the
example given
[here](https://www.digitalocean.com/community/tutorials/how-to-create-a-self-signed-ssl-certificate-for-nginx-in-ubuntu-16-04)

For OS X, I also needed to manually add the cert file
/Users/lrm/src/starbug/starbug.com/ssl/selfsigned/certs/nginx-selfsigned.crt
to keychain-access. Drag and drop and turn on trust always.

### Create the self signed certs

```

$ mkdir -p ssl/selfsigned/private
$ mkdir -p ssl/selfsigned/certs


$ openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout ./ssl/selfsigned/private/nginx-selfsigned.key -out ./ssl/selfsigned/certs/nginx-selfsigned.crt
Generating a 2048 bit RSA private key
...........................+++
.............+++
writing new private key to './ssl/selfsigned/private/nginx-selfsigned.key'
-----
You are about to be asked to enter information that will be incorporated
into your certificate request.
What you are about to enter is what is called a Distinguished Name or a DN.
There are quite a few fields but you can leave some blank
For some fields there will be a default value,
If you enter '.', the field will be left blank.
-----
Country Name (2 letter code) []:US
State or Province Name (full name) []:CA
Locality Name (eg, city) []:Mountain View
Organization Name (eg, company) []:starbug.com
Organizational Unit Name (eg, section) []:starbug.com
Common Name (eg, fully qualified host name) []:starbug.com
Email Address []:lrm@starbug.com

```

Must be full path and exist or it will error after a long time.

```

$ openssl dhparam -out /Users/lrm/src/starbug/starbug.com/ssl/selfsigned/certs/dhparam.pem 2048

Generating DH parameters, 2048 bit long safe prime, generator 2
This is going to take a long time
.......

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

#### /etc/host DNS

intercepts DNS locally

#### edit /etc/hosts

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


#### docker logs

```
$ docker logs nginx-ss-00.starbug.com
2017/11/12 07:16:22 [warn] 1#1: "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
nginx: [warn] "ssl_stapling" ignored, issuer certificate not found for certificate "/etc/ssl/certs/nginx-selfsigned.crt"
172.18.0.1 - - [12/Nov/2017:07:17:23 +0000] "GET / HTTP/1.1" 200 3284 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:32 +0000] "GET / HTTP/2.0" 200 5073 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:33 +0000] "GET / HTTP/2.0" 200 5073 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
172.18.0.1 - - [12/Nov/2017:07:17:41 +0000] "GET /sun_position_ajax HTTP/2.0" 200 8281 "https://0.0.0.0/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/604.3.5 (KHTML, like Gecko) Version/11.0.1 Safari/604.3.5"
```


## Apache

This is with out TLS

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


#### interactive shell

```
docker run -it --entrypoint /bin/bash httpd.starbug.com

root@b6ee76f568c8:/usr/local/apache2# ls
bin  build  cgi-bin  conf  error  htdocs  icons  include  logs	modules

```

#### docker logs

```

$ docker logs apache-00.starbug.com
AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
AH00558: httpd: Could not reliably determine the server's fully qualified domain name, using 172.17.0.3. Set the 'ServerName' directive globally to suppress this message
[Fri Nov 10 23:27:56.933235 2017] [mpm_event:notice] [pid 1:tid 140681374685056] AH00489: Apache/2.4.29 (Unix) configured -- resuming normal operations
[Fri Nov 10 23:27:56.933780 2017] [core:notice] [pid 1:tid 140681374685056] AH00094: Command line: 'httpd -D FOREGROUND'

```
