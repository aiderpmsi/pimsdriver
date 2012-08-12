package aider.org.pmsiadmin.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Validator;

import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import aider.org.pmsiadmin.config.Configuration;

@RequestMapping("/Finess/{numFiness}/PmsiList")
public class PmsiList {
	
	@Resource(name="configuration")
	private Configuration configuration = null;
	
	@Resource(name="validator")
	private Validator validator;
	
	@RequestMapping(value="/{numIndex}", method = RequestMethod.GET)
	public ModelAndView initForm(
			@PathVariable String numIndex,
			HttpServletRequest request,
			ModelMap model) {
		return new ModelAndView("none");
	}
}
