<?xml version="1.0"?>

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" indent="yes"/>

  <!-- transform xml resume into html -->

  <xsl:template match="/">
    <html>

      <head>
	<link href="../styles.css" rel="stylesheet" type="text/css"/>
        <script type="text/javascript">
          function setVisibility(id, visibility) {
            document.all[id].style.display = visibility;
	  }
        </script>
      </head>

      <body>
	<table>
	  <tr>
	    <td width="800">
	      <xsl:apply-templates/>
	    </td>
	  </tr>
	</table>
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
    </center>
  </xsl:template>

  <xsl:template match="objective">
    <p>
      <b>Objective:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="update">
    <p>
      <b>Updated:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- contact information -->

  <xsl:template match="contact">

    <h2>Contact Information</h2>
    <ul>
      <li><b>Location:</b><xsl:apply-templates select="address"/></li>
      <li><b>Phone:</b><xsl:apply-templates select="phone"/></li>
      <li><b>Email:</b><xsl:apply-templates select="email"/></li>
      <li><b>Web:</b><xsl:apply-templates select="web"/></li>
      <li><b>Amateur Radio License:</b><xsl:apply-templates select="ham_license"/></li>
    </ul>
  </xsl:template>

  <xsl:template match="education">
    <p>
      <b>Education:</b><xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- computer experience -->

  <xsl:template match="computer">

    <h2>Computer Experience</h2>

    <xsl:for-each select="detail">
      <p>
        <xsl:apply-templates/>
      </p>
    </xsl:for-each>

  </xsl:template>

  <!-- employment history list -->

  <xsl:template match="history">

    <h2>Work Experience</h2>

	<xsl:for-each select="company">

	  <xsl:apply-templates select="stats/uri"/>

	  <table>
	    <tr>
	      <th>Company</th>
	      <xsl:apply-templates select="stats/name"/>
	    </tr>
	    <tr>
	      <th>Department</th>
	      <td><xsl:value-of select="stats/department"/></td>
	    </tr>
	    <tr>
	      <th>Title</th>
	      <td><xsl:value-of select="stats/title"/></td>
	    </tr>
	    <tr>
	      <th>Period</th>
	      <td><xsl:value-of select="stats/period"/></td>
	    </tr>
	  </table>

	  <xsl:apply-templates select="summary"/>

          <xsl:apply-templates select="details"/>

	</xsl:for-each>

  </xsl:template>

  <!-- uris with names -->
  <xsl:template match="uri[@name]">
    <a>
      <xsl:attribute name="href">
	<xsl:apply-templates/>
      </xsl:attribute>

      <xsl:apply-templates select="@name"/>

    </a>
  </xsl:template>

  <!-- uris with logos -->
  <xsl:template match="uri[@logo]">
    <a>
      <xsl:attribute name="href">
	<xsl:apply-templates/>
      </xsl:attribute>

      <img align="right">
	<xsl:attribute name="src">
	  <xsl:apply-templates select="@logo"/>
	</xsl:attribute>
      </img>
    </a>
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


  <!-- details -->
  <xsl:template match="details">

    <xsl:variable name="detail_id">
      <xsl:number count="details" level="any"/>
    </xsl:variable>

        <div id="less_details_{$detail_id}" style="display:none">

          <xsl:for-each select="detail">
            <p>
              <xsl:apply-templates/>
            </p>
          </xsl:for-each>

          <input type="button" value="less" onClick="setVisibility('less_details_{$detail_id}', 'none'); setVisibility('more_details_{$detail_id}', 'inline');"></input>

        </div>


        <div id="more_details_{$detail_id}" style="display:inline">

          <input type="button" value="more" onClick="setVisibility('less_details_{$detail_id}', 'inline'); setVisibility('more_details_{$detail_id}', 'none');"></input>

        </div>





        <p></p>

  </xsl:template>


 </xsl:stylesheet>

<!-- EoF -->
