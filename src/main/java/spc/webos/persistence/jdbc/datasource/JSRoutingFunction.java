package spc.webos.persistence.jdbc.datasource;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JSRoutingFunction implements RoutingFunction
{
	protected ScriptEngine engine = new ScriptEngineManager().getEngineByName("js");

	public JSRoutingFunction()
	{
	}

	public JSRoutingFunction(String js) throws Exception
	{
		setJs(js);
	}

	public void setJs(String js) throws Exception
	{
		engine.eval("function rule(column){" + js + "}");
	}

	@Override
	public String routing(Object column) throws Exception
	{
		return (String) ((Invocable) engine).invokeFunction("rule", column); // engine支持多线程
	}
}
