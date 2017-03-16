package spc.webos.bean;

/**
 * 自我注入当前spring对象到服务中
 * 
 * @author spc
 * 
 */
public interface BeanSelfAware
{
	void self(Object proxyBean);

	Object self();
}
