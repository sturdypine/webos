package spc.webos.service.common;

import java.util.List;
import java.util.Map;

import spc.webos.model.MenuPO;
import spc.webos.util.tree.TreeNode;

public interface ExtjsService
{
	// 获取数据库配置的js文件
	String js(String id);
	
	MenuPO getMenu(String id);

	String getMenus();

	TreeNode getMenuTree(String parentId);

	TreeNode getExtTree(String sqlId);

	// 通过request获得Ext格式的json树数据
	TreeNode getExtTree(String sqlId, Map param);

	// 刷新树缓存里的信息
	void removeTrees(List treesId);

	// 获取系统所有的树结构
	Map getTrees();

	// 获取系统功能树结构信息
	// TreeNode getSysFns();
}
