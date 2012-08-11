package aider.org.pmsiadmin.controller;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.stream.XMLStreamException;
import javax.xml.xpath.XPathExpressionException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.ModelAndView;
import ru.ispras.sedna.driver.DriverException;

import aider.org.machinestate.MachineStateException;
import aider.org.pmsi.exceptions.PmsiParserException;
import aider.org.pmsi.exceptions.PmsiWriterException;
import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.connector.SednaConnector;
import aider.org.pmsiadmin.model.form.InsertionPmsiForm;
import aider.org.pmsiadmin.model.ldap.Session;
import aider.org.pmsiadmin.model.xml.PmsiDtoSedna;
import aider.org.pmsiadmin.model.xml.PmsiDtoSedna.StoreResult;
import aider.org.pmsiadmin.model.xml.XmlReport;
import aider.org.pmsiadmin.view.InsertionPmsiView;


@Controller
@RequestMapping("/Pmsi/Insert/Form")
public class InsertionPmsi {
	
	@Resource(name="configuration")
	Configuration configuration = null;
	
	@InitBinder
	public void initBinder(HttpServletRequest request, WebDataBinder binder) throws SQLException {
		// On recherche l'objet InsertionPmsiForm
		if (binder.getTarget() != null &&
			binder.getTarget() instanceof InsertionPmsiForm) {

			// On associe la session avec le bean du formulaire (côté serveur)
			InsertionPmsiForm insertionPmsiForm = (InsertionPmsiForm) binder.getTarget();
			
			insertionPmsiForm.setSession((Session) request.getSession().getAttribute("session"));
		}
    }
	
	
	@RequestMapping(method = RequestMethod.GET)
	public String initForm(ModelMap model) throws ClassNotFoundException, SQLException, IOException{

		InsertionPmsiForm insertionPmsiForm = new InsertionPmsiForm();

		//command object
		model.addAttribute("insertionpmsiform", insertionPmsiForm);
 
		//return form view
		return "InsertionPmsiForm";
	}

	@RequestMapping(method = RequestMethod.POST)
	public ModelAndView processSubmit(
		@Valid @ModelAttribute("insertionpmsiform") InsertionPmsiForm insertionPmsiForm,
		BindingResult result,
		SessionStatus status,
		ModelMap model) throws UnsupportedEncodingException, IOException, DriverException, PmsiWriterException, MachineStateException, XMLStreamException, XPathExpressionException {
 
		if (result.getFieldError("session") != null)
			return new ModelAndView("redirect:/Authentification/Form");
		else if (result.hasErrors()) {
			//if validator failed
			return new ModelAndView("InsertionPmsiForm");
		} else {
			status.setComplete();
			//form success
			
			// TODO :
			// Dans un premier temps, on utilise une copie du fichier en mémoire, mais il faudrait :
			// 1 - Le sérialiser dans un container par exemple Derby pour le lire et le relire si
			//     nécessaire

			Reader re = new BufferedReader(
					new InputStreamReader(insertionPmsiForm.getFile().getInputStream(), "ISO-8859-1"));

			char[] target = new char[512];
			int nbread;
			
			StringBuilder stringBuilder = new StringBuilder();
			
			while ((nbread = re.read(target)) != -1) {
				stringBuilder.append(target, 0, nbread);
			}
			
			Reader re2 = new CharArrayReader(stringBuilder.toString().toCharArray());

			// Connexion à Sedna :
			SednaConnector sednaConnector;
			sednaConnector = new SednaConnector();
			// Résultat de stockage de pmsi
			StoreResult storeResult;
			// Résultat du report d'insertion
			XmlReport xmlReport = null;
			try {
				sednaConnector.open(configuration);
				PmsiDtoSedna pmsiDtoSedna = sednaConnector.getPmsiDto();
				
				sednaConnector.begin();
				String newDocName = "pmsi-" + pmsiDtoSedna.getNewPmsiDocNumber("PmsiDocIndice");
				sednaConnector.commit();				
				
				sednaConnector.begin();
				storeResult = pmsiDtoSedna.storePmsi(re2,
						newDocName,
						"Pmsi",
						pmsiDtoSedna.getSednaTime());
				
				if (storeResult.stateSuccess) {
					xmlReport = new XmlReport(pmsiDtoSedna.getReport("pmsi-" + pmsiDtoSedna.getPmsiDocNumber("PmsiDocIndice"), "Pmsi"));
					if (xmlReport.getCountFinessErrors() != 0 ||
							xmlReport.getCountIdentityErrors() !=0 ||
							xmlReport.getCountNumFactureErrors() != 0) {
						storeResult.stateSuccess = false;
						storeResult.parseErrors.add(new PmsiParserException("Erreurs de contenu du fichier Pmsi"));
						sednaConnector.commit();
					}
					else
						sednaConnector.commit();
				}
				
				model.addAttribute("status", storeResult.stateSuccess);
				model.addAttribute("parserreport", storeResult.parseErrors);
				model.addAttribute("xmlreport", xmlReport);
				
			} finally {
				sednaConnector.close();
			}

			return new ModelAndView(new InsertionPmsiView(), "model", model);
		}
	} 
	
}
