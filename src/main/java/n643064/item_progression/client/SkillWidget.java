package n643064.item_progression.client;

import n643064.item_progression.Networking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static n643064.item_progression.Main.MODID;

public class SkillWidget extends ImageButton
{
    private static final WidgetSprites sprites = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(MODID, "widget/skill_enabled"),
            ResourceLocation.fromNamespaceAndPath(MODID, "widget/skill_disabled"),
            ResourceLocation.fromNamespaceAndPath(MODID, "widget/skill_enabled_hovered"),
            ResourceLocation.fromNamespaceAndPath(MODID, "widget/skill_disabled_hovered")
    );
    private final String name;
    private final ItemStack stack;
    private final Minecraft mc;
    private final Font font;
    private final int maxLevel;
    public SkillWidget(String name, int i, int j, int k, int l, Minecraft mc, Font font)
    {
        super(i, j, k, l, sprites, button ->
        {
            ClientPlayNetworking.send(new Networking.RequestSkillIncreasePayload(name));
        });
        this.name = name;
        this.mc = mc;
        stack = new ItemStack(Client.CLIENT_CACHED_SKILLS.get(name).iconItem());
        this.font = font;
        maxLevel = Client.CLIENT_CACHED_SKILLS.get(name).maxLevel();
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f)
    {
        final boolean isActive = isActive();
        final int centerX = getX() + width / 2;
        final int centerY = getY() + height / 2;
        ResourceLocation resourceLocation = sprites.get(isActive, isHovered);
        guiGraphics.blitSprite(resourceLocation, getX(), getY(), width, height);
        guiGraphics.drawCenteredString(font, name, centerX, centerY - 16 , 0xFFFFFF);
        guiGraphics.drawCenteredString(font, Client.CLIENT_CACHED_SKILL_MAP.get(name) + " / " + maxLevel, centerX, centerY + 16, isActive ? 0xAAFFAA : 0xFFAAAA);
        guiGraphics.renderFakeItem(stack, centerX - 8, centerY - 8);

        // Too lazy to do caching here, and it doesn't really impact performance since it only runs when the screen is open
    }

    @Override
    public boolean isActive()
    {
        assert mc.player != null;
        final int l = Client.CLIENT_CACHED_SKILL_MAP.get(name);
        return l < maxLevel && mc.player.experienceLevel > l;
    }
}
