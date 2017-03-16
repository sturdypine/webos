package spc.webos.persistence.matrix.exp.inner;

import com.eteks.parser.Function;
import com.eteks.parser.Interpreter;
import com.eteks.parser.Syntax;

public class MatrixInterpreter implements Interpreter
{
	public static Object convert(Object obj)
	{
		if (!(obj instanceof Number)) return obj;
		if (obj instanceof Long || obj instanceof Double) return obj;
		if (obj instanceof Integer || obj instanceof Short) return new Long(obj
				.toString());
		return new Double(obj.toString());
	}

	public Object getLiteralValue(Object obj)
	{
		return convert(obj);
	}

	public Object getParameterValue(Object obj)
	{
		return convert(obj);
	}

	public Object getConstantValue(Object obj)
	{
		if (Syntax.CONSTANT_PI.equals(obj)) return PI_DOUBLE;
		if (Syntax.CONSTANT_E.equals(obj)) return E_DOUBLE;
		if (Syntax.CONSTANT_FALSE.equals(obj)) return FALSE_DOUBLE;
		if (Syntax.CONSTANT_TRUE.equals(obj)) return TRUE_DOUBLE;
		else throw new IllegalArgumentException("Constant key " + obj
				+ " not implemented");
	}

	public Object getUnaryOperatorValue(Object obj, Object obj1)
	{
		if (!(obj1 instanceof Number)) return obj1;

		if (obj.equals(Syntax.OPERATOR_OPPOSITE))
		{
			if (obj1 instanceof Long) return new Long(((Long) obj1).longValue()
					* -1);
			return new Double(((Double) obj1).doubleValue() * -1);
		}
		if (obj.equals(Syntax.OPERATOR_POSITIVE)) return obj1;
		if (obj.equals(Syntax.OPERATOR_LOGICAL_NOT))
		{
			if (!isTrue(obj1))
			{
				if (obj1 instanceof Long) return TRUE_LONG;
				return TRUE_DOUBLE;
			}
			if (obj1 instanceof Long) return FALSE_LONG;
			return FALSE_DOUBLE;
		}
		double d = ((Number) obj1).doubleValue();
		if (obj.equals(Syntax.OPERATOR_BITWISE_NOT))
		{
			if (Math.floor(d) != d) throw new IllegalArgumentException(
					"Operator operand not an integer");
			if (obj1 instanceof Double) return new Double(~((Number) obj1)
					.longValue());
			return new Long(~((Number) obj1).longValue());
		}
		else
		{
			throw new IllegalArgumentException("Unary operator key " + obj
					+ " not implemented");
		}
	}

	public Object getBinaryOperatorValue(Object obj, Object obj1, Object obj2)
	{
		if (!(obj1 instanceof Number) || !(obj2 instanceof Number))
		{
			if (obj.equals(Syntax.OPERATOR_ADD)) return obj1.toString()
					+ obj2.toString();
			return obj1;
		}
		double d = ((Number) obj1).doubleValue();
		double d1 = ((Number) obj2).doubleValue();
		double r = 0d;
		if (obj.equals(Syntax.OPERATOR_ADD)) r = d + d1;
		else if (obj.equals(Syntax.OPERATOR_SUBSTRACT)) r = d - d1;
		else if (obj.equals(Syntax.OPERATOR_MULTIPLY)) r = d * d1;
		else if (obj.equals(Syntax.OPERATOR_DIVIDE)) r = d / d1;
		else if (obj.equals(Syntax.OPERATOR_POWER)) r = Math.pow(d, d1);
		else if (obj.equals(Syntax.OPERATOR_MODULO))
		{
			double d2 = d - d1 * (double) (int) (d / d1);
			if (d < 0.0D && d1 > 0.0D || d > 0.0D && d1 < 0.0D) d2 += d1;
			r = d2;
		}
		else if (obj.equals(Syntax.OPERATOR_REMAINDER)) r = d % d1;
		else if (obj.equals(Syntax.OPERATOR_EQUAL)) r = d != d1 ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_DIFFERENT)) r = d == d1 ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_GREATER_OR_EQUAL)) r = d < d1 ? 0
				: 1;
		else if (obj.equals(Syntax.OPERATOR_LESS_OR_EQUAL)) r = d > d1 ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_GREATER)) r = d <= d1 ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_LESS)) r = d >= d1 ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_LOGICAL_OR)) r = !isTrue(obj1)
				&& !isTrue(obj2) ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_LOGICAL_AND)) r = !isTrue(obj1)
				|| !isTrue(obj2) ? 0 : 1;
		else if (obj.equals(Syntax.OPERATOR_LOGICAL_XOR)) r = (!isTrue(obj1) || isTrue(obj2))
				&& (isTrue(obj1) || !isTrue(obj2)) ? 0 : 1;
		else if (Math.floor(d) != d) throw new IllegalArgumentException(
				"Operand " + obj1 + " of bit operator not an integer");
		else if (Math.floor(d1) != d1) throw new IllegalArgumentException(
				"Operand " + obj2 + " of bit operator not an integer");
		else if (obj.equals(Syntax.OPERATOR_BITWISE_OR)) r = (long) d
				| (long) d1;
		else if (obj.equals(Syntax.OPERATOR_BITWISE_XOR)) r = (long) d
				^ (long) d1;
		else if (obj.equals(Syntax.OPERATOR_BITWISE_AND)) r = (long) d
				& (long) d1;
		else if (obj.equals(Syntax.OPERATOR_SHIFT_LEFT)) r = (long) d << (int) (long) d1;
		else if (obj.equals(Syntax.OPERATOR_SHIFT_RIGHT)) r = (long) d >> (int) (long) d1;
		else if (obj.equals(Syntax.OPERATOR_SHIFT_RIGHT_0)) r = (long) d >>> (int) (long) d1;
		else throw new IllegalArgumentException("Binary operator key " + obj
				+ " not implemented");
		if (!(obj1 instanceof Long) || !(obj2 instanceof Long)) return new Double(
				r);
		return new Long((long) r);
	}

	public Object getCommonFunctionValue(Object obj, Object obj1)
	{
		if (!(obj1 instanceof Number)) return obj1;
		double d = ((Number) obj1).doubleValue();
		if (obj.equals(Syntax.FUNCTION_LN)) return new Double(Math.log(d));
		if (obj.equals(Syntax.FUNCTION_LOG)) return new Double(Math.log(d)
				/ Math.log(10D));
		if (obj.equals(Syntax.FUNCTION_EXP)) return new Double(Math.exp(d));
		if (obj.equals(Syntax.FUNCTION_SQR)) return new Double(d * d);
		if (obj.equals(Syntax.FUNCTION_SQRT)) return new Double(Math.sqrt(d));
		if (obj.equals(Syntax.FUNCTION_COS)) return new Double(Math.cos(d));
		if (obj.equals(Syntax.FUNCTION_SIN)) return new Double(Math.sin(d));
		if (obj.equals(Syntax.FUNCTION_TAN)) return new Double(Math.tan(d));
		if (obj.equals(Syntax.FUNCTION_ACOS)) return new Double(Math.acos(d));
		if (obj.equals(Syntax.FUNCTION_ASIN)) return new Double(Math.asin(d));
		if (obj.equals(Syntax.FUNCTION_ATAN)) return new Double(Math.atan(d));
		if (obj.equals(Syntax.FUNCTION_COSH)) return new Double(
				(Math.exp(d) + Math.exp(-d)) / 2D);
		if (obj.equals(Syntax.FUNCTION_SINH)) return new Double(
				(Math.exp(d) - Math.exp(-d)) / 2D);
		if (obj.equals(Syntax.FUNCTION_TANH)) return new Double(
				(Math.exp(d) - Math.exp(-d)) / (Math.exp(d) + Math.exp(-d)));
		if (obj.equals(Syntax.FUNCTION_INTEGER)) return new Double(
				((Number) obj1).longValue());
		if (obj.equals(Syntax.FUNCTION_FLOOR)) return new Double(Math.floor(d));
		if (obj.equals(Syntax.FUNCTION_CEIL)) return new Double(Math.ceil(d));
		if (obj.equals(Syntax.FUNCTION_ROUND)) return new Double(Math.rint(d));
		if (obj.equals(Syntax.FUNCTION_ABS)) return new Double(Math.abs(d));
		if (obj.equals(Syntax.FUNCTION_OPPOSITE)) return new Double(-d);
		if (obj.equals(Syntax.FUNCTION_NOT)) return isTrue(obj1) ? FALSE_DOUBLE
				: TRUE_DOUBLE;
		else throw new IllegalArgumentException("Common function key " + obj
				+ " not implemented");
	}

	public Object getConditionValue(Object obj, Object obj1, Object obj2)
	{
		return isTrue(obj) ? obj1 : obj2;
	}

	public boolean isTrue(Object obj)
	{
		if (!(obj instanceof Number)) throw new IllegalArgumentException(
				"Condition " + obj + " not an instance of Number");
		else return ((Number) obj).doubleValue() != 0.0D;
	}

	public boolean supportsRecursiveCall()
	{
		return true;
	}

	public Object getFunctionValue(Function function, Object aobj[],
			boolean flag)
	{
		return function.computeFunction(this, aobj);
	}

	private MatrixInterpreter()
	{
	}

	public static MatrixInterpreter getInstance()
	{
		return inter;
	}

	static MatrixInterpreter inter = new MatrixInterpreter();

	public static final Long FALSE_LONG = new Long(0);
	public static final Long TRUE_LONG = new Long(1);
	public static final Double FALSE_DOUBLE = new Double(0.0D);
	public static final Double TRUE_DOUBLE = new Double(1.0D);
	private static final Double PI_DOUBLE = new Double(3.1415926535897931D);
	private static final Double E_DOUBLE = new Double(2.7182818284590451D);

}
