package n643064.item_progression.mixin;

import n643064.item_progression.Util;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.SERVER)
@Mixin(Player.class)
public abstract class ServerPlayerMixin
{
    @Shadow public abstract Inventory getInventory();
    @Shadow @NotNull public abstract ItemStack getWeaponItem();

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void attack(Entity entity, CallbackInfo ci)
    {
        if (Util.serverCheckItemRestricted((ServerPlayer) getInventory().player, getWeaponItem().getItem()))
            ci.cancel();
    }
}
