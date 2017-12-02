#!/bin/bash

# start cron and nginx

service cron start 
nginx -g 'daemon off;'
