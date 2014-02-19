<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" doctype-system="about:legacy-compat"
		encoding="UTF-8" indent="yes" />

	<xsl:template match="/">

		<html>

			<head>
				<title>NPIMSDRIVER</title>
				<link rel="stylesheet" href="../resources/css/import.css" />
			</head>

			<form action="singlersf" method="post" enctype="multipart/form-data"
				class="formpmsi">

				<div class="item">
					<div class="description">Mois : </div>
					<input class="inputpmsi" type="number" name="month">
						<xsl:attribute name="value">
							<xsl:value-of select="/rsfimport/monthValue/text()" />
						</xsl:attribute>
					</input>
					<xsl:if test="/rsfimport/errorsModel/entry/key[text() = 'monthValue']">
						<div class="error"><xsl:value-of select="/rsfimport/errorsModel/entry[key/text() = 'monthValue']/value/text()" /></div>
					</xsl:if>
				</div>

				<div class="item">
					<div class="description">Année : </div>
					<input class="inputpmsi" type="number" name="year">
						<xsl:attribute name="value">
							<xsl:value-of select="/rsfimport/yearValue/text()" />
						</xsl:attribute>
					</input>
					<xsl:if test="/rsfimport/errorsModel/entry/key[text() = 'yearValue']">
						<div class="error"><xsl:value-of select="/rsfimport/errorsModel/entry[key/text() = 'yearValue']/value/text()" /></div>
					</xsl:if>
				</div>
				<div class="item">
					<div class="description">Finess : </div>
					<input class="inputpmsi" type="number" name="finess">
						<xsl:attribute name="value">
							<xsl:value-of select="/rsfimport/finessValue/text()" />
						</xsl:attribute>
					</input>
					<xsl:if test="/rsfimport/errorsModel/entry/key[text() = 'finessValue']">
						<div class="error"><xsl:value-of select="/rsfimport/errorsModel/entry[key/text() = 'finessValue']/value/text()" /></div>
					</xsl:if>
				</div>
				<div class="item">
					<div class="description">Fichier RSF : </div>
					<input class="inputpmsi" type="file" name="rsf">
						<xsl:attribute name="value">
							<xsl:value-of select="/rsfimport/rsf/text()" />
						</xsl:attribute>
					</input>
					<xsl:if test="/rsfimport/errorsModel/entry/key[text() = 'rsf']">
						<div class="error"><xsl:value-of select="/rsfimport/errorsModel/entry[key/text() = 'rsf']/value/text()" /></div>
					</xsl:if>
				</div>
				<div class="submit">
					<input class="inputpmsi" type="submit" value="Televerser" />
				</div>
			</form>

		</html>
	</xsl:template>

</xsl:stylesheet>