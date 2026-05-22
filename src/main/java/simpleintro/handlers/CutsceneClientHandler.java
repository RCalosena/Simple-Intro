package simpleintro.handlers;

import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.toasts.GuiToast;
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
import java.util.ArrayList;
import simpleintro.SimpleIntro;
import mcp.mobius.waila.overlay.WailaTickHandler;
import static simpleintro.handlers.ForgeConfigHandler.common;
import static simpleintro.handlers.ForgeConfigHandler.client;

@Mod.EventBusSubscriber(modid = SimpleIntro.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class CutsceneClientHandler {
    
    private static int fadeTicks = 0;
    private static boolean fading = false;
    private static boolean hasFaded = false;
    private static int sceneTicks = 0;
    private static boolean crawled = false;

    public static int getContextLength() {
        if (client.context.length == 0) {
            return 1;
        }

        return client.context.length;
    }

    private static int segmentWaitTicks = client.wait_ticks / getContextLength();
    private static int currentDialogue = 0;
    private static float delay = 20.0F;

    public static boolean isInCutscene() {
        
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
            currentDialogue = 0;
            segmentWaitTicks = client.wait_ticks / getContextLength();

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

        // Close any open GUIs (pause menu, etc)
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null) { return; }

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
        if (event.phase == Phase.END || Minecraft.getMinecraft().player == null || Minecraft.getMinecraft().world == null) { return; }

        GuiScreen gui = Minecraft.getMinecraft().currentScreen;

        int wait_ticks = client.wait_ticks;
        int fade_duration = client.fade_duration;

        if (Minecraft.getMinecraft().player.getEntityData().getBoolean("SeenCutscene") && common.cutsceneType == common.cutsceneType.REDUCED) {
            wait_ticks = client.reducedConfig.reduced_wait_ticks;
            fade_duration = client.reducedConfig.reduced_fade_duration;
        }

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
                fadeTicks = 0;
                fading = false;
                hasFaded = false;
                sceneTicks = 0;
                crawled = false;
                currentDialogue = 0;
                segmentWaitTicks = client.wait_ticks / getContextLength();
            }
        }

        //increase the index of the dialogue
        if (sceneTicks > 0 && sceneTicks % segmentWaitTicks == 0) {
            currentDialogue++;
        }
    }

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent event) {
        if (!isInCutscene() && !fading) { return; }

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.world == null) { return; }

        if (event.getType() != RenderGameOverlayEvent.ElementType.VIGNETTE) {
            if (event.isCancelable()) {
                event.setCanceled(true);
            }
        }

        //disable waila tooltips if the mod is installed
        if (Loader.isModLoaded("waila")) {
            if (WailaTickHandler.instance().tooltip != null) {
                WailaTickHandler.instance().tooltip = null;
            }
        }

        ScaledResolution sr = new ScaledResolution(mc);

        int wait_ticks = client.wait_ticks;
        int fade_duration = client.fade_duration;
        int text_draw_delay = client.text_draw_delay;
        String[] context = client.context;

        float fade = 20.0F;

        if (mc.player.getEntityData().getBoolean("SeenCutscene") && common.cutsceneType == common.cutsceneType.REDUCED) {
            wait_ticks = client.reducedConfig.reduced_wait_ticks;
            fade_duration = client.reducedConfig.reduced_fade_duration;
            text_draw_delay = client.reducedConfig.reduced_text_draw_delay;
        }

        //modify alpha for black screen
        int alpha = 0;
        if (fading) {
            if (fadeTicks <= wait_ticks) {
                alpha = 255;
            } else {
                int fadeProgress = fadeTicks - wait_ticks;
                alpha = (int) (255 * (fade_duration - fadeProgress) / (double) fade_duration);
            }
        }

        //draw black rectangle
        if (alpha > 0) {
            int color = (alpha << 24) & 0xFF000000;  // ARGB black with alpha
            Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, color);
        }

        if (sceneTicks > 0 && !mc.player.getEntityData().getBoolean("SeenCutscene") && context.length > 0) {
            int l1 = 0;
            float duration;

            //Fade in-out logic
            if (currentDialogue < getContextLength()) {

                duration = (float) (sceneTicks % segmentWaitTicks + delay) - event.getPartialTicks();
                
                if (duration >= delay) {

                    if (duration - delay < delay + fade) {
                        l1 = (int) (((duration - delay) * 255.0F / fade) - (255.0F / fade) * delay);
                    } else {
                        if (duration - delay < segmentWaitTicks - fade) {
                            l1 = 255;
                        } else {
                            l1 = (int) (((duration - delay) * -255.0F / fade) + (255.0F / fade) * segmentWaitTicks);
                        }
                    }

                }

                if (l1 < 0) {
                    l1 = 0;
                }

                int color = 0xFFFFFF;

                if (l1 > 8) {
                    GlStateManager.enableBlend();
                    GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                    Minecraft.getMinecraft().fontRenderer.drawString(context[currentDialogue], sr.getScaledWidth() / 2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(context[currentDialogue]) / 2, sr.getScaledHeight() / 2, color + (l1 << 24 & -color));
                    GlStateManager.disableBlend();
                }

            }

        }

        //draw text
        if (sceneTicks >= text_draw_delay) {
            FontRenderer font = mc.fontRenderer;
            int screenWidth = sr.getScaledWidth();
            int screenHeight = sr.getScaledHeight();
            String text = "Press " + Keybindings.forceCrawling.getDisplayName() + " to Stand Up";
            int textWidth = font.getStringWidth(text);

            font.drawString(text, screenWidth / 2 - textWidth / 2, screenHeight / 2 + 50, 0xAAAAAA, false);
        }
    }
}