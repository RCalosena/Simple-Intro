package simpleintro.handlers;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import simpleintro.SimpleIntro;

@Config(modid = SimpleIntro.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Server & Client Options")
	@Config.Name("Common Options")
	public static final CommonConfig common = new CommonConfig();
	public static class CommonConfig {
		
		@Config.Name("Repeat Cutscene")
		@Config.Comment({"Should the cutscene repeat? Accepts:","- NEVER: Cutscene plays once per world", "- ALWAYS: Cutscene always plays", "- REDUCED: Cutscenes last half as long as the first one"})
		public CutsceneType cutsceneType = CutsceneType.NEVER;

	}

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static ClientConfig client = new ClientConfig();
	public static class ClientConfig {

		@Config.Name("Reduced Timers")
		@Config.Comment("Uses these timers if \"Repeat Cutscene\" is set to \"REDUCED\".")
		public ReducedConfig reducedConfig = new ReducedConfig();
		public class ReducedConfig {

			@Config.Name("Reduced Cutscene Wait Ticks")
			@Config.Comment("How many ticks to wait")
			@Config.RangeInt(min = 1)
			public int reduced_wait_ticks = 50;

			@Config.Name("Reduced Fade Duration")
			@Config.Comment("How many fade ticks for the black screen")
			@Config.RangeInt(min = 1)
			public int reduced_fade_duration = 50;

			@Config.Name("Reduced Text Draw Delay")
			@Config.Comment("How many ticks before the text that tells you to stand up appears")
			@Config.RangeInt(min = 1)
			public int reduced_text_draw_delay = 100;
			
		}

		@Config.Name("Cutscene Wait Ticks")
		@Config.Comment("How many ticks to wait")
		@Config.RangeInt(min = 1)
		public int wait_ticks = 100;

		@Config.Name("Fade Duration")
		@Config.Comment("How many fade ticks for the black screen")
		@Config.RangeInt(min = 1)
    	public int fade_duration = 100;

		@Config.Name("Text Draw Delay")
		@Config.Comment("How many ticks before the text that tells you to stand up appears")
		@Config.RangeInt(min = 1)
    	public int text_draw_delay = 200;

		@Config.Name("Context")
		@Config.Comment("Each line you add to this list will appear in order, while the black screen is still opaque")
		public String[] context = new String[] {};

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

	public enum CutsceneType {
		NEVER, ALWAYS, REDUCED
	}
}