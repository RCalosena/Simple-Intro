package simpleintro.handlers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import simpleintro.SimpleIntro;

@Config(modid = SimpleIntro.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	public static class ClientConfig {

		@Config.Name("Cutscene Wait Ticks")
		@Config.Comment("How many ticks to wait")
		public int wait_ticks = 100;

		@Config.Name("Fade Duration")
		@Config.Comment("How many fade ticks for the black screen")
    	public int fade_duration = 100;

		@Config.Name("Text Draw Delay")
		@Config.Comment("How many ticks before the text appears")
    	public int text_draw_delay = 200;

	}

	@Mod.EventBusSubscriber(modid = SimpleIntro.MODID)
	private static class EventHandler{

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(SimpleIntro.MODID)) {
				ConfigManager.sync(SimpleIntro.MODID, Config.Type.INSTANCE);
			}
		}
	}
}