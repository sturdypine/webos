package spc.webos.service.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class OptionMap extends HashMap
{
	private static final long serialVersionUID = 1L;

	public OptionMap()
	{
		super(0);
	}

	public int size()
	{
		return this.keys == null ? 0 : this.keys.size();
	}

	public Object get(Object key)
	{
		int index = keys.indexOf(key);
		if (index < 0) return null;
		return this.values.get(index);
	}

	public Object put(Object key, Object value)
	{
		int index = keys.indexOf(key);
		if (index < 0)
		{
			this.values.add(value);
			this.keys.add(key);
		}
		else this.values.set(index, value);
		return value;
	}

	public Set keySet()
	{
		return keysSet;
	}

	public Collection values()
	{
		return valuesSet;
	}

	public Set entrySet()
	{
		return valuesSet;
	}

	public Object clone()
	{
		OptionMap m = new OptionMap();
		m.keys = (ArrayList) (keys.clone());
		m.values = (ArrayList) values.clone();
		m.keysSet = keysSet;
		m.valuesSet = valuesSet;
		return m;
	}

	public String toString()
	{
		return values.toString();
	}

	ArrayList keys = new ArrayList();
	ArrayList values = new ArrayList();
	HashSet keysSet = new OptionsSet(keys);
	HashSet valuesSet = new OptionsSet(values);

	class OptionsSet extends HashSet
	{
		private static final long serialVersionUID = 1L;

		public OptionsSet(List l)
		{
			super(0);
			this.l = l;
		}

		public Iterator iterator()
		{
			return l.iterator();
		}

		public int size()
		{
			return l.size();
		}

		List l;
	}
}
