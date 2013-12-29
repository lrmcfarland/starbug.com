<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- transform xml resume into html -->

  <xsl:template match="/">
    <html>
      <body>
	<xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>

  <!-- header -->

  <xsl:template match="subject">
    <center>
      <h1>
	<xsl:apply-templates select="first_name"/>
        <xsl:text> </xsl:text>
	<xsl:apply-templates select="middle_name"/>
        <xsl:text> </xsl:text>
	<xsl:apply-templates select="last_name"/>
      </h1>
      <h3>
        a.k.a. <xsl:apply-templates select="nick_name"/>
      </h3>
      <h3>
        Amateur Radio License: <xsl:apply-templates select="ham_license"/>
      </h3>
    </center>
  </xsl:template>

  <xsl:template match="objective">
    <p>
      <b>Objective:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="education">
    <p>
      <b>Education:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="contact">
    <p><b>Location:</b><xsl:apply-templates select="address"/></p>
    <p><b>Phone:</b><xsl:apply-templates select="phone"/></p>
    <p><b>Email:</b><xsl:apply-templates select="email"/></p>
    <p><b>Web:</b><xsl:apply-templates select="web"/></p>
    <p><b>Amateur Radio License:</b><xsl:apply-templates select="ham_license"/></p>
  </xsl:template>

  <xsl:template match="update">
    <p>
      <b>Updated:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- computer experience -->

  <xsl:template match="computer">

    <h2>Computer Experience</h2>
      <xsl:apply-templates select="summary"/>

  </xsl:template>

  <!-- employment history list -->

  <xsl:template match="history">

    <h2>Work Experience</h2>

	<xsl:for-each select="company">

	  <h3><xsl:apply-templates select="stats/name"/></h3>
	  <p><xsl:value-of select="stats/department"/></p>
	  <p><xsl:value-of select="stats/title"/></p>
	  <p><xsl:value-of select="stats/period"/></p>

	  <xsl:apply-templates select="summary"/>

	  <xsl:for-each select="details">
	    <xsl:apply-templates select="detail"/>
	  </xsl:for-each>

	</xsl:for-each>

  </xsl:template>

  <!-- uris with names -->
  <xsl:template match="uri[@name]">
      <xsl:apply-templates select="@name"/>
  </xsl:template>

  <!-- name -->
  <xsl:template match="name">
    <td>
      <xsl:apply-templates/>
    </td>
  </xsl:template>

  <!-- summary -->
  <xsl:template match="summary">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- details -->
  <xsl:template match="detail">
    <p>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

 </xsl:stylesheet>

<!-- EoF -->
