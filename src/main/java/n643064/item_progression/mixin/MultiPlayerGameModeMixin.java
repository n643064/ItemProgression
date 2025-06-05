package n643064.item_progression.mixin;

import n643064.item_progression.Util;
import n643064.item_progression.client.Client;
import n643064.item_progression.client.UsagePopup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static n643064.item_progression.Config.CONFIG;

@Environment(EnvType.CLIENT)
@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin
{
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Player player, Entity entity, CallbackInfo ci)
    {
        final ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (Util.clientCheckItemRestricted(stack.getItem()))
        {
            Client.POPUP = new UsagePopup(Minecraft.getInstance(), CONFIG.requirementsPopupSeconds());
            ci.cancel();
        }
    }

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true)
    private void startDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir)
    {
        assert this.minecraft.player != null;
        final ItemStack stack = this.minecraft.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (Util.clientCheckItemRestricted(stack.getItem()))
        {
            Client.POPUP = new UsagePopup(Minecraft.getInstance(), CONFIG.requirementsPopupSeconds());
            cir.setReturnValue(false);
        }
    }
}
