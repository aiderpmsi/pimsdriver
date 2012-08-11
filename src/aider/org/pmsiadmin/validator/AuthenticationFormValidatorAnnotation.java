package aider.org.pmsiadmin.validator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Définition des annotations
 * @author delabre
 *
 */
@Documented
@Constraint(validatedBy = AuthenticationFormValidator.class)
@Target(value = {TYPE})
@Retention(value = RUNTIME)
public @interface AuthenticationFormValidatorAnnotation {
	
	String message() default "{aider.org.bio.biomanager.validator.AuthenticationFormValidator.message}";
	
	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
}
