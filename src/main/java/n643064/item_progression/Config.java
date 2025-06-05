package n643064.item_progression;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.minecraft.world.level.Level.OVERWORLD;

public record Config(
        int infectionDuration,
        int explosionTime,
        int pointsLostOnDeath,
        int pointsPerEntity,
        int pointsPerPlayer,
        int pointsPerCreatureSpawn,
        int pointsTier1,
        int pointsTier2,
        int pointsTier3,
        int evolutionTime,
        int pointsReducedByDetergent,
        int nodeSpreadCooldown,
        int nodeMaxRangeSqr,
        int nodeMaxSteps,
        int nodeSpreadCount,
        double minHealthTier1,
        double minHealthTier2,
        double minHealthTier3,
        double fireDamageMult,
        double onFireMult,
        double infectionSpawnChance,
        double infectChance,
        double explosionChance,
        double explosionGrubChance,
        double onKillSpawnChance,
        double explosionRadiusHealthDiv,
        double spawnWeightMultiplier,
        double maxHardnessForSoftBlock,
        double maxHardnessForConversion,
        boolean someSensitiveToWater,
        boolean avoidIgnitedCreepers,
        boolean infectionEnabled,
        boolean mustSee,
        boolean targetKiller,
        boolean attackWithoutLOS,
        boolean scaleExplosionRadius,
        boolean despawnInPeaceful,
        boolean rideBoats,
        boolean infectionSpawnOnFinish,
        boolean grubsInfestInsteadOfAttacking,
        boolean creaturesArePersistentByDefault,
        List<String> infectionBlacklist,
        List<String> tier1Evolution,
        List<String> tier2Evolution,
        List<String> tier3Evolution,
        List<String> blockConversionBlacklist,
        int infectionClassDepth
)
{

    public static Config CONFIG = new Config(
            1200,
            30,
            1,
            2,
            4,
            1,
            400,
            800,
            1600,
            40,
            50,
            60,
            6144,
            128,
            81,
            20,
            30,
            40,
            2,
            2,
            0.8,
            1,
            0.5,
            0.3,
            1,
            15,
            1,
            1,
            10,
            true,
            true,
            true,
            false,
            true,
            true,
            true,
            true,
            false,
            true,
            true,
            true,
            List.of(
                    "minecraft:ender_dragon",
                    "minecraft:slime",
                    "minecraft:magma_cube"
            ),
            List.of(
                    "cotv:walker",
                    "cotv:node"
            ),
            List.of(
            ),
            List.of(
            ),
            List.of(
            ),
            2
    );


    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().setLenient().create();
    static final String CONFIG_PATH = "config" + File.separator + "cotv.json";

    public static final Set<EntityType<?>> INFECTION_BLACKLIST = new HashSet<>();
    public static final List<EntityType<?>> TIER_1_EVOLUTION = new ArrayList<>();
    public static final List<EntityType<?>> TIER_2_EVOLUTION = new ArrayList<>();
    public static final List<EntityType<?>> TIER_3_EVOLUTION = new ArrayList<>();
    public static final Set<Block> BLOCK_BLACKLIST = new HashSet<>();


    public static void generateCaches(MinecraftServer server, @Nullable ServerLevel level)
    {
        if (level != null && level.dimension() != OVERWORLD)
            return;

        for (String s : CONFIG.infectionBlacklist)
            INFECTION_BLACKLIST.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(s)));

        TIER_1_EVOLUTION.clear();
        for (String s : CONFIG.tier1Evolution)
            TIER_1_EVOLUTION.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(s)));
        TIER_2_EVOLUTION.clear();
        for (String s : CONFIG.tier2Evolution)
            TIER_2_EVOLUTION.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(s)));
        TIER_3_EVOLUTION.clear();
        for (String s : CONFIG.tier3Evolution)
            TIER_3_EVOLUTION.add(BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(s)));
        BLOCK_BLACKLIST.clear();
        for (String s : CONFIG.blockConversionBlacklist)
            BLOCK_BLACKLIST.add(BuiltInRegistries.BLOCK.get(ResourceLocation.parse(s)));

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
