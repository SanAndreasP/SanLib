package de.sanandrew.mods.sanlib.lib.client.gui.element;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.apache.commons.lang3.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({ "WeakerAccess", "Duplicates", "unused" })
public class Button
        implements IGuiElement
{
    public static final ResourceLocation ID = new ResourceLocation("button");

    public BakedData data;

    protected int     currMouseX;
    protected int     currMouseY;
    protected boolean isCurrHovering;

    @Override
    public void bakeData(IGui gui, JsonObject data) {
        if( this.data == null ) {
            this.data = new BakedData();
            this.data.texture = new ResourceLocation(JsonUtils.getStringVal(data.get("texture"), "textures/gui/widgets.png"));
            this.data.size = JsonUtils.getIntArray(data.get("size"), Range.is(2));
            this.data.uvSize = JsonUtils.getIntArray(data.get("uvSize"), new int[] { 200, 20 }, Range.is(2));
            this.data.uvEnabled = JsonUtils.getIntArray(data.get("uvEnabled"), new int[] { 0, 66 }, Range.is(2));
            this.data.uvHover = JsonUtils.getIntArray(data.get("uvHover"), new int[] { this.data.uvEnabled[0], this.data.uvEnabled[1] + this.data.uvSize[1] }, Range.is(2));
            this.data.uvDisabled = JsonUtils.getIntArray(data.get("uvDisabled"), new int[] { this.data.uvEnabled[0], this.data.uvEnabled[1] - this.data.uvSize[1] }, Range.is(2));
            this.data.ctHorizontal = JsonUtils.getIntVal(data.get("ctHorizontal"), 190);
            this.data.ctVertical = JsonUtils.getIntVal(data.get("ctVertical"), 14);
            this.data.buttonFunction = JsonUtils.getIntVal(data.get("buttonFunction"));
            this.data.textureSize = JsonUtils.getIntArray(data.get("textureSize"), new int[] { 256, 256 }, Range.is(2));
            this.data.forceAlpha = JsonUtils.getBoolVal(data.get("forceAlpha"), false);

            JsonElement lbl = data.get("label");
            if( lbl != null ) {
                this.data.label = JsonUtils.GSON.fromJson(lbl, GuiElementInst.class);
                this.data.label.get().bakeData(gui, this.data.label.data);
            }
            this.data.centerLabel = JsonUtils.getBoolVal(data.get("centerLabel"), true);

            this.data.button = new GuiButton(this.data.buttonFunction, 0, 0, "");
        }
    }

    @Override
    public void render(IGui gui, float partTicks, int x, int y, int mouseX, int mouseY, JsonObject data) {
        if( this.isVisible() ) {
            this.currMouseX = mouseX;
            this.currMouseY = mouseY;

            this.isCurrHovering = this.isHovering(gui, x, y, mouseX, mouseY);
            boolean isEnabled = this.isEnabled();

            gui.get().mc.renderEngine.bindTexture(this.data.texture);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if( this.data.forceAlpha ) {
                GlStateManager.enableBlend();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }
            GlStateManager.translate(x, y, 0.0D);
            drawRect(isEnabled, this.isCurrHovering);
            GlStateManager.popMatrix();

            if( this.data.label != null ) {
                int lblX = this.data.label.pos[0];
                int lblY = this.data.label.pos[1];

                IButtonLabel labelElem = (IButtonLabel) this.data.label.get();
                if( this.data.centerLabel ) {
                    lblX = (this.data.size[0] - labelElem.getWidth()) / 2;
                    lblY = (this.data.size[1] - labelElem.getHeight() + 1) / 2;
                }

                labelElem.renderLabel(gui, partTicks, x + lblX, y + lblY, mouseX, mouseY, this.data.label.data, isEnabled, this.isCurrHovering);
            }
        }
    }

    @Override
    public boolean mouseClicked(IGui gui, int mouseX, int mouseY, int mouseButton) {
        if( mouseButton == 0 && this.isVisible() && this.isEnabled() && this.isCurrHovering ) {
            GuiScreen gs = gui.get();
            GuiButton btn = this.data.button;
            List<GuiButton> btnList = new ArrayList<>(Collections.singletonList(this.data.button));
            GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(gs, btn, btnList);
            if( MinecraftForge.EVENT_BUS.post(event) ) {
                return true;
            }
            btn = event.getButton();

            btn.playPressSound(gs.mc.getSoundHandler());
            gui.performAction(this, btn.id);
            if( gs.equals(gs.mc.currentScreen) ) {
                MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(gs, btn, btnList));
            }

            return true;
        }

        return false;
    }

    public boolean isHovering(IGui gui, int x, int y, int mouseX, int mouseY) {
        mouseX -= gui.getScreenPosX();
        mouseY -= gui.getScreenPosY();
        return mouseX >= x && mouseX < x + this.data.size[0] && mouseY >= y && mouseY < y + this.data.size[1];
    }

    protected void drawRect(boolean enabled, boolean hovered) {
        int[] uv = enabled
                   ? (hovered ? this.data.uvHover : this.data.uvEnabled)
                   : this.data.uvDisabled;

        if( this.data.uvSize[0] == this.data.size[0] && this.data.uvSize[1] == this.data.size[1] ) {
            Gui.drawModalRectWithCustomSizedTexture(0, 0, uv[0], uv[1], this.data.size[0], this.data.size[1], this.data.textureSize[0], this.data.textureSize[1]);
        } else {
            int cornerWidth = (this.data.uvSize[0] - this.data.ctHorizontal) / 2;
            int cornerHeight = (this.data.uvSize[1] - this.data.ctVertical) / 2;

            Gui.drawModalRectWithCustomSizedTexture(0, 0,
                                                    uv[0], uv[1],
                                                    cornerWidth, cornerHeight,
                                                    this.data.textureSize[0], this.data.textureSize[1]);
            Gui.drawModalRectWithCustomSizedTexture(0, this.data.size[1] - cornerHeight,
                                                    uv[0], uv[1] + this.data.uvSize[1] - cornerHeight,
                                                    cornerWidth, cornerHeight,
                                                    this.data.textureSize[0], this.data.textureSize[1]);
            Gui.drawModalRectWithCustomSizedTexture(this.data.size[0] - cornerWidth, 0,
                                                    uv[0] + this.data.uvSize[0] - cornerWidth, uv[1],
                                                    cornerWidth, cornerHeight,
                                                    this.data.textureSize[0], this.data.textureSize[1]);
            Gui.drawModalRectWithCustomSizedTexture(this.data.size[0] - cornerWidth, this.data.size[1] - cornerHeight,
                                                    uv[0] + this.data.uvSize[0] - cornerWidth, uv[1] + this.data.uvSize[1] - cornerHeight,
                                                    cornerWidth, cornerHeight,
                                                    this.data.textureSize[0], this.data.textureSize[1]);

            drawTiledTexture(0, cornerHeight,
                             uv[0], uv[1] + cornerHeight,
                             cornerWidth, this.data.uvSize[1] - cornerHeight * 2,
                             cornerWidth, this.data.size[1] - cornerHeight * 2,
                             this.data.textureSize[0], this.data.textureSize[1]);
            drawTiledTexture(cornerWidth, 0,
                             uv[0] + cornerWidth, uv[1],
                             this.data.uvSize[0] - cornerWidth * 2, cornerHeight,
                             this.data.size[0] - cornerWidth * 2, cornerHeight,
                             this.data.textureSize[0], this.data.textureSize[1]);
            drawTiledTexture(this.data.size[0] - cornerWidth, cornerHeight,
                             uv[0] + this.data.uvSize[0] - cornerWidth, uv[1] + cornerHeight,
                             cornerWidth, this.data.uvSize[1] - cornerHeight * 2,
                             cornerWidth, this.data.size[1] - cornerHeight * 2,
                             this.data.textureSize[0], this.data.textureSize[1]);
            drawTiledTexture(cornerWidth, this.data.size[1] - cornerHeight,
                             uv[0] + cornerWidth, uv[1] + this.data.uvSize[1] - cornerHeight,
                             this.data.uvSize[0] - cornerWidth * 2, cornerHeight,
                             this.data.size[0] - cornerWidth * 2, cornerHeight,
                             this.data.textureSize[0], this.data.textureSize[1]);

            drawTiledTexture(cornerWidth, cornerHeight,
                             uv[0] + cornerWidth, uv[1] + cornerHeight,
                             this.data.uvSize[0] - cornerWidth * 2, this.data.uvSize[1] - cornerHeight * 2,
                             this.data.size[0] - cornerWidth * 2, this.data.size[1] - cornerHeight * 2,
                             this.data.textureSize[0], this.data.textureSize[1]);
        }
    }

    protected static void drawTiledTexture(int x, int y, float u, float v, int uWidth, int vHeight, int width, int height, int sheetWidth, int sheetHeight) {
        int txWidth = width;
        int txHeight = height;
        int uvX = Math.min(uWidth, width);
        int uvY = Math.min(vHeight, height);
        while( uvX > 0 ) {
            while( uvY > 0 ) {
                Gui.drawModalRectWithCustomSizedTexture(x + txWidth - width, y + txHeight - height, u, v, uvX, uvY, sheetWidth, sheetHeight);

                height -= vHeight;
                uvY = Math.min(vHeight, height);
            }

            height = txHeight;
            uvY = Math.min(vHeight, height);

            width -= uWidth;
            uvX = Math.min(uWidth, width);
        }
    }

    @Override
    public int getWidth() {
        return this.data.size[0];
    }

    @Override
    public int getHeight() {
        return this.data.size[1];
    }

    public void setVisible(boolean visible) {
        this.data.button.visible = visible;
    }

    public boolean isVisible() {
        return this.data.button.visible;
    }

    public void setEnabled(boolean enabled) {
        this.data.button.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.data.button.enabled;
    }

    public static final class BakedData
    {
        public ResourceLocation texture;
        public int[]            size;
        public int[]            textureSize;
        public int[]            uvEnabled;
        public int[]            uvHover;
        public int[]            uvDisabled;
        public int[]            uvSize;
        public int              ctHorizontal;
        public int              ctVertical;
        public boolean          forceAlpha;
        public int              buttonFunction;

        public GuiButton button;

        public GuiElementInst label;
        public boolean        centerLabel;
    }
}
