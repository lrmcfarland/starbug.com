<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- transform xml resume into html -->

  <xsl:template match="/">
    <html>
      <head>
	<link href="../styles.css" rel="stylesheet" type="text/css"/>
      </head>
      <body>
	<table width="800" cellpadding="3">
	  <xsl:apply-templates/>
	</table>
      </body>
    </html>
  </xsl:template>

  <!-- header -->

  <xsl:template match="subject">
    <tr>
      <td>
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
	</center>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="objective">
    <tr>
      <td>
	<b>Objective:</b><xsl:apply-templates/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="update">
    <tr>
      <td>
	<b>Updated:</b><xsl:apply-templates/>
      </td>
    </tr>
  </xsl:template>

  <!-- contact information -->

  <xsl:template match="contact">

    <tr>
      <th align="left">
	<big>Contact Information</big>
      </th>
    </tr>

    <tr>
      <td>

	<ul>
	  <li><b>Web:</b><xsl:apply-templates select="web"/></li>
	  <li><b>Email:</b><xsl:apply-templates select="email"/></li>
	  <li><b>Phone:</b><xsl:apply-templates select="phone"/></li>
	  <li><b>Location:</b><xsl:apply-templates select="address"/></li>
	  <li><b>Education:</b><xsl:apply-templates select="education"/></li>
	</ul>

      </td>
    </tr>
  </xsl:template>

  <!-- computer experience -->

  <xsl:template match="computer">

    <tr>
      <th align="left">
	<big>Computer Experience</big>
      </th>
    </tr>

    <tr>
      <td>

	<xsl:for-each select="detail">
	  <p>
	    <xsl:apply-templates/>
	  </p>
	</xsl:for-each>

      </td>
    </tr>

  </xsl:template>

  <!-- employment history list -->

  <xsl:template match="history">

    <tr>
      <th align="left">
	<big>Work Experience</big>
      </th>
    </tr>

    <xsl:for-each select="company">

      <tr>

	<th align="left">
	  <xsl:apply-templates select="stats/name"/>
	</th>

	<tr>
	  <td>
	    <xsl:value-of select="stats/title"/>
	  </td>
	</tr>

	<tr>
	  <td>
	    <xsl:value-of select="stats/department"/>
	  </td>
	</tr>

	<tr>
	  <td>
	    <xsl:value-of select="stats/employed"/>
	  </td>
	</tr>

	<tr>
	  <td>
	    <xsl:apply-templates select="summary"/>
	  </td>
	</tr>

	<xsl:for-each select="details">
	  <xsl:apply-templates select="detail"/>
	</xsl:for-each>

      </tr>

    </xsl:for-each>

  </xsl:template>

  <!-- uris with names -->
  <xsl:template match="uri[@name]">
      <xsl:apply-templates select="@name"/>
  </xsl:template>

  <!-- name -->
  <xsl:template match="name">
      <xsl:apply-templates/>
  </xsl:template>

  <!-- summary -->
  <xsl:template match="summary">
    <tr>
      <td>
	<xsl:apply-templates/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="detail">
    <tr>
      <td>
	<xsl:apply-templates/>
      </td>
    </tr>
  </xsl:template>

 </xsl:stylesheet>

<!-- EoF -->
