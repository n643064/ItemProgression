package n643064.item_progression;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static n643064.item_progression.Main.MODID;
import static net.minecraft.world.level.Level.OVERWORLD;

public record Config(
    List<Skill> skills,
    double xpDrainLevelExponent,
    double xpDrainLevelMultiplier,
    boolean cropsDropExperience,
    int cropXpMin,
    int cropXpMax,
    boolean modifySpawnerExperience,
    int spawnerXpMin,
    int spawnerXpMax,
    int requirementsPopupSeconds,
    String requirementsPopupMessage,
    ItemMap itemMap
)
{

    public record Skill(String name, String iconItem, int maxLevel) {}
    public record CachedSkill(Item iconItem, int maxLevel) {}

    public static class ItemMap extends HashMap<String, Map<String, Integer>> {}
    public static class CachedItemMap extends HashMap<Item, Map<String, Integer>> {}

    private static final ItemMap defaultItemMap = new ItemMap();
    static
    {
        // TODO: Block interaction restrictions

        // Compat

        // Better Archeology
        {
            defaultItemMap.put("betterarcheology:iron_brush", Map.of("Gathering", 8));
            defaultItemMap.put("betterarcheology:diamond_brush", Map.of("Gathering", 12));
            defaultItemMap.put("betterarcheology:netherite_brush", Map.of("Gathering", 16));
            defaultItemMap.put("betterarcheology:bomb", Map.of("Attack", 8, "Magic", 4));
            defaultItemMap.put("betterarcheology:soul_totem", Map.of("Attack", 8, "Magic", 8));
            defaultItemMap.put("betterarcheology:torrent_totem", Map.of("Agility", 8, "Magic", 8));
            defaultItemMap.put("betterarcheology:growth_totem", Map.of("Magic", 8));
            defaultItemMap.put("betterarcheology:radiance_totem", Map.of("Magic", 8));
        }
        // Things
        {
            defaultItemMap.put("things:ender_pouch", Map.of("Magic", 8));
            defaultItemMap.put("things:item_magnet", Map.of("Magic", 8));
            defaultItemMap.put("things:infernal_scepter", Map.of("Magic", 8, "Attack", 8));
        }
        // Mutant Monsters
        {
            defaultItemMap.put("mutantmonsters:mutant_skeleton_skull", Map.of("Defense", 8));
            defaultItemMap.put("mutantmonsters:mutant_skeleton_chestplate", Map.of("Defense", 8));
            defaultItemMap.put("mutantmonsters:mutant_skeleton_leggings", Map.of("Defense", 8));
            defaultItemMap.put("mutantmonsters:mutant_skeleton_boots", Map.of("Defense", 8));
            defaultItemMap.put("mutantmonsters:endersoul_hand", Map.of("Attack", 8, "Building", 8, "Magic", 8));
            defaultItemMap.put("mutantmonsters:hulk_hammer", Map.of("Attack", 12));
            defaultItemMap.put("mutantmonsters:creeper_shard", Map.of("Attack", 12));
        }
        // Dragon Loot
        {
            defaultItemMap.put("dragonloot:dragon_helmet", Map.of("Defense", 30));
            defaultItemMap.put("dragonloot:dragon_chestplate", Map.of("Defense", 30));
            defaultItemMap.put("dragonloot:dragon_leggings", Map.of("Defense", 30));
            defaultItemMap.put("dragonloot:dragon_boots", Map.of("Defense", 30));
            defaultItemMap.put("dragonloot:upgraded_dragon_chestplate", Map.of("Defense", 30));
            defaultItemMap.put("dragonloot:dragon_sword", Map.of("Attack", 30));
            defaultItemMap.put("dragonloot:dragon_axe", Map.of("Attack", 26, "Gathering", 30));
            defaultItemMap.put("dragonloot:dragon_pickaxe", Map.of("Mining", 30));
            defaultItemMap.put("dragonloot:dragon_shovel", Map.of("Gathering", 30));
            defaultItemMap.put("dragonloot:dragon_hoe", Map.of("Farming", 30));
            defaultItemMap.put("dragonloot:dragon_bow", Map.of("Attack", 30, "Agility", 24));
            defaultItemMap.put("dragonloot:dragon_crossbow", Map.of("Attack", 30, "Agility", 26));
            defaultItemMap.put("dragonloot:dragon_trident", Map.of("Attack", 30, "Agility", 20, "Magic", 20));
            defaultItemMap.put("dragonloot:dragon_anvil", Map.of("Building", 24, "Magic", 24));

        }
        // Extra Shields
        {
            defaultItemMap.put("shields:plated_shield", Map.of("Defense", 8));
            defaultItemMap.put("shields:copper_shield", Map.of("Defense", 8));
            defaultItemMap.put("shields:plated_copper_shield", Map.of("Defense", 8));
            defaultItemMap.put("shields:gold_shield", Map.of("Defense", 8, "Magic", 4));
            defaultItemMap.put("shields:plated_gold_shield", Map.of("Defense", 8, "Magic", 4));
            defaultItemMap.put("shields:diamond_shield", Map.of("Defense", 16));
            defaultItemMap.put("shields:plated_diamond_shield", Map.of("Defense", 16));
            defaultItemMap.put("shields:netherite_shield", Map.of("Defense", 24));
            defaultItemMap.put("shields:plated_netherite_shield", Map.of("Defense", 24));
        }
        // Farmer's Delight Refabricated
        {
            defaultItemMap.put("farmersdelight:iron_knife", Map.of("Gathering", 8));
            defaultItemMap.put("farmersdelight:golden_knife", Map.of("Gathering", 4, "Magic", 4));
            defaultItemMap.put("farmersdelight:diamond_knife", Map.of("Gathering", 16));
            defaultItemMap.put("farmersdelight:netherite_knife", Map.of("Gathering", 24));
            defaultItemMap.put("farmersdelight:cooking_pot", Map.of("Gathering", 4, "Farming", 4));
            defaultItemMap.put("farmersdelight:skillet", Map.of("Gathering", 4, "Farming", 4));
            defaultItemMap.put("farmersdelight:cutting_board", Map.of("Gathering", 8));
        }
        // Underground Worlds
        {
            defaultItemMap.put("undergroundworlds:temple_sword", Map.of("Attack", 16));
            defaultItemMap.put("undergroundworlds:temple_axe", Map.of("Attack", 12, "Gathering", 16));
            defaultItemMap.put("undergroundworlds:temple_pickaxe", Map.of("Mining", 16));
            defaultItemMap.put("undergroundworlds:temple_shovel", Map.of("Gathering", 16));
            defaultItemMap.put("undergroundworlds:temple_hoe", Map.of("Farming", 16));
            defaultItemMap.put("undergroundworlds:freezing_sword", Map.of("Attack", 8, "Magic", 8));
            defaultItemMap.put("undergroundworlds:freezing_axe", Map.of("Attack", 4, "Gathering", 8, "Magic", 8));
            defaultItemMap.put("undergroundworlds:freezing_pickaxe", Map.of("Mining", 8, "Magic", 8));
            defaultItemMap.put("undergroundworlds:freezing_shovel", Map.of("Gathering", 8, "Magic", 8));
            defaultItemMap.put("undergroundworlds:freezing_hoe", Map.of("Farming", 8, "Magic", 8));
            defaultItemMap.put("undergroundworlds:blade_of_the_jungle", Map.of("Attack", 16, "Magic", 8));
            defaultItemMap.put("undergroundworlds:axe_of_regrowth", Map.of("Attack", 4, "Gathering", 8, "Magic", 8));
        }
        // Ice and Fire
        {
            defaultItemMap.put("iceandfire:tide_trident", Map.of("Attack", 20, "Agility", 16, "Magic", 12));
            defaultItemMap.put("iceandfire:dread_knight_sword", Map.of("Attack", 30));
            defaultItemMap.put("iceandfire:dread_queen_sword", Map.of("Attack", 16));
            defaultItemMap.put("iceandfire:ghost_sword", Map.of("Attack", 24, "Magic", 24));
            
            defaultItemMap.put("iceandfire:copper_sword", Map.of("Attack", 6));
            defaultItemMap.put("iceandfire:copper_axe", Map.of("Attack", 4, "Gathering", 6));
            defaultItemMap.put("iceandfire:copper_pickaxe", Map.of("Mining", 6));
            defaultItemMap.put("iceandfire:copper_shovel", Map.of("Gathering", 6));
            defaultItemMap.put("iceandfire:copper_hoe", Map.of("Farming", 6));
            defaultItemMap.put("iceandfire:armor_copper_metal_helmet", Map.of("Defense", 6));
            defaultItemMap.put("iceandfire:armor_copper_metal_chestplate", Map.of("Defense", 6));
            defaultItemMap.put("iceandfire:armor_copper_metal_leggings", Map.of("Defense", 6));
            defaultItemMap.put("iceandfire:armor_copper_metal_boots", Map.of("Defense", 6));

            defaultItemMap.put("iceandfire:silver_sword", Map.of("Attack", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:silver_axe", Map.of("Attack", 4, "Gathering", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:silver_pickaxe", Map.of("Mining", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:silver_shovel", Map.of("Gathering", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:silver_hoe", Map.of("Farming", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:armor_silver_metal_helmet", Map.of("Defense", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:armor_silver_metal_chestplate", Map.of("Defense", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:armor_silver_metal_leggings", Map.of("Defense", 6, "Magic", 6));
            defaultItemMap.put("iceandfire:armor_silver_metal_boots", Map.of("Defense", 6, "Magic", 6));

            defaultItemMap.put("iceandfire:dragonbone_sword_fire", Map.of("Attack", 24));
            defaultItemMap.put("iceandfire:dragonbone_sword_ice", Map.of("Attack", 24));
            defaultItemMap.put("iceandfire:dragonbone_sword_lightning", Map.of("Attack", 24));
            defaultItemMap.put("iceandfire:dragonbone_sword", Map.of("Attack", 24));
            defaultItemMap.put("iceandfire:dragonbone_axe", Map.of("Attack", 24, "Gathering", 24));
            defaultItemMap.put("iceandfire:dragonbone_pickaxe", Map.of("Mining", 24));
            defaultItemMap.put("iceandfire:dragonbone_shovel", Map.of("Gathering", 24));
            defaultItemMap.put("iceandfire:dragonbone_hoe", Map.of("Farming", 24));

            defaultItemMap.put("iceandfire:deathworm_yellow_helmet", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_yellow_chestplate", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_yellow_leggings", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_yellow_boots", Map.of("Defense", 8));

            defaultItemMap.put("iceandfire:deathworm_red_helmet", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_red_chestplate", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_red_leggings", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_red_boots", Map.of("Defense", 8));

            defaultItemMap.put("iceandfire:deathworm_white_helmet", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_white_chestplate", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_white_leggings", Map.of("Defense", 8));
            defaultItemMap.put("iceandfire:deathworm_white_boots", Map.of("Defense", 8));

            defaultItemMap.put("iceandfire:myrmex_desert_sword", Map.of("Attack", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_sword_venom", Map.of("Attack", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_axe", Map.of("Attack", 10, "Gathering", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_pickaxe", Map.of("Mining", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_shovel", Map.of("Gathering", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_hoe", Map.of("Farming", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_helmet", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_chestplate", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_leggings", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_desert_boots", Map.of("Defense", 12));

            defaultItemMap.put("iceandfire:myrmex_jungle_sword", Map.of("Attack", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_sword_venom", Map.of("Attack", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_axe", Map.of("Attack", 10, "Gathering", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_pickaxe", Map.of("Mining", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_shovel", Map.of("Gathering", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_hoe", Map.of("Farming", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_helmet", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_chestplate", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_leggings", Map.of("Defense", 12));
            defaultItemMap.put("iceandfire:myrmex_jungle_boots", Map.of("Defense", 12));

            defaultItemMap.put("iceandfire:dragonsteel_fire_helmet", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_chestplate", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_leggings", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_boots", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_sword", Map.of("Attack", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_axe", Map.of("Attack", 26, "Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_pickaxe", Map.of("Mining", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_shovel", Map.of("Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_fire_hoe", Map.of("Farming", 30));

            defaultItemMap.put("iceandfire:dragonsteel_ice_helmet", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_chestplate", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_leggings", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_boots", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_sword", Map.of("Attack", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_axe", Map.of("Attack", 26, "Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_pickaxe", Map.of("Mining", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_shovel", Map.of("Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_ice_hoe", Map.of("Farming", 30));

            defaultItemMap.put("iceandfire:dragonsteel_lightning_helmet", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_chestplate", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_leggings", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_boots", Map.of("Defense", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_sword", Map.of("Attack", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_axe", Map.of("Attack", 26, "Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_pickaxe", Map.of("Mining", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_shovel", Map.of("Gathering", 30));
            defaultItemMap.put("iceandfire:dragonsteel_lightning_hoe", Map.of("Farming", 30));
        }
        // Vanilla

        // Magic
        {
            defaultItemMap.put("minecraft:ender_pearl", Map.of("Magic", 6));
            defaultItemMap.put("minecraft:ender_eye", Map.of("Magic", 12));
            defaultItemMap.put("minecraft:splash_potion", Map.of("Magic", 4));
            defaultItemMap.put("minecraft:lingering_potion", Map.of("Magic", 8));
            defaultItemMap.put("minecraft:ominous_bottle", Map.of("Magic", 8));
            defaultItemMap.put("minecraft:end_crystal", Map.of("Magic", 16));
            defaultItemMap.put("minecraft:wind_charge", Map.of("Magic", 8));
        }
        // Building
        {
            defaultItemMap.put("minecraft:enchanting_table", Map.of("Magic", 16));
            defaultItemMap.put("minecraft:brewing_stand", Map.of("Magic", 16));
            defaultItemMap.put("minecraft:lodestone", Map.of("Building", 8));
            defaultItemMap.put("minecraft:conduit", Map.of("Magic", 12));
            defaultItemMap.put("minecraft:beacon", Map.of("Magic", 24));
            defaultItemMap.put("minecraft:cobweb", Map.of("Building", 8));
            defaultItemMap.put("minecraft:dropper", Map.of("Building", 4));
            defaultItemMap.put("minecraft:redstone", Map.of("Building", 4));
            defaultItemMap.put("minecraft:dispenser", Map.of("Building", 8));
            defaultItemMap.put("minecraft:repeater", Map.of("Building", 8));
            defaultItemMap.put("minecraft:comparator", Map.of("Building", 8));
            defaultItemMap.put("minecraft:target", Map.of("Building", 4));
            defaultItemMap.put("minecraft:anvil", Map.of("Building", 8));
            defaultItemMap.put("minecraft:chipped_anvil", Map.of("Building", 8));
            defaultItemMap.put("minecraft:damaged_anvil", Map.of("Building", 8));
            defaultItemMap.put("minecraft:piston", Map.of("Building", 8));
            defaultItemMap.put("minecraft:sticky_piston", Map.of("Building", 8));
            defaultItemMap.put("minecraft:hopper", Map.of("Building", 8));
            defaultItemMap.put("minecraft:observer", Map.of("Building", 8));
            defaultItemMap.put("minecraft:daylight_detector", Map.of("Building", 8));
            defaultItemMap.put("minecraft:crafter", Map.of("Building", 12));
            defaultItemMap.put("minecraft:sculk_sensor", Map.of("Building", 8, "Magic", 4));
            defaultItemMap.put("minecraft:calibrated_sculk_sensor", Map.of("Building", 12, "Magic", 4));
        }
        // Misc
        {
            defaultItemMap.put("minecraft:bone_meal", Map.of("Farming", 8));
            defaultItemMap.put("minecraft:brush", Map.of("Gathering", 4));
            defaultItemMap.put("minecraft:fishing_rod", Map.of("Gathering", 8));
            defaultItemMap.put("minecraft:shield", Map.of("Defense", 8));
            defaultItemMap.put("minecraft:trident", Map.of("Attack", 12, "Agility", 6, "Magic", 8));
            defaultItemMap.put("minecraft:mace", Map.of("Attack", 16, "Agility", 12, "Magic", 8));
            defaultItemMap.put("minecraft:bow", Map.of("Attack", 4, "Agility", 4));
            defaultItemMap.put("minecraft:crossbow", Map.of("Attack", 4, "Agility", 6));
            defaultItemMap.put("minecraft:turtle_helmet", Map.of("Defense", 4, "Magic", 4));
            defaultItemMap.put("minecraft:shears", Map.of("Gathering", 4));
            defaultItemMap.put("minecraft:elytra", Map.of("Defense", 8, "Magic", 16, "Agility", 24));
        }
        // Tiered items
        {
            defaultItemMap.put("minecraft:iron_sword", Map.of("Attack", 8));
            defaultItemMap.put("minecraft:iron_axe", Map.of("Attack", 4, "Gathering", 8));
            defaultItemMap.put("minecraft:iron_pickaxe", Map.of("Mining", 8));
            defaultItemMap.put("minecraft:iron_shovel", Map.of("Gathering", 8));
            defaultItemMap.put("minecraft:iron_hoe", Map.of("Farming", 8));
            defaultItemMap.put("minecraft:iron_helmet", Map.of("Defense", 8));
            defaultItemMap.put("minecraft:iron_chestplate", Map.of("Defense", 8));
            defaultItemMap.put("minecraft:iron_leggings", Map.of("Defense", 8));
            defaultItemMap.put("minecraft:iron_boots", Map.of("Defense", 8));

            defaultItemMap.put("minecraft:chainmail_helmet", Map.of("Defense", 4));
            defaultItemMap.put("minecraft:chainmail_chestplate", Map.of("Defense", 4));
            defaultItemMap.put("minecraft:chainmail_leggings", Map.of("Defense", 4));
            defaultItemMap.put("minecraft:chainmail_boots", Map.of("Defense", 4));

            defaultItemMap.put("minecraft:golden_sword", Map.of("Attack", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_axe", Map.of("Attack", 4, "Gathering", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_pickaxe", Map.of("Mining", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_shovel", Map.of("Gathering", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_hoe", Map.of("Farming", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_helmet", Map.of("Defense", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_chestplate", Map.of("Defense", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_leggings", Map.of("Defense", 6, "Magic", 4));
            defaultItemMap.put("minecraft:golden_boots", Map.of("Defense", 6, "Magic", 4));

            defaultItemMap.put("minecraft:diamond_sword", Map.of("Attack", 16));
            defaultItemMap.put("minecraft:diamond_axe", Map.of("Attack", 12, "Gathering", 16));
            defaultItemMap.put("minecraft:diamond_pickaxe", Map.of("Mining", 16));
            defaultItemMap.put("minecraft:diamond_shovel", Map.of("Gathering", 16));
            defaultItemMap.put("minecraft:diamond_hoe", Map.of("Farming", 16));
            defaultItemMap.put("minecraft:diamond_helmet", Map.of("Defense", 16));
            defaultItemMap.put("minecraft:diamond_chestplate", Map.of("Defense", 16));
            defaultItemMap.put("minecraft:diamond_leggings", Map.of("Defense", 16));
            defaultItemMap.put("minecraft:diamond_boots", Map.of("Defense", 16));

            defaultItemMap.put("minecraft:netherite_sword", Map.of("Attack", 24));
            defaultItemMap.put("minecraft:netherite_axe", Map.of("Attack", 24, "Gathering", 24));
            defaultItemMap.put("minecraft:netherite_pickaxe", Map.of("Mining", 24));
            defaultItemMap.put("minecraft:netherite_shovel", Map.of("Gathering", 24));
            defaultItemMap.put("minecraft:netherite_hoe", Map.of("Farming", 24));
            defaultItemMap.put("minecraft:netherite_helmet", Map.of("Defense", 24));
            defaultItemMap.put("minecraft:netherite_chestplate", Map.of("Defense", 24));
            defaultItemMap.put("minecraft:netherite_leggings", Map.of("Defense", 24));
            defaultItemMap.put("minecraft:netherite_boots", Map.of("Defense", 24));
        }
    }

    public static Config CONFIG = new Config(
        List.of(
                new Skill("Attack", "minecraft:iron_sword", 30),
                new Skill("Defense", "minecraft:iron_chestplate", 30),
                new Skill("Agility", "minecraft:feather", 30),
                new Skill("Mining", "minecraft:iron_pickaxe", 30),
                new Skill("Gathering", "minecraft:iron_axe", 30),
                new Skill("Farming", "minecraft:iron_hoe", 30),
                new Skill("Magic", "minecraft:ender_pearl", 30),
                new Skill("Building", "minecraft:anvil", 30)
        ),
            2,
            6,
            true,
            1,
            3,
            true,
            10,
            30,
            2,
            "You do not meet the requirements to use this item",
            defaultItemMap
    );


    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    static final String CONFIG_PATH = "config" + File.separator + MODID + ".json";

    public static final HashMap<String, CachedSkill> SKILLS = new HashMap<>();
    public static CachedItemMap CACHED_ITEMS;
    public static void generateCaches(MinecraftServer server, @Nullable ServerLevel level)
    {
        if (level != null && level.dimension() != OVERWORLD)
            return;

        SKILLS.clear();
        for (Skill skill : CONFIG.skills)
            SKILLS.put(skill.name, new CachedSkill(BuiltInRegistries.ITEM.get(ResourceLocation.parse(skill.iconItem)), skill.maxLevel));
        CACHED_ITEMS = generateItemCache(CONFIG.itemMap);

        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            ServerPlayNetworking.send(player, new Networking.SyncSkillsPayload(CONFIG.skills()));
            ServerPlayNetworking.send(player, new Networking.SyncItemsPayload(CONFIG.itemMap()));
            ServerPlayNetworking.send(player, new Networking.SetSkillsPayload(PlayerData.safeGetSkillMap(player.server.overworld(), player.getGameProfile().getName())));
        }

    }

    public static Config.CachedItemMap generateItemCache(ItemMap itemMap)
    {
        final CachedItemMap map = new CachedItemMap();
        itemMap.forEach((k, v) ->
        {
            final Item i = BuiltInRegistries.ITEM.get(ResourceLocation.parse(k));
            if (i == Items.AIR)
                return;
            map.put(i, v);
        });
        return map;
    }


    static void onLoad()
    {

    }

    public static void create() throws IOException
    {
        Path p = Path.of("config");
        if (Files.exists(p))
        {
            if (Files.isDirectory(p))
            {
                FileWriter writer = new FileWriter(CONFIG_PATH);
                writer.write(GSON.toJson(CONFIG));
                writer.flush();
                writer.close();
            }
        } else
        {
            Files.createDirectory(p);
            create();
        }
    }

    public static <T> T read(String path, Class<T> clazz) throws IOException
    {
        final FileReader reader = new FileReader(path);
        T t = GSON.fromJson(reader, clazz);
        reader.close();
        return t;
    }

    public static void setup()
    {
        try
        {
            if (Files.exists(Path.of(CONFIG_PATH)))
            {
                CONFIG = read(CONFIG_PATH, Config.class);
            } else
            {
                create();
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        onLoad();
    }
}
