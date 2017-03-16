package spc.webos.util.tree;

public interface ITreeNodeVistor
{
	// 如果返回false 表示不需要继续访问, 此方法可用来查找指定类型的元素
	boolean start(TreeNode treeNode, TreeNode parent, int index);
	
	// 如果返回false 表示不需要继续访问, 此方法可用来查找指定类型的元素
	boolean end(TreeNode treeNode, TreeNode parent, int index);
}
