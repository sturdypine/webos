package spc.webos.exception;

import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import spc.webos.constant.AppRetCode;

/**
 * 报文检查错误异常对象
 * 
 * @author spc
 * 
 */
public class ArgsErrException extends AppException
{
	private static final long serialVersionUID = 1L;

	public ArgsErrException(BindingResult errors)
	{
		super(AppRetCode.MSG_ERRS, null, errors);
	}

	public ArgsErrException(String code, Object[] args, BindingResult errors)
	{
		super(code, args, errors);
	}

	public BindingResult errors()
	{
		return (BindingResult) detail;
	}

	public String getErrDesc()
	{
		List<ObjectError> errors = errors().getAllErrors();
		StringBuilder buf = new StringBuilder();
		for (ObjectError err : errors)
		{
			if (err instanceof FieldError)
				buf.append("field:" + ((FieldError) err).getField() + ",");
			buf.append("code:" + err.getCode());
			buf.append(",text:" + err.getDefaultMessage());
			buf.append('\n');
		}
		return buf.toString();
	}
}
