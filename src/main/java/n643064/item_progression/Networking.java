package n643064.item_progression;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static n643064.item_progression.Main.MODID;

public class Networking
{

    public record SyncSkillsPayload(List<Config.Skill> skills) implements CustomPacketPayload
    {

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return SYNC_SKILLS;
        }
    }
    public static final CustomPacketPayload.Type<SyncSkillsPayload> SYNC_SKILLS = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_skills"));

    public record SyncItemsPayload(Config.ItemMap map) implements CustomPacketPayload
    {
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return SYNC_ITEMS;
        }
    }
    public static final CustomPacketPayload.Type<SyncItemsPayload> SYNC_ITEMS = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync_items"));

    public record SetSkillsPayload(HashMap<String, Integer> skills) implements CustomPacketPayload
    {

        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return SET_SKILLS;
        }
    }
    public static final CustomPacketPayload.Type<SetSkillsPayload> SET_SKILLS = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "set_skills"));

    public record RequestSkillIncreasePayload(String name) implements CustomPacketPayload
    {
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return REQUEST_SKILL_INCREASE;
        }
    }
    public static final CustomPacketPayload.Type<RequestSkillIncreasePayload> REQUEST_SKILL_INCREASE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "request_skill_increase"));

    public record SkillChangedPayload(String name, int newLevel) implements CustomPacketPayload
    {
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return SKILL_CHANGED;
        }
    }
    public static final CustomPacketPayload.Type<SkillChangedPayload> SKILL_CHANGED = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "skill_changed"));

    public record RequestUpdateSkills(List<String> names) implements CustomPacketPayload
    {
        @Override
        public @NotNull Type<? extends CustomPacketPayload> type()
        {
            return REQUEST_UPDATE_SKILLS;
        }
    }
    public static final CustomPacketPayload.Type<RequestUpdateSkills> REQUEST_UPDATE_SKILLS = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "request_update_skills"));


    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSkillsPayload> SYNC_SKILLS_CODEC = StreamCodec.of((buf, payload) ->
    {
        payload.skills().forEach(skill ->
        {
            buf.writeUtf(skill.name());
            buf.writeUtf(skill.iconItem());
            buf.writeInt(skill.maxLevel());
        });
    }, buf -> {
        final ArrayList<Config.Skill> s = new ArrayList<>();
        while (buf.readableBytes() != 0)
        {
            final String name = buf.readUtf();
            final String icon = buf.readUtf();
            final int maxLevel = buf.readInt();
            s.add(new Config.Skill(name, icon, maxLevel));
        }
        return new SyncSkillsPayload(s);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncItemsPayload> SYNC_ITEMS_CODEC = StreamCodec.of((buf, payload) ->
    {
        payload.map.forEach((k, v) ->
        {
            buf.writeUtf(k);
            buf.writeInt(v.size());
            v.forEach((k2, v2) ->
            {
                buf.writeUtf(k2);
                buf.writeInt(v2);
            });

        });
    }, buf -> {
        final Config.ItemMap map = new Config.ItemMap();
        while (buf.readableBytes() > 0)
        {
            final String k = buf.readUtf();
            final int size = buf.readInt();
            final HashMap<String, Integer> requirements = new HashMap<>();
            for (int i = 0; i < size; i++)
            {
                requirements.put(buf.readUtf(), buf.readInt());
            }
            map.put(k, requirements);
        }
        return new SyncItemsPayload(map);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, SetSkillsPayload> SET_SKILLS_CODEC = StreamCodec.of((buf, payload) ->
    {
        payload.skills().forEach((k, v) ->
        {
            buf.writeUtf(k);
            buf.writeInt(v);
        });
    }, buf -> {
        final HashMap<String, Integer> map = new HashMap<>();
        while (buf.readableBytes() != 0)
        {
            final String name = buf.readUtf();
            final int value = buf.readInt();
            map.put(name, value);
        }
        return new SetSkillsPayload(map);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSkillIncreasePayload> REQUEST_SKILL_INCREASE_CODEC = StreamCodec.of(
            (buf, payload) -> buf.writeUtf(payload.name),
            buf -> new RequestSkillIncreasePayload(buf.readUtf())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillChangedPayload> SKILL_CHANGED_CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeUtf(payload.name);
                buf.writeInt(payload.newLevel);
            },
            buf -> new SkillChangedPayload(buf.readUtf(), buf.readInt())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestUpdateSkills> REQUEST_UPDATE_SKILLS_CODEC = StreamCodec.of(
            (buf, payload) -> payload.names.forEach(buf::writeUtf),
            buf -> {
                final ArrayList<String> names = new ArrayList<>();
                while (buf.readableBytes() != 0)
                    names.add(buf.readUtf());
                return new RequestUpdateSkills(names);
            }
    );


}
