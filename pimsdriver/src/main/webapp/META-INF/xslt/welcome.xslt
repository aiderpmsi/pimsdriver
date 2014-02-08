<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="html" doctype-system="about:legacy-compat"
		encoding="UTF-8" indent="yes" />

	<xsl:template match="/">

		<html>

			<head>
				<title>NPIMSDRIVER</title>
				<link rel="stylesheet" href="../resources/css/welcome.css" />
			</head>

			<nav class="iconswall">
				<a class="icon" href="/">
					<div class="iconimport iconimage" />
					<div class="icontext">import</div>
				</a>
				<a class="icon" href="/">
					<div class="iconprocess iconimage" />
					<div class="icontext">process</div>
				</a>
			</nav>

		</html>
	</xsl:template>

</xsl:stylesheet>