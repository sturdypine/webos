package spc.webos.bean.propertyeditors;

import java.beans.PropertyEditorSupport;
import java.util.Hashtable;
import java.util.Map;

import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

public class JsonMapPropertyEditor extends PropertyEditorSupport
{
	protected boolean hashtable;

	public void setAsText(String text)
	{
		if (!StringX.nullity(text)) setValue(hashtable
				? new Hashtable<String, Object>((Map<String, Object>) JsonUtil.json2obj(text))
				: (Map<String, Object>) JsonUtil.json2obj(text));
	}

	public String getAsText()
	{
		Object v = getValue();
		return v == null ? null : JsonUtil.obj2json(v);
	}

	public void setHashtable(boolean hashtable)
	{
		this.hashtable = hashtable;
	}
}
