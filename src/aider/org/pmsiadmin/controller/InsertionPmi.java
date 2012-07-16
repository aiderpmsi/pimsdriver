package aider.org.pmsiadmin.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;

import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.model.form.InsertionPmsiForm;
import aider.org.pmsiadmin.parser.PmsiParser;
import aider.org.pmsiadmin.parser.PmsiParser.FileType;
import aider.org.pmsiamin.model.ldap.Session;

@Controller
@RequestMapping("/Pmsi/Insert/Form")
public class InsertionPmi {
	
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
	public String processSubmit(
		@Valid @ModelAttribute("insertionpmsiform") InsertionPmsiForm insertionPmsiForm,
		BindingResult result,
		SessionStatus status,
		ModelMap model) throws Throwable {
 
		if (result.getFieldError("session") != null)
			return "redirect:/Authentification/Form";
		else if (result.hasErrors()) {
			//if validator failed
			return "InsertionPmsiForm";
		} else {
			status.setComplete();
			//form success
			
			PmsiParser parser = new PmsiParser();
			
			FileType ret = parser.parse(
					insertionPmsiForm.getFile().getInputStream(),
					configuration);
			
			if (ret == null) {
				model.addAttribute("ret", "Impossible d'insérer le fichier");
				model.addAttribute("val", parser.getPmsiErrors());
			} else {
				model.addAttribute("ret", "Insertion réussie");
				model.addAttribute("val", "Oui!");
			}
			return "InsertionPmsi";
		} 
	}
	
}
