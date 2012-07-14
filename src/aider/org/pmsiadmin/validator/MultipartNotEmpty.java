package aider.org.pmsiadmin.validator;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;


import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Crée une annotation {@link MultipartNotEmpty} pour valider qu'un élément
 * de fichier de Multipart est bien défini. L'annotation est une contrainte
 * validée par {@link MultipartNotEmptyValidator}
 * @author delabre
 *
 */
@Documented
@Constraint(validatedBy = MultipartNotEmptyValidator.class)
@Target( { METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MultipartNotEmpty {
 String message() default "Le fichier n'est pas d�fini";
 
 Class<?>[] groups() default {};
 
 Class<? extends Payload>[] payload() default {};
}