package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

public class PmsiDtoSedna {

	private SednaConnection sednaConnection;
	
	public PmsiDtoSedna(SednaConnection sednaConnection) {
		this.sednaConnection = sednaConnection;
	}
	
	/**
	 * Récupère dans Sedna un nouveau numéro unique pour un compteur
	 * @throws DriverException 
	 * @throws DtoPmsiException 
	 */
	public String getNewPmsiDocNumber(String docCounter) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("update \n" +
				"replace $l in fn:doc(\"" + docCounter + "\", \"Pmsi\")/indice \n" +
				"with <indice>{$l/text() + 1}</indice>");

		st = sednaConnection.createStatement();
		st.execute("fn:doc(\"PmsiDocIndice\", \"Pmsi\")/indice/text()");
		SednaSerializedResult pr = st.getSerializedResult();

		return pr.next();
	}

	/**
	 * @throws DriverException 
	 * Récupère dans sedna l'heure de la db
	 * @throws  
	 */
	public String getSednaTime() throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("current-dateTime()");
		SednaSerializedResult pr = st.getSerializedResult();
		return pr.next();
	}

	/**
	 * Retourne le rapport d'insertion
	 * @return
	 * @throws DriverException 
	 * @throws PmsiDtoException
	 */
	public String getReport(String docName, String collectionName) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute("(for $i in fn:doc(\"" + docName + "\", \"" + collectionName + "\")/(*[1])/(*[1])\n" +
			"return if (name($i/..) = \"RSF2012\" or (name($i/..) = \"RSF2009\"))\n" +
			"then\n" +
			"<result>\n" +
			"  <parent>\n" +
			"    <doc type = \"{name($i/..)}\"\n" +
			"         headertype = \"{name($i)}\"\n" +
			"         finess = \"{string($i/@Finess)}\"\n" +
			"         insertion = \"{string($i/../@insertionTimeStamp)}\"\n" +
			"         finperiode = \"{string($i/@DateFin)}\"/>\n" +
			"  </parent> {\n" +
			"  let $items:=fn:collection(\"Pmsi\")/\n" +
			"    (RSF2012 | RSF2009)[string(@insertionTimeStamp) = string($i/../@insertionTimeStamp)]/\n" +
			"      RsfHeader[. != $i and string(@Finess) = string($i/@Finess)]\n" +
			"      return <identityerrors count=\"{count($items)}\">{\n" +
			"        for $l in $items\n" +
			"        return\n" +
			"          <doc type = \"{name($l/..)}\" headertype = \"{name($l)}\" finess = \"{string($l/@Finess)}\" insertion = \"{string($l/../@insertionTimeStamp)}\"/>\n" +
			"      }</identityerrors> \n" +
			"} {\n" +
			"  let $rsfcontent:=$i/(RsfA | RsfA/(RsfB | RsfC | RsfH | RsfM))[@Finess != $i/@Finess]\n" +
			"  return <finesserrors count=\"{count($rsfcontent)}\"> {\n" +
			"    for $rsf in $rsfcontent \n" +
			"    return\n" +
			"    <rsf type=\"{name($rsf)}\" numfacture=\"{$rsf/@NumFacture}\"/>\n" +
			"  }\n" +
			"  </finesserrors> \n" +
			"} {\n" +
			"  let $rsfchildren:=$i/RsfA/(RsfB | RsfC | RsfH | RsfM)[@NumFacture != ../@NumFacture]\n" +
			"  return <numfactureerrors count=\"{count($rsfchildren)}\"> {\n" +
			"    for $rsf in $rsfchildren\n" +
			"    return\n" +
			"    <rsf type=\"{name($rsf)}\" numfacture=\"{$rsf/@NumFacture}\"/>\n" +
			"  }\n" +
			"  </numfactureerrors>\n" +
			"}\n" +
			"</result>\n" +
			"else\n" +
			"<toimplement/>)[1]");

		SednaSerializedResult pr = st.getSerializedResult();
		return pr.next();
	}

}
