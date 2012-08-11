package aider.org.pmsiadmin.controller;

import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import ru.ispras.sedna.driver.DriverException;

import aider.org.pmsiadmin.config.Configuration;
import aider.org.pmsiadmin.connector.SednaConnector;
import aider.org.pmsiadmin.model.get.FinessListModel;
import aider.org.pmsiadmin.model.ldap.Session;
import aider.org.pmsiadmin.model.xml.DtoFinessList;
import aider.org.pmsiadmin.view.FinessListView;

@Controller
@RequestMapping("/FinessList")
public class FinessList {
	
	@Resource(name="configuration")
	Configuration configuration = null;
	
	@Resource(name="validator")
	private Validator validator;
	
	@RequestMapping(value="/{numIndex}", method = RequestMethod.GET)
	public ModelAndView initForm(
			@PathVariable String numIndex,
			HttpServletRequest request,
			ModelMap model) throws DriverException {
		
		// Validation de l'autorisation d'acéder à cette méthode Get avec :
		// numIndex, configuration et Session
		FinessListModel finessListModel = new FinessListModel(
				numIndex, (Session) request.getSession().getAttribute("session"));
		
		Set<ConstraintViolation<FinessListModel>> constraintViolations =
					validator.validate(finessListModel);
				
		if (constraintViolations.size() > 0 ) {
			String violations = "";
			for (ConstraintViolation<FinessListModel> contraintes : constraintViolations) {
				if (contraintes.getPropertyPath().toString().equals("session"))
					return new ModelAndView("redirect:/Authentification/Form");
					violations += contraintes.getRootBeanClass().getSimpleName() + "." +
							contraintes.getPropertyPath() + " " + contraintes.getMessage() + "\n";
			}
			throw new ValidationException("Erreurs de validation : " + violations);
		}
		
		// Les données sont validées, on peut renvoyer ce que le client demande
		// 1 - Connexion à Sedna
		SednaConnector sednaConnector = new SednaConnector();
		try {
			sednaConnector.open(configuration);
			sednaConnector.begin();
			// 2 - Récupération de l'objet de transfert de données
			DtoFinessList dtoFinessList = sednaConnector.getDtoFinessList();
			// 3 - Récupération de la liste de finess demandés
			model.addAttribute("finess", dtoFinessList.getFinessList(
					finessListModel.getNumIndexI() * 10 + 1,
					(finessListModel.getNumIndexI() + 1) * 10));
			// Acquittement de la réception des données
			sednaConnector.commit();
			// Renvoi de la vue adaptée
			return new ModelAndView(new FinessListView(), "model", model);
		} finally {
			sednaConnector.close();
		}
	}

}
