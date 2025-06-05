package n643064.item_progression;

import n643064.item_progression.client.Client;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class Util
{
    public static boolean checkItemRestricted(Config.CachedItemMap map, HashMap<String, Integer> skillMap, Item item)
    {
        if (!map.containsKey(item))
            return false;
        final Map<String, Integer> map2 = map.get(item);
        for (String k : map2.keySet())
        {
            if (map2.get(k) > skillMap.get(k))
                return true;
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static boolean clientCheckItemRestricted(Item item)
    {
        final Player player = Minecraft.getInstance().player;
        assert player != null;
        if (player.isCreative())
            return false;
        return checkItemRestricted(Client.CLIENT_CACHED_ITEM_MAP, Client.CLIENT_CACHED_SKILL_MAP, item);
    }

    @Environment(EnvType.SERVER)
    public static boolean serverCheckItemRestricted(ServerPlayer player, Item item)
    {
        if (player.isCreative())
            return false;
        final PlayerData pd = PlayerData.get(player.server.overworld());
        return checkItemRestricted(Config.CACHED_ITEMS, pd.safeGetSkillMap(player.getGameProfile().getName()), item);
    }
}
