package aider.org.pmsiadmin.validator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Crée une annotation {@link FinessListModelValid} pour valider qu'un élément
 * de fichier de Multipart est bien défini. L'annotation est une contrainte
 * validée par {@link MultipartNotEmptyValidator}
 * @author delabre
 *
 */
@Documented
@Constraint(validatedBy = FinessListModelValidator.class)
@Target( {TYPE})
@Retention(value = RUNTIME)
public @interface FinessListModelValid {
 String message() default "{aider.org.bio.biomanager.validator.IntegerValidator.message}";
 
 Class<?>[] groups() default {};
 
 Class<? extends Payload>[] payload() default {};
}