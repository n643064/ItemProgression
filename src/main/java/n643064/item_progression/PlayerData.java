package n643064.cotv.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import static n643064.cotv.Config.CONFIG;

public class Points extends SavedData
{
    protected static final String POINT_ID = "points";
    protected static final String ID = "cotv:points";
    protected int points = 0;

    protected static Factory<Points> factory = new Factory<>(Points::new, Points::load, null);

    public void updatePoints(int a)
    {
        points += a;
        if (points < 0)
            points = 0;
        setDirty(true);
        //System.out.println("update points " + points);
    }

    public static int get(ServerLevel level)
    {
        return level.getDataStorage().computeIfAbsent(factory, ID).points;
    }

    public static void add(ServerLevel level, int a)
    {
        final Points p = level.getDataStorage().computeIfAbsent(factory, ID);
        byte b = getTier(p.points);
        p.updatePoints(a);
        byte b2 = getTier(p.points);
        if (b != b2)
            level.players().forEach(player -> player.sendSystemMessage(Component.translatable("chat.cotv.tier_message_p1").append(String.valueOf(b2)).append(Component.translatable("chat.cotv.tier_message_p2"))));
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        compoundTag.putInt(POINT_ID, points);
        return compoundTag;
    }

    public static Points load(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider)
    {
        final Points p = new Points();
        p.updatePoints(compoundTag.getInt(POINT_ID));
        return p;
    }

    public static byte getTier(ServerLevel level)
    {
        return getTier(get(level));
    }

    protected static byte getTier(int points)
    {
        if (points >= CONFIG.pointsTier3())
            return 3;
        else if (points >= CONFIG.pointsTier2())
            return 2;
        else if (points >= CONFIG.pointsTier1())
            return 1;
        return 0;
    }


}
