package spc.webos.web.filter.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

public class AntPathBasedFilterDefinitionMap
{
	private List<EntryHolder> requestMap = new ArrayList<>();
	private PathMatcher pathMatcher = new AntPathMatcher();
	private boolean convertUrlToLowercaseBeforeComparison = true;

	public void addSecureUrl(String antPath, List<String> attr)
	{
		requestMap.add(new EntryHolder(antPath, attr));
	}

	public Iterator getConfigAttributeDefinitions()
	{
		Set set = new HashSet();
		Iterator iter = requestMap.iterator();

		while (iter.hasNext())
		{
			EntryHolder entryHolder = (EntryHolder) iter.next();
			set.add(entryHolder.getConfigAttributeDefinition());
		}

		return set.iterator();
	}

	public int getMapSize()
	{
		return this.requestMap.size();
	}

	public boolean isConvertUrlToLowercaseBeforeComparison()
	{
		return convertUrlToLowercaseBeforeComparison;
	}

	public List<String> lookupAttributes(String url)
	{
		// Strip anything after a question mark symbol, as per SEC-161.
		int firstQuestionMarkIndex = url.lastIndexOf("?");
		if (firstQuestionMarkIndex > 0) url = url.substring(0, firstQuestionMarkIndex);
		if (convertUrlToLowercaseBeforeComparison) url = url.toLowerCase();

		Iterator iter = requestMap.iterator();
		while (iter.hasNext())
		{ // match url...
			EntryHolder entryHolder = (EntryHolder) iter.next();
			if (pathMatcher.match(entryHolder.getAntPath(), url))
				return entryHolder.getConfigAttributeDefinition();
		}
		return new ArrayList<>();
	}

	public void setConvertUrlToLowercaseBeforeComparison(
			boolean convertUrlToLowercaseBeforeComparison)
	{
		this.convertUrlToLowercaseBeforeComparison = convertUrlToLowercaseBeforeComparison;
	}

	public String toString()
	{
		return this.requestMap.toString();
	}

	/**
	 * @author chenjs
	 */
	protected class EntryHolder
	{
		private List<String> configAttributeDefinition;
		private String antPath;

		public EntryHolder(String antPath, List<String> attr)
		{
			this.antPath = antPath.trim();
			this.configAttributeDefinition = attr;
		}

		protected EntryHolder()
		{
			throw new IllegalArgumentException("Cannot use default constructor");
		}

		public String getAntPath()
		{
			return antPath;
		}

		public List<String> getConfigAttributeDefinition()
		{
			return configAttributeDefinition;
		}

		public String toString()
		{
			return antPath + " = " + configAttributeDefinition;
		}
	}
}
