package aider.org.pmsiadmin.controller;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
import aider.org.pmsiadmin.model.get.FinessListGetParamModel;
import aider.org.pmsiadmin.model.ldap.Session;
import aider.org.pmsiadmin.model.xml.DtoFinessList;
import aider.org.pmsiadmin.view.FinessListView;

@Controller
@RequestMapping("/FinessList")
public class FinessList extends UtilsController<FinessListGetParamModel> {
	
	@Resource(name="configuration")
	private Configuration configuration = null;
	
	@Resource(name="validator")
	private Validator validator;
	
	@RequestMapping(value="/{numIndex}", method = RequestMethod.GET)
	public ModelAndView initForm(
			@PathVariable String numIndex,
			HttpServletRequest request,
			ModelMap model) throws DriverException {
		
		// Validation de l'autorisation d'acéder à cette méthode Get avec :
		// numIndex, configuration et Session
		FinessListGetParamModel finessListGetParamModel = new FinessListGetParamModel(
				numIndex, (Session) request.getSession().getAttribute("session"));
		ModelAndView redirect = checkGetParams(finessListGetParamModel, validator);
		if (redirect != null)
			return redirect;
		
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
					finessListGetParamModel.getNumIndexI() * 10 + 1,
					(finessListGetParamModel.getNumIndexI() + 1) * 10));
			// Acquittement de la réception des données
			sednaConnector.commit();
			// Renvoi de la vue adaptée
			return new ModelAndView(new FinessListView(), "model", model);
		} finally {
			sednaConnector.close();
		}
	}

}
