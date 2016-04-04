package ftb.utils.api.guide.repos;

import ftb.lib.TextureCoords;
import ftb.lib.api.client.FTBLibClient;
import latmod.lib.net.*;
import latmod.lib.util.FinalIDObject;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Created by LatvianModder on 03.04.2016.
 */
public class GuideOnlineRepo extends FinalIDObject implements IGuide
{
	public final String homePath;
	private final GuideInfo info;
	private Map<String, GuideMode> modes;
	private TextureCoords icon;
	
	public GuideOnlineRepo(String id, String path) throws Exception
	{
		super(id);
		homePath = path;
		info = new GuideInfo(getFile("guide.json").asJson().getAsJsonObject());
	}
	
	public GuideInfo getInfo()
	{ return info; }
	
	public Map<String, GuideMode> getModes()
	{
		if(modes == null)
		{
			Map<String, GuideMode> map = new HashMap<>();
			
			for(String s : info.modes)
			{
				try
				{
					map.put(s, new GuideMode(this, s));
				}
				catch(Exception ex)
				{
					//ex.printStackTrace();
				}
			}
			
			modes = Collections.unmodifiableMap(map);
		}
		
		return modes;
	}
	
	public GuideMode getMergedMode(String id)
	{
		GuideMode mode = getModes().get("default");
		if(mode == null) return getModes().get(id);
		else return mode.mergeWith(getModes().get(id));
	}
	
	public Response getFile(String path) throws Exception
	{ return new LMURLConnection(RequestMethod.SIMPLE_GET, homePath + path).connect(); }
	
	public String toString()
	{ return info.name; }
	
	public TextureCoords getIcon()
	{
		if(icon == null)
		{
			icon = new TextureCoords(new ResourceLocation("ftbu_guidepacks", getID()), 0, 0, 16, 16, 16, 16);
			FTBLibClient.getDownloadImage(icon.texture, homePath + "icon.png", new ResourceLocation("textures/misc/unknown_pack.png"), null);
		}
		
		return icon;
	}
}