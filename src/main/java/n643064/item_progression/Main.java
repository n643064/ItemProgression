package n643064.item_progression;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.Vec3;

import static n643064.item_progression.Config.CONFIG;
import static n643064.item_progression.Config.SKILLS;
import static n643064.item_progression.Networking.*;

public class Main implements ModInitializer
{
    public static final String MODID = "item_progression";

    @Override
    public void onInitialize()
    {
        ServerWorldEvents.LOAD.register(Config::generateCaches);
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, serverResourceManager, success) -> Config.generateCaches(server, null));
        Config.setup();


        PayloadTypeRegistry.playS2C().register(SYNC_SKILLS, SYNC_SKILLS_CODEC);
        PayloadTypeRegistry.playS2C().register(SYNC_ITEMS, SYNC_ITEMS_CODEC);
        PayloadTypeRegistry.playS2C().register(SET_SKILLS, SET_SKILLS_CODEC);
        PayloadTypeRegistry.playS2C().register(SKILL_CHANGED, SKILL_CHANGED_CODEC);
        PayloadTypeRegistry.playC2S().register(REQUEST_SKILL_INCREASE, REQUEST_SKILL_INCREASE_CODEC);
        PayloadTypeRegistry.playC2S().register(REQUEST_UPDATE_SKILLS, REQUEST_UPDATE_SKILLS_CODEC);


        ServerPlayerEvents.JOIN.register(serverPlayer ->
        {
            ServerPlayNetworking.send(serverPlayer, new Networking.SyncSkillsPayload(CONFIG.skills()));
            ServerPlayNetworking.send(serverPlayer, new Networking.SyncItemsPayload(CONFIG.itemMap()));
            ServerPlayNetworking.send(serverPlayer, new Networking.SetSkillsPayload(PlayerData.safeGetSkillMap(serverPlayer.server.overworld(), serverPlayer.getGameProfile().getName())));
        });

        ServerPlayNetworking.registerGlobalReceiver(REQUEST_SKILL_INCREASE, ((payload, context) ->
        {
            final ServerPlayer player = context.player();
            final String playerName = player.getGameProfile().getName();
            final PlayerData data = PlayerData.get(context.server().overworld());
            final int nextLevel = data.safeGetSkillMap(playerName).get(payload.name()) + 1;

            if (nextLevel <= SKILLS.get(payload.name()).maxLevel() && player.experienceLevel >= nextLevel)
            {
                player.giveExperiencePoints(-xpDrainFromLevel(nextLevel));
                data.setSkillLevel(playerName, payload.name(), nextLevel);
                ServerPlayNetworking.send(player, new SkillChangedPayload(payload.name(), nextLevel));
            }
        }));
        if (CONFIG.cropsDropExperience())
            PlayerBlockBreakEvents.AFTER.register((level, player, blockPos, blockState, blockEntity) ->
            {
                if (!level.isClientSide && blockState.getBlock() instanceof CropBlock cropBlock && cropBlock.isMaxAge(blockState))
                    ExperienceOrb.award((ServerLevel) level, Vec3.atCenterOf(blockPos), level.random.nextInt(CONFIG.cropXpMin(), CONFIG.cropXpMax()));
            });


        ServerEntityEvents.EQUIPMENT_CHANGE.register(((livingEntity, equipmentSlot, itemStack, itemStack1) ->
        {
            if (livingEntity instanceof ServerPlayer player && equipmentSlot.isArmor())
            {
                if (Util.serverCheckItemRestricted(player, itemStack1.getItem()))
                    player.addItem(itemStack1.copyAndClear());
            }

        }));
    }

    public static int xpDrainFromLevel(int level)
    {
        return (int) (Math.pow(level, CONFIG.xpDrainLevelExponent()) + level * CONFIG.xpDrainLevelMultiplier());
    }

}
