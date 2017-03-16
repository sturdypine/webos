package spc.webos.message;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;

import spc.webos.service.BaseService;
import spc.webos.service.common.DictDesc;
import spc.webos.service.common.DictService;
import spc.webos.service.common.OptionMap;
import spc.webos.service.common.impl.DictServiceImpl;
import spc.webos.util.JsonUtil;
import spc.webos.util.StringX;

/**
 * 系统数据字典服务的Message
 * 
 * @author chen.jinsong
 * 
 */
public class DictMessageSource extends BaseService implements MessageSource
{
	protected Map<String, DictDesc> dict = new ConcurrentHashMap<>();
	private static DictMessageSource DICT_MSG = new DictMessageSource();

	public static DictMessageSource getInstance()
	{
		return DICT_MSG;
	}

	private DictMessageSource()
	{
		versionKey = "status.refresh.common.dictms";
	}

	public void refresh() throws Exception
	{
		Map<String, DictDesc> dict = new HashMap<>();
		if (dictService != null) dictService.getAllDictDescVO(dict);
		log.info("dict table items: {}", dict.size());
		loadBySqlId(dict);
		log.info("dict items with sql: {}", dict.size());
		this.dict = dict;
	}

	/**
	 * 如果args[0]有值且不为空，则表示在树结构的数据字典中，需要显示父亲节点
	 * args[1]有值，则表示item项需要使用MessageFormat对这一项的数据进行格式化
	 */
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale)
	{
		List path = args != null && args.length > 0 && args[0] != null
				&& ((String) args[0]).length() > 0 ? new ArrayList() : null;
		DictDesc dd = getCode(code, path);
		if (dd == null) return defaultMessage;
		if (path == null)
		{
			if (args == null || args.length < 2) return dd.toString();
			return MessageFormat.format((String) args[1], dd.getItem());
		}

		StringBuffer b = new StringBuffer();
		for (int i = 0; i < path.size(); i++)
		{
			DictDesc d = (DictDesc) path.get(i);
			if (b.length() > 0) b.append(args[0]);
			b.append(d.getName());
		}
		b.append(args[0]);
		b.append(dd);
		return b.toString();
	}

	public String getMessage(String code, Object[] args, Locale locale)
	{
		return getMessage(code, args, null, locale);
	}

	public String getMessage(MessageSourceResolvable resolvable, Locale locale)
	{
		return null;
	}

	DictDesc getCode(String code, List path)
	{
		int index = code.indexOf(AppMessageSource.SEPARATOR);
		String prefix = code.substring(0, index);
		log.debug("dict:{}, code:{}, path:{}", prefix, code, path);
		DictDesc dd = (DictDesc) this.dict.get(prefix);
		if (dd == null) return null;
		dd = get(dd, code.substring(index + 1), path);
		return dd;
	}

	DictDesc get(DictDesc dd, String key, List path)
	{
		int index = key.indexOf(AppMessageSource.SEPARATOR);
		if (index < 0) return dd.dfs(key, path);
		return get((DictDesc) dd.getDict().get(key.substring(0, index)), key.substring(index + 1),
				path);
	}

	protected void loadBySqlId(Map<String, DictDesc> cache)
	{
		if (dictSqlId == null) return;
		log.info("load dict sqls:{}", dictSqlId);
		for (String typ : dictSqlId.keySet())
		{
			String sqId = dictSqlId.get(typ);
			List dicts = (List) persistence.query(sqId, null);
			if (dicts == null || dicts.size() == 0) return;
			String dtype = StringX.EMPTY_STRING;
			DictDesc ddesc = null;
			OptionMap dict = null;
			for (int i = 0; i < dicts.size(); i++)
			{
				List row = (List) dicts.get(i);
				// String typ = (String) row.get(row.size() - 1); //
				// 数据字典最后一个字段为类型
				if (!typ.equals(dtype))
				{
					dtype = typ;
					ddesc = new DictDesc();
					dict = new OptionMap();
					ddesc.setDict(dict);
					ddesc.setItem(new Object[] { typ, typ });
					cache.put(typ, ddesc);
				}
				DictDesc vo = new DictDesc();
				vo.setItem(row.toArray());
				dict.put(vo.getItem()[0], vo);
			}
		}
	}

	DictService dictService = new DictServiceImpl();
	protected Map<String, String> dictSqlId = new HashMap<>(); // key为数据字典类型

	public void setDictService(DictService dictService)
	{
		this.dictService = dictService;
	}

	public void setDictSqlId(String dictSqlId)
	{
		if (StringX.nullity(dictSqlId)) return;
		this.dictSqlId = (Map<String, String>) JsonUtil.json2obj(dictSqlId);
	}

	public Map getDict()
	{
		return dict;
	}
}
