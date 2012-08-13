package aider.org.pmsiadmin.validator;

import java.lang.reflect.Field;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validation de l'annotation {@link IsIntegerTransformer}
 * @author delabre
 *
 */
public class IsIntegerTransformerValidator implements ConstraintValidator<IsIntegerTransformer, Object>{
	
	private String sourceName;
	
	private String destinationName;
	
	@Override
	 public void initialize(IsIntegerTransformer isIntegerTransformer) {
		sourceName = isIntegerTransformer.source();
		destinationName = isIntegerTransformer.destination();
	 }
	 
	 @Override
	 public boolean isValid(final Object value, ConstraintValidatorContext context) {
		 try {
			 // Récupération de la source et vérification du type (String)
			 Field sourceField = value.getClass().getDeclaredField(sourceName);
			 sourceField.setAccessible(true);
			 if (!(sourceField.get(value) instanceof String))
				 return false;
			 // Si le champ est nul, on renvoit true
			 if (sourceField.get(value) == null)
				 return true;
	
			 // Récupération de la destination et vérification du type (Integer)
			 Field destinationField = value.getClass().getDeclaredField(destinationName);
			 destinationField.setAccessible(true);
			 if (!(destinationField.get(value) instanceof Integer))
				 return false;
					 
			 // Transformation string => integer
			 Integer result = Integer.parseInt((String) sourceField.get(value));
			 if (result < 0)
				 return false;
			 
			 // Assignement du champ de destination
			 destinationField.set(value, result);
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
