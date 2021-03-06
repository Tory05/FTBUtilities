package com.feed_the_beast.ftbu.ranks;

import com.feed_the_beast.ftbl.api.FTBLibAPI;
import com.feed_the_beast.ftbl.api.IRankConfig;
import com.feed_the_beast.ftbl.api.config.IConfigValue;
import com.feed_the_beast.ftbl.lib.config.PropertyNull;
import com.feed_the_beast.ftbu.api.IRank;

/**
 * @author LatvianModder
 */
public class DefaultOPRank extends DefaultRank
{
	public static final DefaultOPRank INSTANCE = new DefaultOPRank();

	private DefaultOPRank()
	{
		super("builtin_op");
	}

	@Override
	public IRank getParent()
	{
		return DefaultPlayerRank.INSTANCE;
	}

	@Override
	public IConfigValue getConfig(String id)
	{
		IRankConfig config = FTBLibAPI.API.getRankConfigRegistry().get(id);
		return config == null ? PropertyNull.INSTANCE : config.getDefOPValue();
	}
}