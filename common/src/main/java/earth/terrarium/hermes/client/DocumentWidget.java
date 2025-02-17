package earth.terrarium.hermes.client;

import com.teamresourceful.resourcefullib.client.utils.RenderUtils;
import earth.terrarium.hermes.api.TagElement;
import earth.terrarium.hermes.api.themes.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DocumentWidget extends AbstractContainerEventHandler implements Renderable {

    private final List<TagElement> elements = new ArrayList<>();
    private final Theme theme;

    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final double overscrollTop;
    private final double overscrollBottom;

    private double scrollAmount;
    private int lastFullHeight;

    //We have to defer the mouse click until during render because we don't know the height of the document until then.
    private DocumentMouse mouse = null;

    public DocumentWidget(int x, int y, int width, int height, double overscrollTop, double overscrollBottom, Theme theme, List<TagElement> elements) {
        this.x = x;
        this.y = y;
        this.width = width - 6;
        this.height = height - 6;
        this.overscrollTop = overscrollTop;
        this.overscrollBottom = overscrollBottom;
        this.lastFullHeight = this.height;
        this.theme = theme;
        this.elements.addAll(elements);
        this.scrollAmount = -overscrollTop;
    }

    public DocumentWidget(int x, int y, int width, int height, Theme theme, List<TagElement> elements) {
        this(x, y, width, height, 0.0D, 0.0D, theme, elements);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.x;
        int y = this.y;

        int fullHeight = 0;
        try (var ignored = RenderUtils.createScissor(Minecraft.getInstance(), graphics, x, y, width, height)) {
            for (TagElement element : this.elements) {
                if (this.mouse != null && element.mouseClicked(this.mouse.x() - x, this.mouse.y() - (y - this.scrollAmount), this.mouse.button(), this.width)) {
                    this.mouse = null;
                }
                element.render(this.theme, graphics, x, y - (int) this.scrollAmount, this.width, mouseX, mouseY, this.isMouseOver(mouseX, mouseY), partialTicks);
                var itemheight = element.getHeight(this.width);
                y += itemheight;
                fullHeight += itemheight;
            }
            this.mouse = null;
            this.lastFullHeight = fullHeight;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollAmount) {
        this.scrollAmount = Mth.clamp(this.scrollAmount - scrollAmount * 10, -overscrollTop, Math.max(-overscrollTop, this.lastFullHeight - this.height + overscrollBottom));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isMouseOver(mouseX, mouseY)) {
            this.mouse = new DocumentMouse(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children() {
        return List.of();
    }
}
