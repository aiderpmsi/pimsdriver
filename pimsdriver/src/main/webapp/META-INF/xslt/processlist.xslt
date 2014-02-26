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
			<xsl:variable name="firstrow" select="/uploaded/askedFirst/text()" />
			<xsl:variable name="numrows" select="/uploaded/askedRows/text()" />

			<div class="simpletable">
				<div class="header">
					<a>
						<xsl:attribute name="href">
							<xsl:value-of
							select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'finess'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
						</xsl:attribute>
						<div class="headercontent">Finess</div>
					</a>
					<a>
						<xsl:attribute name="href">
							<xsl:value-of
							select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'year'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
						</xsl:attribute>
						<div class="headercontent">Année PMSI</div>
					</a>
					<a>
						<xsl:attribute name="href">
							<xsl:value-of
							select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'month'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
						</xsl:attribute>
						<div class="headercontent">Mois PMSI</div>
					</a>
					<a>
						<xsl:attribute name="href">
							<xsl:value-of
							select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'dateenvoi'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
						</xsl:attribute>
						<div class="headercontent">Date d'envoi</div>
					</a>
					<div class="headercontent">Commentaire</div>
					<div class="headercontent">Action</div>
				</div>
				<div class="content">
					<xsl:for-each select="/uploaded/elements/element">
						<div class="element">
							<div>
								<xsl:value-of select="finess/text()" />
							</div>
							<div>
								<xsl:value-of select="year/text()" />
							</div>
							<div>
								<xsl:value-of select="month/text()" />
							</div>
							<div>
								<xsl:value-of select="dateEnvoi/text()" />
							</div>
							<div>
								<xsl:value-of select="comment/text()" />
							</div>
							<div>
								<xsl:choose>
									<xsl:when test="processed/text() = 'waiting'">
										<a>
											<xsl:attribute name="href">
												<xsl:value-of select="concat('./process/', recordId/text(), '?')" />
												<xsl:call-template name="createtableurl">
													<xsl:with-param name="ordername" select="''" />
													<xsl:with-param name="addorder" select="'false'" />
													<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
													<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
													</xsl:call-template>
											</xsl:attribute>
											Traiter
										</a>
									</xsl:when>
									<xsl:when test="processed/text() = 'pending'">
										En cours de traitement
									</xsl:when>
									<xsl:otherwise>
										Déjà traité
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</xsl:for-each>
				</div>
			</div>


			<div class="filter">
				<xsl:choose>
					<xsl:when test="/uploaded/onlyPending/text() = 'true'">
						<a>
							<xsl:attribute name="href">
							<xsl:value-of
								select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="''" />
								<xsl:with-param name="addorder" select="'false'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
							<xsl:value-of select="'onlyPending=false&amp;'" />
						</xsl:attribute>
							<input type="checkbox" name="onlyPending" checked="checked" />
							Uniquement les envois en attente de traitement
						</a>
					</xsl:when>
					<xsl:otherwise>
						<a>
							<xsl:attribute name="href">
							<xsl:value-of
								select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="''" />
								<xsl:with-param name="addorder" select="'false'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							</xsl:call-template>
							<xsl:value-of select="'onlyPending=true&amp;'" />
						</xsl:attribute>
							<input type="checkbox" name="onlyPending" />
							Uniquement les envois en attente de traitement
						</a>
					</xsl:otherwise>
				</xsl:choose>

			</div>

		</html>
	</xsl:template>

	<xsl:template name="createtableurl">

		<xsl:param name="ordername" />
		<xsl:param name="addorder" />
		<xsl:param name="orderdesclist" />
		<xsl:param name="orderlist" />

		<xsl:for-each select="$orderdesclist/order">
			<!-- Remember the position of this node -->
			<xsl:variable name="count" select="count(preceding-sibling::*) + 1" />
			<xsl:choose>
				<!-- if order elements is already defined for this element and order 
					is true, we have to change this ordering to false -->
				<xsl:when
					test="text() = $ordername and $orderlist/orderdir[$count]/text() = 'true'">
					<xsl:value-of
						select="concat('orderelts=', text(), '&amp;order=false&amp;')" />
				</xsl:when>
				<!-- if order elements is already defined for this element and order 
					is false, we have to remove this ordering -->
				<xsl:when
					test="text() = $ordername and $orderlist/orderdir[$count]/text() = 'false'" />

				<!-- if order elements is not for this element, we have just to copy -->
				<xsl:otherwise>
					<xsl:value-of
						select="concat('orderelts=', text(), '&amp;order=',
											$orderlist/orderdir[$count]/text(), '&amp;')" />
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
		<!-- If no ordering was defined for this element, and if addorder is true, create one more -->
		<xsl:choose>
			<xsl:when
				test="$addorder = 'true' and not($orderdesclist/order[text() = $ordername])">
				<xsl:value-of
					select="concat('orderelts=', $ordername, '&amp;order=true&amp;')" />
			</xsl:when>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>