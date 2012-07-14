package aider.org.pmsiadmin.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.web.multipart.MultipartFile;

/**
 * Validation de l'annotation {@link MultipartNotEmpty}
 * @author delabre
 *
 */
public class MultipartNotEmptyValidator implements ConstraintValidator<MultipartNotEmpty, MultipartFile>{
	
	@Override
	 public void initialize(MultipartNotEmpty multipartnotempty) {
	 }
	 
	 @Override
	 public boolean isValid(MultipartFile value, ConstraintValidatorContext context) {
		 if (value == null || value.isEmpty())
			 return false;
		 else
			 return true;
	 }

}
