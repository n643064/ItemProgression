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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
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

@Environment(EnvType.SERVER)
@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin
{

    @Shadow @Final protected ServerPlayer player;

    @Inject(method = "destroyBlock", at = @At("HEAD"), cancellable = true)
    private void destroyBlock(BlockPos blockPos, CallbackInfoReturnable<Boolean> cir)
    {
        final ItemStack stack = this.player.getItemInHand(InteractionHand.MAIN_HAND);
        if (Util.serverCheckItemRestricted(player, stack.getItem()))
            cir.setReturnValue(false);
    }
}
