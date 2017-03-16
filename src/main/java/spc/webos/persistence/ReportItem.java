package spc.webos.persistence;

import java.util.List;

public class ReportItem
{
	public String[] preFnodes; // 前置执行的函数数组, flow node名
	public String[] postFnodes; // 后置执行的函数数组, flow node名
	public List preScripts;
	public Script main;
	public List postScripts;
	public String[] dependence;
	public String rowIndex;
}
