package spc.webos.service.common.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import spc.webos.model.JsPO;
import spc.webos.model.MenuPO;
import spc.webos.service.BaseService;
import spc.webos.service.common.ExtjsService;
import spc.webos.service.common.LoginService;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;
import spc.webos.util.tree.ExtTreeNode;
import spc.webos.util.tree.TreeNode;

@Service("extjsService")
public class ExtjsServiceImpl extends BaseService implements ExtjsService
{
	public ExtjsServiceImpl()
	{
		versionKey = "status.refresh.common.extjs";
	}

	public String js(String id)
	{
		JsPO po = js.get(id);
		if (po != null) return js(po);
		po = persistence.find(new JsPO(id));
		if (po == null) return StringX.EMPTY_STRING;
		if (config.isProduct()) js.put(id, po);
		return js(po);
	}

	protected String js(JsPO po)
	{
		StringBuilder js = new StringBuilder();
		js.append(StringX.null2emptystr(po.getJs()));
		js.append(StringX.null2emptystr(po.getJs1()));
		js.append(StringX.null2emptystr(po.getJs2()));
		js.append(StringX.null2emptystr(po.getJs3()));
		return js.toString();
	}

	public void refresh()
	{
		js.clear();
	}

	public TreeNode getExtTree(String sqlId)
	{
		return getExtTree(sqlId, null);
	}

	public TreeNode getExtTree(String sqlId, Map param)
	{
		List nodes = (List) persistence.execute(sqlId, param);
		int size = nodes == null ? 0 : nodes.size();
		ExtTreeNode root = new ExtTreeNode();
		root.createTree(nodes);
		log.info("all nodes:{}, tree child:{}", size,
				root.getChildren() == null ? 0 : root.getChildren().size());
		return root;
	}

	public void removeTrees(List treesId)
	{
		if (treesId == null || treesId.size() == 0) return;
		for (int i = 0; i < treesId.size(); i++)
			extTreeCache.remove((String) treesId.get(i));
	}

	public Map getTrees()
	{
		return extTreeCache;
		// Map trees = new HashMap();
		// if (extTreeCache == null) return trees;
		// Iterator keys = extTreeSql.keySet().iterator();
		// while (keys.hasNext())
		// {
		// String key = keys.next().toString();
		// trees.put(key, extTreeCache.get(key));
		// }
		// return trees;
	}

	public TreeNode getMenuTree(String parentId)
	{
		List<MenuPO> m = loginService.getMenu();
		TreeNode node = new TreeNode();
		node.createTree(m, (n) -> ((MenuPO) n).getMid().equalsIgnoreCase(parentId));
		return node;
	}

	public MenuPO getMenu(String id)
	{
		for (MenuPO m : loginService.getMenu())
			if (m.getMid().equalsIgnoreCase(id)) return m;
		return null;
	}

	public String getMenus()
	{
		List<MenuPO> m = new ArrayList<>();
		loginService.getMenu().forEach((p) -> {
			if (!"0".equals(p.getMmenu())) m.add(p);
		});
		TreeNode node = new TreeNode();
		node.createTree(m);
		Map<String, Object> root = tree2menu(node);
		Map<String, Object> rm = (Map<String, Object>) root.get("menu");
		return JsonUtil.gson(rm.get("items")).replace("\"##", "").replace("##\"", "").replace('~',
				'"');
	}

	Map<String, Object> tree2menu(TreeNode n)
	{
		MenuPO m = (MenuPO) n.getTreeNodeValue();

		Map<String, Object> menu = StringX
				.nullity(StringX.trim(StringX.null2emptystr(m.getConfig()))) ? new HashMap<>()
						: (Map<String, Object>) JsonUtil.gson2obj(m.getConfig());
		menu.put("text", m.getText());
		menu.put("mid", m.treeId());
		if (n.isLeaf())
		{ // 设置占位符##是为了在生成json字符串时，对js函数去掉前面的引号
			menu.put("handler",
					"##function(){openWin(this,~" + m.getWin() + "~,~" + m.getJs() + "~)}##");
			return menu;
		}
		Map<String, Object> mitems = new HashMap<>();
		List items = new ArrayList();
		for (TreeNode tn : n.getChildren())
		{
			m = (MenuPO) tn.getTreeNodeValue();
			if ("-".equals(m.getWin())) items.add(m.getJs());
			else items.add(tree2menu(tn));
		}
		mitems.put("items", items);
		menu.put("menu", mitems);
		return menu;
	}

	@Resource
	protected LoginService loginService;
	protected Map<String, TreeNode> extTreeCache = new ConcurrentHashMap<>();
	protected Map<String, JsPO> js = new ConcurrentHashMap<>();

	public void setExtTreeCache(Map<String, String> extTreeCache) throws Exception
	{
		this.extTreeCache = new ConcurrentHashMap<String, TreeNode>();
		for (String key : extTreeCache.keySet())
			this.extTreeCache.put(key, new ExtTreeNode()
					.createTree((List) persistence.execute(extTreeCache.get(key), null)));
	}
}
