package spc.webos.service.common.impl;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.Template;
import spc.webos.model.FTLPO;
import spc.webos.model.SQLPO;
import spc.webos.persistence.loader.DBSQLItemLoader;
import spc.webos.service.BaseService;
import spc.webos.util.FTLUtil;
import spc.webos.util.StringX;

public class FtlSqlServiceImpl extends BaseService
{
	protected String[] ds; // 数据源
	protected String[] sys; // 系统编号
	protected String[] lastVer; // 最后一次版本时间

	@Override
	public void init() throws Exception
	{
		log.info("refresh sql & ftl in DS:{}, sys:{}", ds, sys);
		for (int i = 0; i < sys.length; i++)
		{
			loadSQLInDB(null, sys[i]);
			loadFTLInDB(null, sys[i]);
			lastVer[i] = config.getProperty("status.refresh.ftlsql." + sys[i], "");
		}
		super.init();
	}

	@Override
	public boolean needRefresh()
	{
		for (int i = 0; i < sys.length; i++)
		{
			String curVer = config.getProperty("status.refresh.ftlsql." + sys[i], "");
			if (curVer.equals(lastVer[i])) continue;
			log.info("ftl&sql refresh sys:{}, lastVer:{}, curVer:{}", sys[i], lastVer[i], curVer);
			loadSQLInDB(null, sys[i]);
			loadFTLInDB(null, sys[i]);
			lastVer[i] = curVer;
		}
		return false;
	}

	public void loadFTLInDB(String ds, String sys)
	{
		log.info("refresh ftl in DS:{}, sys:{}", ds, sys);
		try// (SwitchDynamicDS sdds = new SwitchDynamicDS(ds))
		{
			FTLPO po = new FTLPO();
			if (!StringX.nullity(sys)) po.setSys(sys);
			List<FTLPO> ftls = persistence.get(po);
			if (ftls == null) return;
			Map<String, Template> cache = new HashMap<>();
			Configuration cfg = new Configuration();
			cfg.setNumberFormat(FTLUtil.numberFormat);
			StringBuilder ftl = new StringBuilder();
			ftls.forEach((vo) -> {
				try
				{
					ftl.setLength(0);
					ftl.append(StringX.null2emptystr(vo.getFtl()));
					ftl.append(StringX.null2emptystr(vo.getFtl1()));
					ftl.append(StringX.null2emptystr(vo.getFtl2()));
					ftl.append(StringX.null2emptystr(vo.getFtl3()));
					cache.put(vo.getId(),
							new Template(vo.getId(), new StringReader(ftl.toString()), cfg));
				}
				catch (Exception e)
				{
					throw new RuntimeException(e);
				}
			});
			log.info("FTL in DB({}) size:{}", ds, cache.size());
			FTLUtil.getInstance().addFtlCache(cache);
		}
		catch (Exception e)
		{
			log.info("loadFTLInDB", e);
		}
	}

	public void loadSQLInDB(String ds, String sys)
	{
		log.info("refresh sql in DS:{}, sys:{}", ds, sys);
		try// (SwitchDynamicDS sdds = new SwitchDynamicDS(ds))
		{
			Map dbSqlMap = new HashMap();
			SQLPO po = new SQLPO();
			if (!StringX.nullity(sys)) po.setSys(sys);
			DBSQLItemLoader.loadSQLItem(persistence.get(po), dbSqlMap, true);
			log.info("SQL in DB({}) size:{}, {}", ds, dbSqlMap.size(), dbSqlMap.keySet());
			persistence.addSqlMap(dbSqlMap);
		}
		catch (Exception e)
		{
			log.info("fail to load SQL in DB:" + e);
		}
	}

	public void setDs(String[] ds)
	{
		this.ds = ds;
	}

	public void setSys(String[] sys)
	{
		this.sys = sys;
		lastVer = new String[sys.length];
	}
}
