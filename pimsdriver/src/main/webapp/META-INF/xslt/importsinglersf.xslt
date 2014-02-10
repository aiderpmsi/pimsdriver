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
					<input class="inputpmsi" type="number" name="month"/>
					<xsl:if test="/rsfimport/errorsModel/entry/key/month">
						<div class="error"><xsl:value-of select="../value" /></div>
					</xsl:if>
				</div>
				<div class="item">
					<div class="description">Année : </div>
					<input class="inputpmsi" type="number" name="year"/>
				</div>
				<div class="item">
					<div class="description">Finess : </div>
					<input class="inputpmsi" type="number" name="finess"/>
				</div>
				<div class="item">
					<div class="description">Fichier RSF : </div>
					<input class="inputpmsi" type="file" name="rsf"/>
				</div>
				<div class="submit">
					<input class="inputpmsi" type="submit" value="Televerser" />
				</div>
			</form>

		</html>
	</xsl:template>

</xsl:stylesheet>