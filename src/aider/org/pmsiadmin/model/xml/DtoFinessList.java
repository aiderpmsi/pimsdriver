package aider.org.pmsiadmin.model.xml;

import ru.ispras.sedna.driver.DriverException;
import ru.ispras.sedna.driver.SednaConnection;
import ru.ispras.sedna.driver.SednaSerializedResult;
import ru.ispras.sedna.driver.SednaStatement;

public class DtoFinessList {
	
	private SednaConnection sednaConnection;
	
	public DtoFinessList(SednaConnection sednaConnection) {
		this.sednaConnection = sednaConnection;
	}

	public String getFinessList(int first, int last) throws DriverException {
		SednaStatement st = sednaConnection.createStatement();
		st.execute(
				"for $i in (1)\n" +
				"return\n" +
				"<finessechantillon\n" +
				"  totalcount=\"{count(distinct-values(fn:collection(\"Pmsi\")/*/*/@Finess))}\"\n" +
				"  first=\"" + Integer.toString(first) + "\"\n" +
				"  last=\"" + Integer.toString(last) + "\">\n" +
				"{\n" +
				"(for $i in distinct-values(fn:collection(\"Pmsi\")/*/*/@Finess)\n" +
				"let $o:=fn:doc(\"ListFiness\", \"Pmsi\")/listfiness/finess[@num = $i]\n" +
				"return if ($o)\n" +
				"then <finess num=\"{string($i)}\" name=\"{string($o)}\"/>\n" +
				"else <finess num=\"{string($i)}\"/>)[position() = " + Integer.toString(first) + " to " + Integer.toString(last) + "]\n" +
				"}</finessechantillon>");
		SednaSerializedResult pr = st.getSerializedResult();
		return pr.next();
	}
	
}
