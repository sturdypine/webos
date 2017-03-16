package spc.webos.util.tree;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;

import spc.webos.util.POJOUtil;

public class TreeNode implements MessageSource, Serializable
{
	private static final long serialVersionUID = 1L;
	Object id;
	String text;
	boolean leaf = true;
	List<TreeNode> children;
	ITreeNodeValue treeNodeValue;
	Map attributes;
	transient TreeNode parent; // 父亲节点
	transient protected Logger log = LoggerFactory.getLogger(getClass());

	public Map toJson()
	{
		Map json = new HashMap();
		json.put("text", text == null ? "undefined" : text);
		json.put("leaf", leaf);
		POJOUtil.pojo2map(treeNodeValue, json);
		if (attributes != null) json.putAll(attributes);
		// exttree 不能提供id
		json.remove("id");
		// if (id != null)
		// {
		// buf.append("',id:'");
		// buf.append(id);
		// }
		// if (attributes != null && attributes.size() > 0)
		// {
		// // buf.append(",attributes:{"); // modified by spc 2010-06-07
		// buf.append(",");
		// boolean first = true;
		// Iterator keys = attributes.keySet().iterator();
		// while (keys.hasNext())
		// {
		// String key = keys.next().toString();
		// Object v = attributes.get(key);
		// if (v == null) continue;
		// if (!first) buf.append(',');
		// buf.append(key);
		// buf.append(':');
		// if (!(v instanceof Number || v instanceof Boolean)) buf.append('\'');
		// buf.append(v.toString());
		// if (!(v instanceof Number || v instanceof Boolean)) buf.append('\'');
		// first = false;
		// }
		// // buf.append('}');
		// }
		if (children != null && this.children.size() > 0)
		{
			// buf.append(",children:[");
			List child = new ArrayList();
			for (int i = 0; i < children.size(); i++)
			{
				TreeNode node = children.get(i);
				// if (i > 0) buf.append(',');
				// buf.append(node.toJson());
				child.add(node.toJson());
			}
			json.put("children", child);
			// buf.append(']');
		}
		// buf.append('}');
		return json;
	}

	public void setTreeNodeValue(ITreeNodeValue n)
	{
		this.id = n.treeId();
		this.text = n.treeText();
		this.treeNodeValue = n;
	}

	public String getPath()
	{
		String ppath = null;
		if (parent != null) ppath = parent.getPath();
		return ppath == null ? id.toString() : ppath + '/' + id;
	}

	public TreeNode findByPath(String path)
	{
		path = path.trim();
		if (path.length() == 0) return this;
		int index = path.indexOf('/');
		if (index == 0) path = path.substring(1);
		index = path.indexOf('/');
		String id = index < 0 ? path : path.substring(0, index);

		if (children == null || children.size() == 0) return null;
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode node = children.get(i);
			if (node.id.equals(id))
				return index < 0 ? node : node.findByPath(path.substring(index + 1));
		}
		return null;
	}

	public TreeNode find(Object id)
	{
		if (id == null) return null;
		if (id.equals(this.id)) return this;
		if (children == null || children.size() == 0) return null;
		// 700 2013-09-09 在查找节点时使用广度优先，因为schema表新增.MD规范。使用深度优先导致问题
		// for (int i = children.size() -1; i >=0 ; i--)
		// {
		// TreeNode target = (TreeNode) children.get(i);
		// if (target.id.equals(id)) return target;
		// }

		for (int i = children.size() - 1; i >= 0; i--)
		{
			TreeNode target = ((TreeNode) children.get(i)).find(id);
			if (target != null) return target;
		}
		return null;
	}

	public TreeNode findChildByTextIgnoreCase(String text)
	{
		if (text.equalsIgnoreCase(text)) return this;
		if (children == null || children.size() == 0) return null;
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode target = ((TreeNode) children.get(i)).findChildByTextIgnoreCase(text);
			if (target != null) return target;
		}
		return null;
	}

	public TreeNode findChildByText(String text)
	{
		if (this.text != null && this.text.equals(text)) return this;
		if (children == null || children.size() == 0) return null;
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode target = ((TreeNode) children.get(i)).findChildByText(text);
			if (target != null) return target;
		}
		return null;
	}

	public TreeNode find(ITreeNodeMatcher matcher)
	{
		if (matcher.match(this)) return this;
		if (children == null || children.size() == 0) return null;
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode target = ((TreeNode) children.get(i)).find(matcher);
			if (target != null) return target;
		}
		return null;
	}

	public List findAll(ITreeNodeMatcher matcher)
	{
		List matches = new ArrayList();
		if (matcher.match(this)) matches.add(this);
		if (children == null || children.size() == 0) return matches;
		for (int i = 0; i < children.size(); i++)
			matches.addAll(((TreeNode) children.get(i)).findAll(matcher));
		return matches;
	}

	public void insertChild(TreeNode node)
	{
		this.leaf = false;
		if (children == null) children = new ArrayList();
		children.add(node);
		node.parent = this;
	}

	public boolean dfsTraverse(ITreeNodeVistor vistor)
	{
		if (children == null || children.size() == 0) return true;
		for (int i = 0; i < children.size(); i++)
		{
			if (!vistor.start((TreeNode) children.get(i), this, i)) return false;
			if (!((TreeNode) children.get(i)).dfsTraverse(vistor)) return false;
			if (!vistor.end((TreeNode) children.get(i), this, i)) return false;
		}
		return true;
	}

	public boolean bfsTraverse(ITreeNodeVistor vistor)
	{
		if (children == null || children.size() == 0) return true;
		for (int i = 0; i < children.size(); i++)
		{
			if (!vistor.start((TreeNode) children.get(i), this, i)) return false;
			if (!vistor.end((TreeNode) children.get(i), this, i)) return false;
		}
		for (int i = 0; i < children.size(); i++)
			if (!((TreeNode) children.get(i)).bfsTraverse(vistor)) return false;
		return true;
	}

	public TreeNode createTree(List list)
	{
		return createTree(list, null, (n) -> n.treeRoot());
	}

	// 用数据集合创建树
	public TreeNode createTree(List list, ITreeCreator creator)
	{
		return createTree(list, creator, (n) -> n.treeRoot());
	}

	public TreeNode createTree(List list, Function<ITreeNodeValue, Boolean> root)
	{
		return createTree(list, null, root);
	}

	public TreeNode createTree(List list, ITreeCreator creator,
			Function<ITreeNodeValue, Boolean> root)
	{
		if (list == null) return this;
		for (int i = 0; i < list.size(); i++)
		{
			ITreeNodeValue nodeValue = createNodeValue(list.get(i));
//			log.info("treenode:{},{}:{}", nodeValue.treeId(), nodeValue.parentTreeId(),
//					root.apply(nodeValue));
//			System.out.println("root:"+root.apply(nodeValue)+","+nodeValue.treeId());
			if (root.apply(nodeValue)) // if (nodeValue.isRoot())
			{ // 此节点为根节点
				setTreeNodeValue(nodeValue);
				list.remove(i);
				break;
			}
		}
		createTree(list, this, creator);
		return this;
	}

	// 创建文件目录结构的树
	public TreeNode createTree(File file, String path)
	{
		this.text = file.getName();
		this.attributes = new HashMap();
		attributes.put("path", path);
		if (!file.isDirectory())
		{ // 如果是文件，不是文件夹
			this.leaf = true;
			return this;
		}
		this.leaf = false;
		this.children = new ArrayList();
		File[] subFiles = file.listFiles();
		for (int i = 0; subFiles != null && i < subFiles.length; i++)
		{
			TreeNode node = new TreeNode();
			node.createTree(subFiles[i], path + '/' + this.text);
			children.add(node);
		}
		return this;
	}

	protected int createTree(List list, TreeNode root, ITreeCreator creator)
	{
		if (list == null || list.size() == 0) return 0;
		// System.out.println(list);
		List left = new ArrayList(); // 剩余没有建立进树结构的数据集合
		for (int i = 0; i < list.size(); i++)
		{
			Object v = list.get(i);
			ITreeNodeValue nodeValue = null;
			if (v instanceof ITreeNodeValue) nodeValue = (ITreeNodeValue) v;
			else nodeValue = createNodeValue(v);
			TreeNode parentNode = root.find(nodeValue.parentTreeId());
//			System.out.println("pid:"+nodeValue.parentTreeId()+","+parentNode+","+root.getId());
			if (parentNode != null)
			{
				TreeNode n = createTreeNode();
				n.setTreeNodeValue(nodeValue);
				if (creator == null) parentNode.insertChild(n);
				else creator.insertChild(parentNode, n);
			}
			else left.add(nodeValue);
		}
		if (left.isEmpty()) return 0;
		if (left.size() != list.size()) return createTree(left, root, creator);
		return left.size();
	}

	public ITreeNodeValue createNodeValue(Object nodeValue)
	{
		return (ITreeNodeValue) nodeValue;
	}

	public TreeNode createTreeNode()
	{
		return new TreeNode();
	}

	public TreeNode find(List trees, Object id)
	{
		if (trees == null || trees.size() == 0) return null;
		for (int i = 0; i < trees.size(); i++)
		{
			TreeNode n = ((TreeNode) trees.get(i)).find(id);
			if (n != null) return n;
		}
		return null;
	}

	public static void leafs(TreeNode node, List leafs)
	{
		if (node.isLeaf()) leafs.add(node);
		if (node.children == null || node.children.size() == 0) return;
		for (int i = 0; i < node.children.size(); i++)
			leafs((TreeNode) node.children.get(i), leafs);
	}

	public Object getId()
	{
		return id;
	}

	public void setId(Object id)
	{
		this.id = id;
	}

	public boolean isLeaf()
	{
		return leaf;
	}

	public void setLeaf(boolean leaf)
	{
		this.leaf = leaf;
	}

	public List<TreeNode> getChildren()
	{
		return children;
	}

	public void setChildren(List<TreeNode> children)
	{
		this.children = children;
	}

	public String getMessage(MessageSourceResolvable arg0, Locale arg1)
			throws NoSuchMessageException
	{
		return null;
	}

	public String getMessage(String arg0, Object[] arg1, Locale arg2) throws NoSuchMessageException
	{
		return getMessage(arg0, arg1, null, arg2);
	}

	public String getMessage(String arg0, Object[] arg1, String arg2, Locale arg3)
	{
		if (id.equals(arg0)) return getText();
		if (children == null || children.size() == 0) return arg2;
		for (int i = 0; i < children.size(); i++)
		{
			TreeNode treeNode = (TreeNode) children.get(i);
			// 树的节点必须有规则, 即父节点id必须是孩子节点id的开头
			if (arg0.startsWith((String) treeNode.id))
				return treeNode.getMessage(arg0, arg1, arg2, arg3);
		}
		return arg2;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public ITreeNodeValue getTreeNodeValue()
	{
		return treeNodeValue;
	}

	public Map getAttributes()
	{
		return attributes;
	}

	public void setAttributes(Map attributes)
	{
		this.attributes = attributes;
	}

	public String toString()
	{
		return toJson().toString();
	}
}
