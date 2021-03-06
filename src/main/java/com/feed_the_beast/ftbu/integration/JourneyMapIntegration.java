package com.feed_the_beast.ftbu.integration;

import com.feed_the_beast.ftbl.api.EventHandler;
import com.feed_the_beast.ftbl.lib.client.ClientUtils;
import com.feed_the_beast.ftbl.lib.math.MathUtils;
import com.feed_the_beast.ftbu.FTBUFinals;
import com.feed_the_beast.ftbu.api_impl.ChunkUpgrade;
import com.feed_the_beast.ftbu.client.FTBUClient;
import com.feed_the_beast.ftbu.client.FTBUClientConfig;
import com.feed_the_beast.ftbu.gui.ClaimedChunks;
import com.feed_the_beast.ftbu.net.MessageClaimedChunksRequest;
import com.feed_the_beast.ftbu.net.MessageJMRequest;
import journeymap.client.api.ClientPlugin;
import journeymap.client.api.IClientAPI;
import journeymap.client.api.IClientPlugin;
import journeymap.client.api.display.DisplayType;
import journeymap.client.api.display.PolygonOverlay;
import journeymap.client.api.event.ClientEvent;
import journeymap.client.api.model.MapPolygon;
import journeymap.client.api.model.ShapeProperties;
import journeymap.client.api.util.PolygonHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LatvianModder
 */
@ClientPlugin //FIXME: JourneyMap Plugin
@EventHandler(value = Side.CLIENT, requiredMods = "journeymap")
public class JourneyMapIntegration implements IClientPlugin, IJMIntegration
{
	private IClientAPI clientAPI;
	private static final Map<ChunkPos, PolygonOverlay> POLYGONS = new HashMap<>();
	private static ChunkPos lastPosition = null;

	@Override
	public void initialize(IClientAPI api)
	{
		clientAPI = api;
		FTBUClient.JM_INTEGRATION = this;
		api.subscribe(getModId(), EnumSet.of(ClientEvent.Type.DISPLAY_UPDATE, ClientEvent.Type.MAPPING_STARTED, ClientEvent.Type.MAPPING_STOPPED));
	}

	@Override
	public String getModId()
	{
		return FTBUFinals.MOD_ID;
	}

	@Override
	public void onEvent(ClientEvent event)
	{
		switch (event.type)
		{
			case DISPLAY_UPDATE:
				if (ClientUtils.MC.player != null)
				{
					new MessageClaimedChunksRequest(ClientUtils.MC.player).sendToServer();
				}
				break;
			case MAPPING_STOPPED:
				clearData();
				break;
		}
	}

	@Override
	public void clearData()
	{
		if (!POLYGONS.isEmpty())
		{
			POLYGONS.clear();
			clientAPI.removeAll(FTBUFinals.MOD_ID);
		}
	}

	@Override
	public void chunkChanged(ChunkPos pos, ClaimedChunks.Data chunk)
	{
		if (!POLYGONS.isEmpty() && (!FTBUClientConfig.JOURNEYMAP_OVERLAY.getBoolean() || !clientAPI.playerAccepts(FTBUFinals.MOD_ID, DisplayType.Polygon)))
		{
			clearData();
			return;
		}

		try
		{
			PolygonOverlay p = POLYGONS.get(pos);

			if (p != null)
			{
				clientAPI.remove(p);

				if (!chunk.hasUpgrade(ChunkUpgrade.CLAIMED))
				{
					POLYGONS.remove(pos);
				}
			}

			if (chunk.hasUpgrade(ChunkUpgrade.CLAIMED))
			{
				int dim = 0;

				MapPolygon poly = PolygonHelper.createChunkPolygon(pos.x, 100, pos.z);
				ShapeProperties shapeProperties = new ShapeProperties();

				shapeProperties.setFillOpacity(0.2F);
				shapeProperties.setStrokeOpacity(0.1F);

				shapeProperties.setFillColor(chunk.team.color.getColor().rgba());
				shapeProperties.setStrokeColor(shapeProperties.getFillColor());

				p = new PolygonOverlay(FTBUFinals.MOD_ID, "claimed_" + dim + '_' + pos.x + '_' + pos.z, dim, shapeProperties, poly);
				p.setOverlayGroupName("Claimed Chunks").setTitle(chunk.team.formattedName + "\n" + TextFormatting.GREEN + ChunkUpgrade.CLAIMED.getLangKey());
				POLYGONS.put(pos, p);
				clientAPI.show(p);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	@SubscribeEvent
	public static void onEnteringChunk(EntityEvent.EnteringChunk event)
	{
		if (FTBUClientConfig.JOURNEYMAP_OVERLAY.getBoolean() && event.getEntity() == ClientUtils.MC.player && (lastPosition == null || MathUtils.dist(event.getNewChunkX(), event.getNewChunkZ(), lastPosition.x, lastPosition.z) >= 3D))
		{
			lastPosition = new ChunkPos(event.getNewChunkX(), event.getNewChunkZ());
			new MessageJMRequest(ClientUtils.MC.player).sendToServer();
		}
	}
}