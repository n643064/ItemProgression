package n643064.item_progression.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.SpawnerBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static n643064.item_progression.Config.CONFIG;

@Mixin(SpawnerBlock.class)
public class SpawnerBlockMixin
{
    @ModifyArg(method = "spawnAfterBreak", at = @At(
            target = "Lnet/minecraft/world/level/block/SpawnerBlock;popExperience(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V",
            value = "INVOKE"
        ), index = 2)
    private int modifyInt(ServerLevel serverlevel, BlockPos bp, int i)
    {
        if (CONFIG.modifySpawnerExperience())
            return serverlevel.random.nextInt(CONFIG.spawnerXpMin(), CONFIG.spawnerXpMax());
        return i;
    }
}
