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
           <th>Etablissement</th>
           <th>Numéro Finess</th>
         </tr>
         <xsl:for-each select="/finesslist/finessechantillon/finess">
           <tr>
             <td><xsl:value-of select="./@name"/></td>
             <td><a><xsl:attribute name="href">
               <xsl:value-of select="/finesslist/@basedir" />/Finess/<xsl:value-of select="./@num"/>
               </xsl:attribute><xsl:value-of select="./@num"/></a></td>
           </tr>
         </xsl:for-each>
       </table>
       
       <table>
         <tr>
           <td>
             <xsl:choose>
               <xsl:when test="/finesslist/finessechantillon/@first &lt;= 1">
                 Precedent
               </xsl:when>
               <xsl:otherwise>
                 <a><xsl:attribute name="href"><xsl:value-of select="/finesslist/@basedir" />/FinessList/<xsl:call-template name="getPrevIndex">
                   <xsl:with-param name="lastIndex" select="/finesslist/finessechantillon/@last" />
                   </xsl:call-template></xsl:attribute>
                   Precedent
                 </a>
               </xsl:otherwise>
             </xsl:choose>
           </td>
           <td>
             <xsl:choose>
               <xsl:when test="/finesslist/finessechantillon/@last &gt;= /finesslist/finessechantillon/@totalcount">
                 Suivant
               </xsl:when>
               <xsl:otherwise>
                 <a><xsl:attribute name="href"><xsl:value-of select="/finesslist/@basedir" />/FinessList/<xsl:call-template name="getNextIndex">
                   <xsl:with-param name="lastIndex" select="/finesslist/finessechantillon/@last" />
                   </xsl:call-template></xsl:attribute>
                   Suivant
                 </a>
               </xsl:otherwise>
             </xsl:choose>
           </td>
         </tr>
       </table>
     </body>

    </html>
  
  </xsl:template>

  <xsl:template name="getPrevIndex">
    <xsl:param name="lastIndex" />
    <xsl:value-of select="$lastIndex div 10 - 2" />
  </xsl:template>
  <xsl:template name="getNextIndex">
    <xsl:param name="lastIndex" />
    <xsl:value-of select="$lastIndex div 10" />
  </xsl:template>
</xsl:stylesheet>