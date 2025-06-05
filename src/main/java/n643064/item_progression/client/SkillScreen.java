package n643064.item_progression.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import static n643064.item_progression.Main.MODID;

public class SkillScreen extends Screen
{
    @Nullable private final Screen previousScreen;

    private int leftPos, topPos, imageWidth, imageHeight;
    private static final ResourceLocation BACKGROUND_LOCATION = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/skills.png");

    private static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_background");
    private static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/experience_bar_progress");

    protected SkillScreen(@Nullable Screen previousScreen)
    {
        super(Component.translatable("menu.item_progression.skills"));
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init()
    {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        final int l = Client.CLIENT_CACHED_SKILLS.size();
        int c = (width - l * 58) / 2;
        for (String k : Client.CLIENT_CACHED_SKILLS.keySet().stream().sorted().toList())
        {
            addRenderableWidget(new SkillWidget(k, c, 10, 48, 96, minecraft, font));
            c += 58;
        }
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f)
    {
        super.render(guiGraphics, i, j, f);

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f)
    {
        renderTransparentBackground(guiGraphics);
        renderBg(guiGraphics);
        renderExp(guiGraphics);
    }

    private void renderBg(GuiGraphics guiGraphics)
    {
        //guiGraphics.blit(BACKGROUND_LOCATION, this, l, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.setScreen(previousScreen);
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f, double g)
    {
        System.out.println(d + " " + e + " " + f + " " + g);
        return super.mouseScrolled(d, e, f, g);
    }

    private void renderExp(GuiGraphics guiGraphics)
    {
        assert this.minecraft != null;
        assert this.minecraft.player != null;
        final int center = width / 2;
        int j = this.minecraft.player.getXpNeededForNextLevel();
        RenderSystem.enableBlend();
        if (j > 0)
        {
            int l = (int) (this.minecraft.player.experienceProgress * 183f);
            guiGraphics.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, center - 91, height - 29, 182, 5);

            if (l > 0)
                guiGraphics.blitSprite(EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, center - 91, height - 29, l, 5);
        }

        final String level = "" + this.minecraft.player.experienceLevel;
        guiGraphics.drawCenteredString(font, level, center, height - 35, 0x00FF00);
        RenderSystem.disableBlend();

    }
}
