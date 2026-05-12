package simpleintro.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import simpleintro.SimpleIntro;

public class PacketHandler {
    public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(SimpleIntro.MODID);
    private static int packetId = 0;

    public static void init() {
        INSTANCE.registerMessage(PacketCutsceneState.Handler.class, PacketCutsceneState.class, packetId++, Side.CLIENT);
    }
}
