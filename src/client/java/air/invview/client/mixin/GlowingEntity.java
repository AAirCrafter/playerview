package air.invview.client.mixin;

import air.invview.client.gui.PlayerListScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GlowingEntity {

    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void forceGlowing(CallbackInfoReturnable<Boolean> cir) {
        var entity = (LivingEntity)(Object) this;

        if (entity instanceof PlayerEntity player) {
            if (!(MinecraftClient.getInstance().currentScreen instanceof PlayerListScreen)) return;
            if (PlayerListScreen.hoveredPlayer == null) return;
            if (PlayerListScreen.hoveredPlayer.getProfile().id().equals(player.getUuid())) cir.setReturnValue(true);
        }
    }
}
