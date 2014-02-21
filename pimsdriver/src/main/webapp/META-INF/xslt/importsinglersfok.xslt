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

			<div class="message">
				<div class="content">
					Upload réussi. Il faut maintenant demander son
					traitement.
				</div>
				<a class="okmessage" href="../welcome/main">
					<div>Valider</div>
				</a>
			</div>
		</html>
	</xsl:template>

</xsl:stylesheet>