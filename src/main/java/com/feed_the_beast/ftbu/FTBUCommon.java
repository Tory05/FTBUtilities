package com.feed_the_beast.ftbu;

import com.feed_the_beast.ftbu.api.FTBUtilitiesAPI;
import com.feed_the_beast.ftbu.api.NodeEntry;
import com.feed_the_beast.ftbu.api.chunks.IChunkUpgrade;
import com.feed_the_beast.ftbu.api.events.registry.RegisterChunkUpgradesEvent;
import com.feed_the_beast.ftbu.api.events.registry.RegisterCustomPermissionPrefixesEvent;
import com.feed_the_beast.ftbu.api_impl.FTBUtilitiesAPI_Impl;
import com.feed_the_beast.ftbu.net.FTBUNetHandler;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Collection;
import java.util.HashSet;

public class FTBUCommon
{
	public static final Collection<NodeEntry> CUSTOM_PERM_PREFIX_REGISTRY = new HashSet<>();
	public static final IChunkUpgrade[] CHUNK_UPGRADES = new IChunkUpgrade[32];

	public void preInit()
	{
		FTBUtilitiesAPI.API = new FTBUtilitiesAPI_Impl();
		FTBUNetHandler.init();

		new RegisterCustomPermissionPrefixesEvent(CUSTOM_PERM_PREFIX_REGISTRY::add).post();
		new RegisterChunkUpgradesEvent(upgrade -> CHUNK_UPGRADES[upgrade.getId()] = upgrade).post();
	}

	public void postInit()
	{
	}

	public void onReloadedClient()
	{
	}

	public void openNBTEditorGui(NBTTagCompound info, NBTTagCompound mainNbt)
	{
	}
}