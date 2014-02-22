<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" doctype-system="about:legacy-compat"
		encoding="UTF-8" indent="yes" />

	<xsl:template match="/">

		<html>

			<head>
				<title>NPIMSDRIVER</title>
				<link rel="stylesheet" href="../resources/css/processlist.css" />
			</head>
			
			<!-- Retrieve the values or the anchors -->
			<xsl:variable name="firstrow" select="/uploaded/askedFirst/text()"/>
			<xsl:variable name="numrows" select="/uploaded/askedRows/text()" />

			<div class="simpletable">
				<div class="header">
					<a>
						<xsl:attribute name="href">
							<xsl:value-of select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:for-each select="/uploaded/order/order">
								<!-- Remember the position of this node -->
								<xsl:variable name="count" select="count(preceding-sibling::text-body) + 1"/>
								<xsl:choose>
									<!-- if order elements is already defined for this element
									     and order is true, we have to change this ordering to false-->
									<xsl:when test="./text() = 'finess' and /uploaded/orderdirection/orderdirection[$count]/text() = 'true'">
										<xsl:value-of select="concat('orderelts=', text(), '&amp;order=false&amp;')" />
									</xsl:when>
									<!-- if order elements is already defined for this element
									     and order is false, we have to remove this ordering -->
									<xsl:when test="./text() = 'finess' and /uploaded/orderdirection/orderdirection[$count]/text() = 'false'">
									</xsl:when>
									<!-- if order elements is not for this element, we have just to copy -->
									<xsl:otherwise>
										<xsl:value-of select="concat('orderelts=', text(), '&amp;order=',
											/uploaded/orderdirection/orderdirection[$count]/text(), '&amp;')" />
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
							<!-- If no ordering was defined for this element, create one more -->
							<xsl:choose>
								<xsl:when test="not(/uploaded/order/order[text() = 'finess'])">
										<xsl:value-of select="'orderelts=finess&amp;order=true&amp;'" />
								</xsl:when>
							</xsl:choose>
						</xsl:attribute>
					<div class="headercontent">Finess</div>
					</a>
				</div>
			</div>
		</html>
	</xsl:template>

</xsl:stylesheet>