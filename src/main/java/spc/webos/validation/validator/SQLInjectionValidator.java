package spc.webos.validation.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import spc.webos.persistence.IPersistence;
import spc.webos.validation.SQLInjection;

public class SQLInjectionValidator implements ConstraintValidator<SQLInjection, String>
{
	private SQLInjection injection;

	@Override
	public void initialize(SQLInjection injection)
	{
		this.injection = injection;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext cxt)
	{
		if (value != null && IPersistence.SQL_INJECTION.matcher(value.toLowerCase()).find())
		{
			cxt.disableDefaultConstraintViolation();
			cxt.buildConstraintViolationWithTemplate(injection.message() + ":" + value)
					.addConstraintViolation();
			return false;
		}
		return true;
	}
}
