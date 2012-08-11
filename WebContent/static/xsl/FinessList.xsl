<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml">

  <xsl:output method="xml" indent="yes" omit-xml-declaration="yes"
		encoding="UTF-8" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" />

  <xsl:template match="/">
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head>
        <meta equiv="Content-Type" content="application/xml; charset=UTF-8" />
        <title>Liste des finess</title>
        <link rel="stylesheet">
          <xsl:attribute name="href">
            <xsl:value-of select="/finesslist/@basedir" />/static/css/FinessList.css
          </xsl:attribute>
        </link>
      </head>

     <body>
     
       <table>
         <tr>
           <th>Numéro Finess</th>
           <th>Nom</th>
         </tr>
         <xsl:for-each select="/finesslist/finessechantillon/finess">
           <tr>
             <td><xsl:value-of select="./@num"/></td>
             <td><xsl:value-of select="./@name"/></td>
           </tr>
         </xsl:for-each>
       </table>
          
     </body>

    </html>
  
  </xsl:template>
	
</xsl:stylesheet>