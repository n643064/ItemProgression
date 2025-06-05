package n643064.item_progression.mixin;

import n643064.item_progression.Config;
import n643064.item_progression.Util;
import n643064.item_progression.client.Client;
import n643064.item_progression.client.UsagePopup;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ClientItemStackMixin
{
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir)
    {
        final ItemStack stack = player.getItemInHand(interactionHand);
        if (Util.clientCheckItemRestricted(stack.getItem()))
        {
            Client.POPUP = new UsagePopup(Minecraft.getInstance(), CONFIG.requirementsPopupSeconds());
            cir.setReturnValue(InteractionResultHolder.fail(stack));
        }
    }

    @Inject(method = "useOn", at = @At("HEAD"), cancellable = true)
    private void useOn(UseOnContext useOnContext, CallbackInfoReturnable<InteractionResult> cir)
    {
        final ItemStack stack = useOnContext.getItemInHand();
        if (Util.clientCheckItemRestricted(stack.getItem()))
        {
            Client.POPUP = new UsagePopup(Minecraft.getInstance(), CONFIG.requirementsPopupSeconds());
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }

    @ModifyArg(method = "getTooltipLines", at = @At(
            target = "Lnet/minecraft/world/item/Item;appendHoverText(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/world/item/TooltipFlag;)V",
            value = "INVOKE"
    ), index = 2)
    private List<Component> modifyAppendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag)
    {
        final Item item = itemStack.getItem();
        if(Client.CLIENT_CACHED_ITEM_MAP.containsKey(item))
        {
            list.add(Component.literal("Requirements:").withStyle(ChatFormatting.DARK_AQUA));
            Client.CLIENT_CACHED_ITEM_MAP.get(item).forEach((k, v) ->
            {
                final Integer a = Client.CLIENT_CACHED_SKILL_MAP.get(k);
                if (a == null) return;
                list.add(Component.literal("  - " + k + " " + v).withStyle(a < v ? ChatFormatting.RED : ChatFormatting.GREEN));
            });
        }
        return list;
    }
}
