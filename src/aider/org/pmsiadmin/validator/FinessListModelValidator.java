package aider.org.pmsiadmin.validator;

import java.lang.reflect.Field;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import aider.org.pmsiadmin.model.get.FinessListGetParamModel;

/**
 * Validation de l'annotation {@link MultipartNotEmpty}
 * @author delabre
 *
 */
public class FinessListModelValidator implements ConstraintValidator<FinessListModelValid, FinessListGetParamModel>{
	
	@Override
	 public void initialize(FinessListModelValid finessListModelValid) {
	 }
	 
	 @Override
	 public boolean isValid(FinessListGetParamModel value, ConstraintValidatorContext context) {
		 if (value.getNumIndex() == null)
			 return false;
		 
		 try {
			 // Transformation string => integer
			 Integer result = Integer.parseInt(value.getNumIndex());
			 
			 // Transformation du value.integerI en champ accessible
			 Field integerIField = value.getClass().getDeclaredField("numIndexI");
			 integerIField.setAccessible(true);
			 
			 // Changement du champ privé
			 integerIField.set(value, result);
		 } catch (NumberFormatException e) {
			 return false;
		 } catch (SecurityException e) {
			 return false;
		 } catch (IllegalAccessException e) {
			 return false;
		 } catch (NoSuchFieldException e) {
			 return false;
		 }
		 return true;
	 }

}
