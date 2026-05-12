
package simpleintro.handlers;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import com.fuzs.aquaacrobatics.entity.player.IPlayerResizeable;
import simpleintro.SimpleIntro;
import simpleintro.network.PacketCutsceneState;
import simpleintro.network.PacketHandler;

@Mod.EventBusSubscriber(modid = SimpleIntro.MODID)
public class CutsceneHandler {


    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerLoggedInEvent event) {

        if (!SimpleIntro.crawlingEnabled) { return; }

        EntityPlayer player = event.player;
        NBTTagCompound nbt = player.getEntityData();

        //if player hasn't seen cutscene yet, mark them as in cutscene
        if (!nbt.getBoolean("SeenCutscene")) {
            nbt.setBoolean("InCutscene", true);
            nbt.setBoolean("InitCrawl", false);

            //sync to client
            if (player instanceof EntityPlayerMP) {
                PacketHandler.INSTANCE.sendTo(
                        new PacketCutsceneState(true),
                        (EntityPlayerMP) player
                );
            }
        }
    }


    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent event) {
        if (event.phase == Phase.END || event.side != Side.SERVER) { return; }

        EntityPlayer player = event.player;
        NBTTagCompound nbt = player.getEntityData();

        if (!nbt.getBoolean("InCutscene")) { return; }

        //apply invisibility and invulnerability
        player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 100, 0, true, false));
        player.setEntityInvulnerable(true);

        //get player pose
        IPlayerResizeable playerResizable = (IPlayerResizeable) player;
        String pose = playerResizable.getPose().toString();

        //if player wasn't crawling initially, mark them as needing to crawl
        if (!pose.equals("STANDING")) {
            nbt.setBoolean("InitCrawl", true);
        }

        //cutscene ends when:
            //player has been forced to crawl
            //player stands back up
        if (nbt.getBoolean("InitCrawl") && pose.equals("STANDING")) {
            //end cutscene
            //remove effects
            nbt.setBoolean("InitCrawl", false);
            player.removePotionEffect(MobEffects.INVISIBILITY);
            player.setEntityInvulnerable(false);
            nbt.setBoolean("InCutscene", false);
            nbt.setBoolean("SeenCutscene", true);

            //sync to client
            if (player instanceof EntityPlayerMP) {
                PacketHandler.INSTANCE.sendTo(
                        new PacketCutsceneState(false),
                        (EntityPlayerMP) player
                );
            }
        }
    }
}
