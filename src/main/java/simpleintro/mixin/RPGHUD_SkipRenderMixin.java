package simpleintro.mixin; 

import simpleintro.handlers.CutsceneClientHandler; 
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.spellcraftgaming.rpghud.main.RenderOverlay;
import org.spongepowered.asm.mixin.Mixin; 
import org.spongepowered.asm.mixin.injection.At; 
import org.spongepowered.asm.mixin.injection.Inject; 
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo; 

@SideOnly(Side.CLIENT)
@Mixin(RenderOverlay.class) 
public abstract class RPGHUD_SkipRenderMixin { 
    @Inject( 
        method = "onGameOverlayRender", 
        at = @At(value = "HEAD"), 
        cancellable = true,
        remap = false 
    ) 
    
    private void simpleintro_skip_rpghud_render(RenderGameOverlayEvent event, CallbackInfo ci) { 
        if (CutsceneClientHandler.isInCutscene()) { 
            ci.cancel(); 
        } 
    } 
}