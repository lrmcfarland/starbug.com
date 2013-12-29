#!/usr/bin/env bash

xsltproc cv2html_js.xsl cv.xml > resume_js.html
xsltproc cv2text_full.xsl cv.xml > resume_ftxt.html
xsltproc cv2text_short.xsl cv.xml > resume_stxt.html
