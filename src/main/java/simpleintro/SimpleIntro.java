package simpleintro;

import com.fuzs.aquaacrobatics.config.ConfigHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import simpleintro.network.PacketHandler;

@Mod(modid = SimpleIntro.MODID, version = SimpleIntro.VERSION, name = SimpleIntro.NAME, dependencies = "required-after:aquaacrobatics;required-after:fermiumbooter")
public class SimpleIntro {
    public static final String MODID = "simpleintro";
    public static final String VERSION = "2.0.0";
    public static final String NAME = "SimpleIntro";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean completedLoading = false;
    public static boolean crawlingEnabled;
	
	@Instance(MODID)
	public static SimpleIntro instance;

	@Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler.init();
        crawlingEnabled = ConfigHandler.MovementConfig.enableToggleCrawling;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        completedLoading = true;
    }
}