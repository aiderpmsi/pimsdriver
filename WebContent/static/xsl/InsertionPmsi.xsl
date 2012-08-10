<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"
		encoding="ISO-8859-1" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta equiv="Content-Type" content="application/xml; charset=ISO-8859-1" />
        <title>R�sultat d'insertion de fichier pmsi</title>
        <link rel="stylesheet">
          <xsl:attribute name="href">
            <xsl:value-of select="/insertionpmsi/@basedir" />/static/css/InsertionPmsi.css
          </xsl:attribute>
        </link>
      </head>

     <body>
     </body>

    </html>
  
  </xsl:template>
	
</xsl:stylesheet>