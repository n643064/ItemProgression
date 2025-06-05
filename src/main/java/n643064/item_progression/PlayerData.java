package n643064.item_progression;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static n643064.item_progression.Main.MODID;

public class PlayerData extends SavedData
{
    protected static final String ID = MODID + ":PlayerData";
    protected static Factory<PlayerData> factory = new Factory<>(PlayerData::new, PlayerData::load, null);

    public HashMap<String, HashMap<String, Integer>> data = new HashMap<>();

    public static HashMap<String, Integer> safeGetSkillMap(ServerLevel level, String playerName)
    {
        return get(level).safeGetSkillMap(playerName);
    }

    public HashMap<String, Integer> safeGetSkillMap(String playerName)
    {
        if (data.containsKey(playerName))
            return data.get(playerName);
        final HashMap<String, Integer> skills = new HashMap<>();
        Config.SKILLS.keySet().forEach(k -> skills.put(k, 1));
        data.put(playerName, skills);
        return skills;
    }

    public void setSkillLevel(String playerName, String skillName, int level)
    {
        safeGetSkillMap(playerName).put(skillName, level);
        this.setDirty(true);
    }

    public static PlayerData get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(factory, ID);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        data.forEach((k, v) ->
        {
            final CompoundTag innerTag = new CompoundTag();
            v.forEach(innerTag::putInt);
            compoundTag.put(k, innerTag);
        });
        return compoundTag;
    }


    public static PlayerData load(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        PlayerData playerData = new PlayerData();
        compoundTag.getAllKeys().forEach(player ->
        {
            final HashMap<String, Integer> innerMap = new HashMap<>();
            final CompoundTag innerTag = compoundTag.getCompound(player);
            innerTag.getAllKeys().forEach(skill -> innerMap.put(skill, innerTag.getInt(skill)));
            playerData.data.put(player, innerMap);
        });
        /*
        playerData.data.forEach((v, k) ->
        {
            k.forEach((k2, v2) -> System.out.println(v + " " + k2 + ":" + v2));
        });

         */
        return playerData;
    }

}
