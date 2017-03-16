package spc.webos.bean.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.List;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

public class JsonListPropertyEditor extends PropertyEditorSupport
{
	public void setAsText(String text)
	{
		if (!StringX.nullity(text)) setValue((List) JsonUtil.json2obj(text));
	}

	public String getAsText()
	{
		Object v = getValue();
		return v == null ? null : JsonUtil.obj2json(v);
	}
}
