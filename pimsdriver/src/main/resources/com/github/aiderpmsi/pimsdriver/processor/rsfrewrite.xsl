<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" encoding="UTF-8" indent="yes" />

	<xsl:template match="/">

		<entry type="rsfheader">
			<xsl:for-each select="/root/rsfheader/*">
				<xsl:variable name="LocalName" select="local-name()" />
				<xsl:attribute name="{$LocalName}"> 
                       <xsl:value-of select="./text()" />
                </xsl:attribute>
			</xsl:for-each>
			<xsl:for-each select="/root/*[name() = 'rsfa' or name() = 'rsfb'
				or name() = 'rsfc' or name() = 'rsfh' or name() = 'rsfi'
				or name() = 'rsfl' or name() = 'rsfm']">
				<entry>
					<xsl:for-each select="./*">
						<xsl:variable name="LocalName" select="local-name()" />
						<xsl:attribute name="{$LocalName}"> 
                       <xsl:value-of select="./text()" />
                </xsl:attribute>
					</xsl:for-each>
				</entry>
			</xsl:for-each>
		</entry>

	</xsl:template>
</xsl:stylesheet>