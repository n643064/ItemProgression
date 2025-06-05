package n643064.item_progression.mixin;

import n643064.item_progression.Util;
import n643064.item_progression.client.Client;
import n643064.item_progression.client.UsagePopup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static n643064.item_progression.Config.CONFIG;

@Environment(EnvType.SERVER)
@Mixin(ItemStack.class)
public class ServerItemStackMixin
{
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir)
    {
        final ItemStack stack = player.getItemInHand(interactionHand);
        if (Util.serverCheckItemRestricted((ServerPlayer) player, stack.getItem()))
        {
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir)
    {
        final ItemStack stack = useOnContext.getItemInHand();
        if (Util.serverCheckItemRestricted((ServerPlayer) useOnContext.getPlayer(), stack.getItem()))
        {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
