package spc.webos.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import spc.webos.validation.validator.SQLInjectionValidator;

@Constraint(validatedBy = SQLInjectionValidator.class)
@Target({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Documented
public @interface SQLInjection
{
	String message() default "SQLInjection";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
