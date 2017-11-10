2013dec29

cv.xml is the source, but with out formatting.
cv.dtd validates cv.xml.

Not all browsers support reading cv.xml directly, e.g. cv2html_js.xsl.
This works with safari, not firefox (see below).

multiple cv2html_*.xsl scripts are provided to generate 'resume'
versions in html using xsltproc:

xsltproc cv2html_js.xsl cv.xml > resume_js.html
xsltproc cv2text_full.xsl cv.xml > resume_ftxt.html
xsltproc cv2text_short.xsl cv.xml > resume_stxt.html

resume_js.html: full text with javascript
resume_ftxt.html: full text only
resume_stxt.html: short text only

-----

2011oct02

To avoid copying, you can use DTD ENTITY, but they will not work with
firefox which does not load external DTDs.  This is a security issue
to prevent DoS attacks in xml files.

See http://stackoverflow.com/questions/1512747/will-firefox-do-xslt-on-external-entities

https://bugzilla.mozilla.org/show_bug.cgi?id=204102

cv_import_full.xml uses ENTITY importing to combine cv.xml with
cv2html_full.xml style sheet. This works with safari, not firefox.

cv_import_js.xml uses ENTITY importing to combine cv.xml with
cv2html_js.xml style sheet. This works with safari, not firefox.
