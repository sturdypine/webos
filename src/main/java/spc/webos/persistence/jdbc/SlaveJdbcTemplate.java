package spc.webos.persistence.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;

import spc.webos.config.AppConfig;
import spc.webos.service.Status;
import spc.webos.util.StringX;

public class SlaveJdbcTemplate extends XJdbcTemplate implements Status
{
	protected String name;
	private String lastVersion = StringX.EMPTY_STRING;
	protected String versionKey; //

	protected List<XJdbcTemplate> jdbc;
	protected List<DataSource> ds;
	private volatile int startIdx = 0;

	public List query(String sqlID, String sql, String rowClazzName) throws DataAccessException
	{
		return forEach((jt) -> {
			return jt.query(sqlID, sql, rowClazzName);
		});
	}

	public <T> T forEach(Consumer<T> consumer)
	{
		if (startIdx >= jdbc.size()) startIdx = 0;
		int start = startIdx++;
		log.debug("slave ds start:{}/{}", start, jdbc.size());
		RuntimeException ex = null;
		for (int num = 0; num < jdbc.size(); num++, start++)
		{
			int idx = start % jdbc.size();
			try
			{
				return consumer.accept(jdbc.get(idx));
			}
			catch (DataAccessException dae)
			{
				throw dae;
			}
			catch (RuntimeException re)
			{
				ex = re;
				log.info("fail to select:" + idx, re);
			}
		}
		throw ex;
	}

	public void setDs(List<DataSource> ds)
	{
		this.ds = ds;
		jdbc = new ArrayList<>();
		ds.forEach((d) -> {
			XJdbcTemplate j = new XJdbcTemplate();
			j.setDataSource(d);
			jdbc.add(j);
		});
		setDataSource(ds.get(0));
	}

	public static interface Consumer<T>
	{
		T accept(XJdbcTemplate jt) throws DataAccessException;
	}

	public void init()
	{
		versionKey = "status.refresh.jdbc." + name;
		lastVersion = AppConfig.getInstance().getProperty(versionKey, StringX.EMPTY_STRING);
	}
	
	public void destroy()
	{
		ds.forEach((d)->{
		});
	}

	@Override
	public Map checkStatus(Map param)
	{
		return null;
	}

	@Override
	public boolean changeStatus(Map param)
	{
		return false;
	}

	@Override
	public void refresh() throws Exception
	{

	}

	@Override
	public boolean needRefresh()
	{
		if (StringX.nullity(versionKey))
		{
			log.debug("dbVerDtKey is null!!!");
			return false;
		}
		String curVersion = AppConfig.getInstance().getProperty(versionKey, StringX.EMPTY_STRING);
		if (StringX.nullity(curVersion) || lastVersion.equalsIgnoreCase(curVersion))
		{
			log.debug("no refresh key:{}, lastVersion:{}={}", versionKey, lastVersion, curVersion);
			return false;
		}
		log.info("refresh key:{}, lastVersion:{} != {}", versionKey, lastVersion, curVersion);
		lastVersion = curVersion;
		return true;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
