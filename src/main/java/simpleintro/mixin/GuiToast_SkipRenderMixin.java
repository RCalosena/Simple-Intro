package simpleintro.mixin; 

import simpleintro.handlers.CutsceneClientHandler; 
import net.minecraft.client.gui.toasts.GuiToast; 
import net.minecraft.client.gui.ScaledResolution; 
import org.spongepowered.asm.mixin.Mixin; 
import org.spongepowered.asm.mixin.injection.At; 
import org.spongepowered.asm.mixin.injection.Inject; 
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo; 

@Mixin(GuiToast.class) public abstract class GuiToast_SkipRenderMixin { 
    @Inject( 
        method = "drawToast", 
        at = @At(value = "HEAD"), 
        cancellable = true 
    ) 
    
    private void simpleintro_skip_toast_render(ScaledResolution resolution, CallbackInfo ci) { 
        if (CutsceneClientHandler.isInCutscene()) { 
            ci.cancel(); 
        } 
    } 
}