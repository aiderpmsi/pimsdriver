package aider.org.pmsiadmin.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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
import aider.org.pmsiamin.model.ldap.Session;

@Controller
@RequestMapping("/Pmsi/Insert/Form")
public class InsertionPmi {
	
	@Resource(name="configuration")
	Configuration configuation = null;
	
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

		InsertionPmsiForm labosListForm = new InsertionPmsiForm();

		//command object
		model.addAttribute("laboslistform", labosListForm);
 
		//return form view
		return "LabosListForm";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String processSubmit(
		@Valid @ModelAttribute("laboslistform") InsertionPmsiForm labosListForm,
		BindingResult result,
		SessionStatus status,
		ModelMap model) throws ClassNotFoundException, SQLException, IOException {
 
		if (result.getFieldError("session") != null)
			return "redirect:/Authentification/Form";
		else if (result.hasErrors()) {
			//if validator failed
			return "LabosListForm";
		} else {
			status.setComplete();
			//form success
			
			BioManagerConnection connection = null;
			List<Labo_LabosList> laboslist = null;
			
			try {
				connection = new BioManagerConnection(bioManagerDataSource.getConnection());
			
				laboslist = connection.getDTOLabosList().getListByName(labosListForm.getLabo());
			
				connection.commit();
				
				model.addAttribute("user", labosListForm.getSession());
				model.addAttribute("laboslist", laboslist);
				model.addAttribute("encoding", bioManagerConfig.getEncoding());
			} finally {
				if (connection != null) {
					connection.close();
				}
			}
						
			return "LabosList";
		} 
	}
	
}
