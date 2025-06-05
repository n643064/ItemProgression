package n643064.item_progression.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class UsagePopup
{
    int renderTicks;
    private final Font font;
    public UsagePopup(Minecraft mc, int seconds)
    {
        this.renderTicks = mc.getFps() * seconds;
        this.font = mc.font;
    }

    public void render(GuiGraphics guiGraphics)
    {
        if (renderTicks == 0)
            Client.POPUP = null;
        final int center = guiGraphics.guiWidth() / 2;
        final int fourth = guiGraphics.guiHeight() / 4;

        guiGraphics.drawCenteredString(font, "You do not meet the requirements to use this item", center, fourth, 0xFFAAAA);
        renderTicks--;
    }
}
