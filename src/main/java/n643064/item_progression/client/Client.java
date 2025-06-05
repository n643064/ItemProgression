package n643064.item_progression.client;

import com.mojang.blaze3d.platform.InputConstants;
import n643064.item_progression.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

import static n643064.item_progression.Networking.*;

public class Client implements ClientModInitializer
{

    public static HashMap<String, Config.CachedSkill> CLIENT_CACHED_SKILLS = new HashMap<>();
    public static HashMap<String, Integer> CLIENT_CACHED_SKILL_MAP = new HashMap<>();
    public static Config.CachedItemMap CLIENT_CACHED_ITEM_MAP = new Config.CachedItemMap();

    @Nullable
    public static UsagePopup POPUP = null;

    public static KeyMapping OPEN_SKILLS_SCREEN = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.item_progression.open_skills",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Y,
            "key.categories.item_progression"
    ));
    @Override
    public void onInitializeClient()
    {
        HudRenderCallback.EVENT.register(((guiGraphics, deltaTracker) ->
        {
            if (POPUP != null)
                POPUP.render(guiGraphics);
        }));


        ClientPlayNetworking.registerGlobalReceiver(SYNC_SKILLS, ((payload, context) ->
        {
            CLIENT_CACHED_SKILLS.clear();
            payload.skills().forEach(skill -> CLIENT_CACHED_SKILLS.put(skill.name(), new Config.CachedSkill(BuiltInRegistries.ITEM.get(ResourceLocation.parse(skill.iconItem())), skill.maxLevel())));
        }));

        ClientPlayNetworking.registerGlobalReceiver(SKILL_CHANGED, ((payload, context) ->
                CLIENT_CACHED_SKILL_MAP.put(payload.name(), payload.newLevel())));

        ClientPlayNetworking.registerGlobalReceiver(SET_SKILLS, ((payload, context) ->
                CLIENT_CACHED_SKILL_MAP = payload.skills()));

        ClientPlayNetworking.registerGlobalReceiver(SYNC_ITEMS, ((payload, context) ->
                CLIENT_CACHED_ITEM_MAP = Config.generateItemCache(payload.map())));

        ClientTickEvents.END_CLIENT_TICK.register(minecraft ->
        {
            if (OPEN_SKILLS_SCREEN.consumeClick() && minecraft.screen == null)
            {
                minecraft.setScreen(new SkillScreen(null));
            }
        });




    }
}
