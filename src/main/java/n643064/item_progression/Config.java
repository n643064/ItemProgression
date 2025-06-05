package n643064.item_progression;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
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
        defaultItemMap.put("minecraft:shield", Map.of("Defense", 8));
        defaultItemMap.put("minecraft:ender_pearl", Map.of("Magic", 6));
        defaultItemMap.put("minecraft:bow", Map.of("Attack", 4, "Agility", 4));
        defaultItemMap.put("minecraft:crossbow", Map.of("Attack", 4, "Agility", 6));
        defaultItemMap.put("minecraft:iron_sword", Map.of("Attack", 8));
        defaultItemMap.put("minecraft:iron_axe", Map.of("Attack", 4, "Gathering", 8));
        defaultItemMap.put("minecraft:iron_pickaxe", Map.of("Mining", 8));
        defaultItemMap.put("minecraft:iron_shovel", Map.of("Gathering", 8));
        defaultItemMap.put("minecraft:iron_hoe", Map.of("Farming", 8));
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
