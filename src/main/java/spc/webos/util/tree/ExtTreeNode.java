package spc.webos.util.tree;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import spc.webos.util.StringX;

public class ExtTreeNode extends TreeNode
{
	private static final long serialVersionUID = 1L;
	Boolean checked;

	public ExtTreeNode()
	{
	}

	public ExtTreeNode(String id, String text, Boolean checked)
	{
		leaf = true;
		this.id = id;
		this.text = text;
		this.checked = checked;
	}

	public void setTreeNodeValue(ITreeNodeValue value)
	{
		Map node = ((MapNodeValue) value).value;
		attributes = new HashMap();
		Iterator iter = node.keySet().iterator();
		while (iter.hasNext())
		{
			String key = (String) iter.next();
			String v = node.get(key).toString();
			if (v == null || v.length() == 0 || key.equalsIgnoreCase("parentid")) continue;
			if (key.equalsIgnoreCase("id")) id = v;
			else if (key.equalsIgnoreCase("text")) text = v;
			else if (key.equalsIgnoreCase("checked"))
				checked = v.equals("1") ? Boolean.TRUE : Boolean.FALSE;
			else attributes.put(key.toLowerCase(), v);
		}
	}

	/*
	 * public static void main(String[] args) { List list = new ArrayList(); Map
	 * node = null;
	 * 
	 * node = new HashMap(); node.put("id", "0201"); node.put("parentid", "02");
	 * node.put("text", "f2-1"); node.put("atrr1", "attr1"); list.add(node);
	 * 
	 * node = new HashMap(); node.put("id", "0101"); node.put("parentid", "01");
	 * node.put("text", "f1-1"); node.put("atrr1", "attr1"); list.add(node);
	 * 
	 * node = new HashMap(); node.put("id", "00"); node.put("parentid", "");
	 * node.put("text", "root"); node.put("atrr1", "attr1"); node.put("atrr2",
	 * "attr2"); list.add(node);
	 * 
	 * node = new HashMap(); node.put("id", "01"); node.put("parentid", "00");
	 * node.put("text", "f1"); node.put("atrr1", "attr1"); node.put("atrr2",
	 * "attr2"); list.add(node);
	 * 
	 * node = new HashMap(); node.put("id", "02"); node.put("parentid", "00");
	 * node.put("text", "f2"); node.put("atrr1", "attr1"); list.add(node);
	 * 
	 * ExtTreeNode root = new ExtTreeNode(); root.createTree(list);
	 * System.out.print(root.getMessage("0201", null, "def", null)); }
	 */
	public ITreeNodeValue createNodeValue(Object nodeValue)
	{
		return new MapNodeValue((Map) nodeValue);
	}

	public TreeNode createTreeNode()
	{
		return new ExtTreeNode();
	}

	public Map toJson()
	{
		Map json = super.toJson();
		if (checked == null) return json;
		json.put("checked", checked);
		// buf.setLength(buf.length() - 1);
		// buf.append(",checked:");
		// buf.append(checked);
		// buf.append('}');
		return json;
	}

	public String toString()
	{
		return toJson().toString();
	}

	public static class MapNodeValue implements ITreeNodeValue
	{
		public Map value;

		public boolean treeRoot()
		{
			Object parentId = parentTreeId();
			return parentId == null || parentId.toString().trim().length() == 0;
		}

		public MapNodeValue(Map value)
		{
			this.value = value;
		}

		public Object treeId()
		{
			Object v = value.get("id");
			if (v == null) v = value.get("ID");
//			System.out.println("id:"+value);
			return v.toString();
		}

		public Object parentTreeId()
		{
			return StringX.null2emptystr(value.get("parentid"));
		}

		public String treeText()
		{
			return (String) value.get("text");
		}
	}
}
