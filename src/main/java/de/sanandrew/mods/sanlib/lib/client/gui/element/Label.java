package de.sanandrew.mods.sanlib.lib.client.gui.element;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.client.util.GuiUtils;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Range;

@SuppressWarnings("WeakerAccess")
public class Label
        implements IGuiElement
{
    public static final ResourceLocation ID = new ResourceLocation("label");

    public BakedData data;
    protected boolean isVisible;
    protected IGuiElement visibleFor;

    @Override
    public void bakeData(IGui gui, JsonObject data) {
        if( this.data == null ) {
            this.data = new BakedData();
            this.data.size = JsonUtils.getIntArray(data.get("size"), Range.is(2));
            this.data.backgroundColor = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("backgroundColor"), "0xF0100010"));
            this.data.borderTopColor = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("borderTopColor"), "0x505000FF"));
            this.data.borderBottomColor = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("borderBottomColor"), "0x5028007F"));
            this.data.setPadding(JsonUtils.getIntArray(data.get("padding"), new int[0], Range.between(0, 4)));

            GuiElementInst elemFor = gui.getDefinition().getElementById(JsonUtils.getStringVal(data.get("for"), ""));
            this.visibleFor = elemFor == null ? null : elemFor.get();

            this.data.content = this.getLabel(gui, data);
        }
    }

    public GuiElementInst getLabel(IGui gui, JsonObject data) {
        GuiElementInst lbl = null;
        if( data.has("content") ) {
            lbl = JsonUtils.GSON.fromJson(data.get("content"), GuiElementInst.class);

        } else if( data.has("text") ) {
            lbl = new GuiElementInst();
            lbl.element = new Text();
            lbl.data = new JsonObject();
            JsonUtils.addJsonProperty(lbl.data, "text", JsonUtils.getStringVal(data.get("text")));
            JsonUtils.addJsonProperty(lbl.data, "color", "0xFFFFFFFF");
        } else {
            throw new JsonParseException("No data property called \"content\" or \"text\" has been found.");
        }

        gui.getDefinition().initElement(lbl);
        lbl.get().bakeData(gui, lbl.data);

        return lbl;
    }

    @Override
    public void update(IGui gui, JsonObject data) {
        this.data.content.get().update(gui, this.data.content.data);
    }

    @Override
    public void render(IGui gui, float partTicks, int x, int y, int mouseX, int mouseY, JsonObject data) {
        int locMouseX = mouseX - gui.getScreenPosX();
        int locMouseY = mouseY - gui.getScreenPosY();
        if( locMouseX >= x && locMouseX < x + this.data.size[0] && locMouseY >= y && locMouseY < y + this.data.size[1] ) {
            IGuiElement contentElem = this.data.content.get();
            int width = contentElem.getWidth() + this.data.padding[1] + this.data.padding[3];
            int height = contentElem.getHeight() + this.data.padding[0] + this.data.padding[2];
            int xPos = locMouseX + 12;
            int yPos = locMouseY - 12;

            if( mouseX + width + 16 > gui.get().width ) {
                xPos -= width + 28;
            }

            GlStateManager.disableDepth();
            Gui.drawRect(xPos - 3,         yPos - 4,          xPos + width + 3, yPos - 3,          this.data.backgroundColor);
            Gui.drawRect(xPos - 3,         yPos + height + 3, xPos + width + 3, yPos + height + 4, this.data.backgroundColor);
            Gui.drawRect(xPos - 3,         yPos - 3,          xPos + width + 3, yPos + height + 3, this.data.backgroundColor);
            Gui.drawRect(xPos - 4,         yPos - 3,          xPos - 3,         yPos + height + 3, this.data.backgroundColor);
            Gui.drawRect(xPos + width + 3, yPos - 3,          xPos + width + 4, yPos + height + 3, this.data.backgroundColor);

            GuiUtils.drawGradientRect(xPos - 3,         yPos - 2, 1, height + 4, this.data.borderTopColor, this.data.borderBottomColor, true);
            GuiUtils.drawGradientRect(xPos + width + 2, yPos - 2, 1, height + 4, this.data.borderTopColor, this.data.borderBottomColor, true);
            Gui.drawRect(xPos - 3, yPos - 3,          xPos + width + 3, yPos - 2,          this.data.borderTopColor);
            Gui.drawRect(xPos - 3, yPos + height + 2, xPos + width + 3, yPos + height + 3, this.data.borderBottomColor);

            contentElem.render(gui, partTicks,
                               xPos + this.data.padding[3] + this.data.content.pos[0], yPos + this.data.padding[0] + this.data.content.pos[1],
                               mouseX, mouseY,
                               data);

            GlStateManager.enableDepth();
        }
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return this.isVisible && (this.visibleFor == null || this.visibleFor.isVisible());
    }

    @Override
    public void setVisible(boolean visible) {
        this.isVisible = visible;
    }

    public static final class BakedData
    {
        public int[] size;
        public GuiElementInst content;
        public int backgroundColor;
        public int borderTopColor;
        public int borderBottomColor;
        public int[] padding;

        public void setPadding(int[] padding) {
            if( padding == null || padding.length == 0 ) {
                this.padding = new int[] { 0, 0, 0, 0 };
                return;
            }

            switch( padding.length ) {
                case 1: this.padding = new int[] { padding[0], padding[0], padding[0], padding[0] }; break;
                case 2: this.padding = new int[] { padding[0], padding[1], padding[0], padding[1] }; break;
                case 3: this.padding = new int[] { padding[0], padding[1], padding[2], padding[1] }; break;
                case 4: this.padding = new int[] { padding[0], padding[1], padding[2], padding[3] }; break;
            }
        }
    }
}
