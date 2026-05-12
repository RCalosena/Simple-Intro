package simpleintro.handlers;

import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.nbt.NBTTagCompound;
import com.fuzs.aquaacrobatics.util.Keybindings;
import com.fuzs.aquaacrobatics.network.NetworkHandler;
import com.fuzs.aquaacrobatics.network.message.PacketSendKey;
import simpleintro.SimpleIntro;
import static simpleintro.handlers.ForgeConfigHandler.client;

@Mod.EventBusSubscriber(modid = SimpleIntro.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CutsceneClientHandler {
    
    private static int fadeTicks = 0;
    private static boolean fading = false;
    private static boolean hasFaded = false;
    private static int sceneTicks = 0;
    private static boolean crawled = false;

    private static boolean isInCutscene() {
        
        Minecraft mc = Minecraft.getMinecraft();
        
        if (mc.player == null) { 
            return false;
        }

        NBTTagCompound nbt = mc.player.getEntityData();
        return nbt.getBoolean("InCutscene");

    }

    //reset variables on join
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() == null) { return; }
        if (event.getEntity() instanceof EntityPlayerSP) {
            fadeTicks = 0;
            fading = false;
            hasFaded = false;
            sceneTicks = 0;
            crawled = false;

            if (!SimpleIntro.crawlingEnabled) {
                TextComponentString msg = new TextComponentString("\u00A7cWarning: \u00A76Simple Intro does not work without enabling \"Toggle Crawling\" in the Aqua Acrobatics config.");
                Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(msg);
            }
        }

    }

    //restrictions
    @SubscribeEvent
    public static void onInputUpdate(InputUpdateEvent event) {
        if (!isInCutscene()) { return; }

        // Disable movement
        event.getMovementInput().moveForward = 0;
        event.getMovementInput().moveStrafe = 0;
        event.getMovementInput().jump = false;
        event.getMovementInput().sneak = false;

        // Force player to crawl on first tick
        if (!crawled) {
            NetworkHandler.INSTANCE.sendToServer(new PacketSendKey(PacketSendKey.KeybindPacket.TOGGLE_CRAWLING));
            crawled = true;
        }

        // Close any open GUIs (pause menu, etc)
        Minecraft mc = Minecraft.getMinecraft();

        // Lock inventory hand to slot 8
        if (mc.player != null) {
            mc.player.inventory.currentItem = 8;
        }
    }

    //disable guis (except pause)
    @SubscribeEvent
    public static void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() == null) { return; }
        if (event.getGui().doesGuiPauseGame()) { return; }
        if (!isInCutscene()) { return; }

        event.setGui(null);
    }

    //disable mouse inputs
    @SubscribeEvent
    public static void onMouseEvent(MouseEvent event) {
        if (isInCutscene()) {
            event.setCanceled(true);
        }
    }

    //handle fading
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == Phase.END || Minecraft.getMinecraft().player == null) { return; }

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        int wait_ticks = client.wait_ticks;
        int fade_duration = client.fade_duration;

        //trigger fade
        if (isInCutscene() && !hasFaded) {
            fading = true;
            fadeTicks = 0;
            hasFaded = true;
        }

        //update fade
        if (fading) {
            if (gui == null) {
                fadeTicks++;
            } else {
                if (!gui.doesGuiPauseGame()) {
                    fadeTicks++;
                }
            }
            if (fadeTicks >= wait_ticks + fade_duration) {
                fading = false;
            }
        }

        //update scene timer
        if (isInCutscene()) {
            if (gui == null) {
                sceneTicks++;
            } else {
                if (!gui.doesGuiPauseGame()) {
                    sceneTicks++;
                }
            }
        } else {
            //reset variables when cutscene ends
            if (hasFaded) {
                sceneTicks = 0;
                fadeTicks = 0;
                fading = false;
                hasFaded = false;
                crawled = false;
            }
        }
    }


    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (!isInCutscene() && !fading) { return; }

        //cancel all UI elements except VIGNETTE
        if (event.getType() != RenderGameOverlayEvent.ElementType.VIGNETTE) {
            event.setCanceled(true);
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) { return; }
        int wait_ticks = client.wait_ticks;
        int fade_duration = client.fade_duration;
        int text_draw_delay = client.text_draw_delay;

        //modify alpha for black screen
        int alpha = 0;
        if (fading) {
            if (fadeTicks <= wait_ticks) {
                alpha = 255;
            } else {
                int fadeProgress = fadeTicks - wait_ticks;
                if (fadeProgress >= fade_duration) {
                    alpha = 0;
                } else {
                    alpha = (int) (255 * (fade_duration - fadeProgress) / (double) fade_duration);
                }
            }
        }

        //draw black rectangle
        if (alpha > 0) {
            int color = (alpha << 24) & 0xFF000000;  // ARGB black with alpha
            Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, color);
        }

        //draw text
        if (sceneTicks >= text_draw_delay) {
            ScaledResolution sr = new ScaledResolution(mc);
            FontRenderer font = mc.fontRenderer;
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();
            String text = "Press " + Keybindings.forceCrawling.getDisplayName() + " to Stand Up";
            int textWidth = font.getStringWidth(text);

            font.drawString(text, screenWidth / 2 - textWidth / 2, screenHeight / 2 + 50, 0xAAAAAA, false);
        }
    }
}