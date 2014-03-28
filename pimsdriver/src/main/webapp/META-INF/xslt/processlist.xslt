<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
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
				<div class="content">
					<div class="header">
						<div class="element">
							<a>
								<xsl:attribute name="href">
							<xsl:value-of
									select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'finess'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
								<xsl:with-param name="filtermanage" select="'copy'" />
								<xsl:with-param name="onlyPendingValue"
									select="/uploaded/onlyPending/text()" />
							</xsl:call-template>
						</xsl:attribute>
								<div class="headercontent">Finess</div>
							</a>
						</div>
						<div class="element">
							<a>
								<xsl:attribute name="href">
							<xsl:value-of
									select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'year'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
								<xsl:with-param name="filtermanage" select="'copy'" />
								<xsl:with-param name="onlyPendingValue"
									select="/uploaded/onlyPending/text()" />
							</xsl:call-template>
						</xsl:attribute>
								<div class="headercontent">Année PMSI</div>
							</a>
						</div>
						<div class="element">
							<a>
								<xsl:attribute name="href">
							<xsl:value-of
									select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'month'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
								<xsl:with-param name="filtermanage" select="'copy'" />
								<xsl:with-param name="onlyPendingValue"
									select="/uploaded/onlyPending/text()" />
							</xsl:call-template>
						</xsl:attribute>
								<div class="headercontent">Mois PMSI</div>
							</a>
						</div>
						<div class="element">
							<a>
								<xsl:attribute name="href">
							<xsl:value-of
									select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="'dateenvoi'" />
								<xsl:with-param name="addorder" select="'true'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
								<xsl:with-param name="filtermanage" select="'copy'" />
								<xsl:with-param name="onlyPendingValue"
									select="/uploaded/onlyPending/text()" />
							</xsl:call-template>
						</xsl:attribute>
								<div class="headercontent">Date d'envoi</div>
							</a>
						</div>
						<div class="element">Commentaire</div>
						<div class="element">Action</div>
					</div>
					<xsl:for-each select="/uploaded/elements/element">
						<div class="row">
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
								<xsl:value-of
									select="format-dateTime(dateEnvoi/text(), 
                          		'[M01]/[D01]/[Y0001] [H01]:[m01]:[s01]')" />
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
													<xsl:with-param name="orderdesclist"
												select="/uploaded/orders" />
													<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
													<xsl:with-param name="filtermanage" select="'copy'" />
													<xsl:with-param name="onlyPendingValue"
												select="/uploaded/onlyPending/text()" />
												</xsl:call-template>
											</xsl:attribute>
											Traiter
										</a>
									</xsl:when>
									<xsl:when test="processed/text() = 'pending'">
										En cours de traitement
									</xsl:when>
									<xsl:when test="processed/text() = 'failed'">
										<xsl:value-of select="'Echec du traitement : '" />
										<xsl:value-of select="processed/errorComment/text()"/>
									</xsl:when>
									<xsl:otherwise>
										Déjà traité
									</xsl:otherwise>
								</xsl:choose>
							</div>
						</div>
					</xsl:for-each>

				</div>

				<div class="footer">
					<div class="paginator">
						<xsl:variable name="prev_firstrow">
							<xsl:choose>
								<xsl:when test="($firstrow - $numrows) &lt; 0">
									<xsl:value-of select="0" />
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$firstrow - $numrows" />
								</xsl:otherwise>
							</xsl:choose>
						</xsl:variable>

						<div class="pre">
							<a>
								<xsl:attribute name="href">
							<xsl:value-of
									select="concat('./list?first=', $prev_firstrow, '&amp;rows=', $numrows, '&amp;')" />
							<xsl:call-template name="createtableurl">
								<xsl:with-param name="ordername" select="''" />
								<xsl:with-param name="addorder" select="'false'" />
								<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
								<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
								<xsl:with-param name="filtermanage" select="'copy'" />
								<xsl:with-param name="onlyPendingValue"
									select="/uploaded/onlyPending/text()" />
							</xsl:call-template>
						</xsl:attribute>
								Precedent
							</a>
						</div>
						<div class="post">
							<xsl:choose>
								<xsl:when test="/uploaded/lastChunk[text() = 'false']">
									<a>
										<xsl:attribute name="href">
									<xsl:value-of
											select="concat('./list?first=', $firstrow + $numrows, '&amp;rows=', $numrows, '&amp;')" />
									<xsl:call-template name="createtableurl">
										<xsl:with-param name="ordername" select="''" />
										<xsl:with-param name="addorder" select="'false'" />
										<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
										<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
										<xsl:with-param name="filtermanage" select="'copy'" />
										<xsl:with-param name="onlyPendingValue"
											select="/uploaded/onlyPending/text()" />
									</xsl:call-template>
								</xsl:attribute>
										Suivant
									</a>
								</xsl:when>
							</xsl:choose>
						</div>
					</div>
					<div class="filter">
						<a>
							<xsl:attribute name="href">
						<xsl:value-of
								select="concat('./list?first=', $firstrow, '&amp;rows=', $numrows, '&amp;')" />
						<xsl:call-template name="createtableurl">
							<xsl:with-param name="ordername" select="''" />
							<xsl:with-param name="addorder" select="'false'" />
							<xsl:with-param name="orderdesclist" select="/uploaded/orders" />
							<xsl:with-param name="orderlist" select="/uploaded/orderdirs" />
							<xsl:with-param name="filtermanage" select="'switch'" />
							<xsl:with-param name="onlyPendingValue"
								select="/uploaded/onlyPending/text()" />
						</xsl:call-template>
					</xsl:attribute>
							<xsl:choose>
								<xsl:when test="/uploaded/onlyPending/text() = 'true'">
									<input type="checkbox" name="onlyPending" checked="checked" />
									Uniquement les envois en attente de traitement
								</xsl:when>
								<xsl:otherwise>
									<input type="checkbox" name="onlyPending" />
									Uniquement les envois en attente de traitement
								</xsl:otherwise>
							</xsl:choose>
						</a>
					</div>
				</div>
			</div>

		</html>
	</xsl:template>

	<xsl:template name="createtableurl">

		<xsl:param name="addorder" /> <!-- Defines if we have to switch / add order (defined by ordername) -->
		<xsl:param name="ordername" /> <!-- Order parameter -->
		<xsl:param name="orderdesclist" /> <!-- Order elements -->
		<xsl:param name="orderlist" /> <!-- Order order (asc or desc) -->
		<xsl:param name="filtermanage" /> <!-- 'switch' if we have to switch onlyPending, 'copy' if we have to copy 
			onlyPending -->
		<xsl:param name="onlyPendingValue" /> <!-- Value of onlypending -->

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
		<!-- If no ordering was defined for this element, and if addorder is true, 
			create one more -->
		<xsl:choose>
			<xsl:when
				test="$addorder = 'true' and not($orderdesclist/order[text() = $ordername])">
				<xsl:value-of
					select="concat('orderelts=', $ordername, '&amp;order=true&amp;')" />
			</xsl:when>
		</xsl:choose>
		<!-- Manage onlyPending -->
		<xsl:value-of select="'onlyPending='" />
		<xsl:choose>
			<xsl:when
				test="($filtermanage = 'switch' and $onlyPendingValue = 'true') or ($filtermanage = 'copy' and $onlyPendingValue = 'false')">
				<xsl:value-of select="'false'" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="'true'" />
			</xsl:otherwise>
		</xsl:choose>
		<xsl:value-of select="'&amp;'" />
	</xsl:template>

</xsl:stylesheet>