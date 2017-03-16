package spc.webos.persistence.jdbc.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于程序显示切换数据源
 * 
 * @author chenjs
 *
 */
public class SwitchDS implements AutoCloseable
{
	protected Logger log = LoggerFactory.getLogger(getClass());
	String ds;
	String oldDS;

	public SwitchDS(String ds)
	{
		this.ds = ds;
		oldDS = DynamicDataSource.current();
		DynamicDataSource.current(ds);
		log.info("DS routing:{}, old:{}", ds, oldDS);
	}

	@Override
	public void close()
	{
		DynamicDataSource.current(oldDS);
		log.info("DS remove:{}, old:{}", DynamicDataSource.current(), oldDS);
	}
}
