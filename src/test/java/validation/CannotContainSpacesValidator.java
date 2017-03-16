package validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CannotContainSpacesValidator implements ConstraintValidator<CannotContainSpaces, String> {
	private CannotContainSpaces ccs;

	/**
	 * 初始参数,获取注解中length的值
	 */
	@Override
	public void initialize(CannotContainSpaces ccs) {
		this.ccs = ccs;
	}

	@Override
	public boolean isValid(String str, ConstraintValidatorContext constraintValidatorContext) {
		if (str != null && str.indexOf(" ") > 0) {
			constraintValidatorContext.disableDefaultConstraintViolation();// 禁用默认的message的值
			// 重新添加错误提示语句
			constraintValidatorContext.buildConstraintViolationWithTemplate("字符串不能包含空格").addConstraintViolation();
			return false;
		}
		return true;
	}
}
