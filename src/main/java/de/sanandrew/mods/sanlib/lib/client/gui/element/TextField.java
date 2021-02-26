////////////////////////////////////////////////////////////////////////////////
// This file is subject to the terms and conditions defined in the             /
// file '.github/LICENSE.md', which is part of this source code package.       /
////////////////////////////////////////////////////////////////////////////////

package de.sanandrew.mods.sanlib.lib.client.gui.element;

import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import de.sanandrew.mods.sanlib.lib.client.gui.GuiElementInst;
import de.sanandrew.mods.sanlib.lib.client.gui.IGui;
import de.sanandrew.mods.sanlib.lib.client.gui.IGuiElement;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import de.sanandrew.mods.sanlib.lib.util.LangUtils;
import de.sanandrew.mods.sanlib.lib.util.MiscUtils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.EventPriority;
import org.apache.commons.lang3.Range;

import java.util.function.Consumer;
import java.util.function.Predicate;

@SuppressWarnings({ "unused", "WeakerAccess" })
@IGuiElement.Priority(value = EventPriority.HIGHEST, target = IGuiElement.PriorityTarget.KEY_INPUT)
public class TextField
        implements IGuiElement
{
    public static final ResourceLocation ID = new ResourceLocation("textfield");

    private boolean enabled = true;
    
    public int[]        size;
    public int          color;
    public int          disabledColor;
    public int          placeholderColor;
    public boolean      canLoseFocus;
    public boolean      drawBackground;
    public boolean      shadow;
    public String       text;
    public String       placeholderText;
    public FontRenderer fontRenderer;

    public TextFieldWidget textfield;

    private int maxStringLength;

    @Override
    public void bakeData(IGui gui, JsonObject data, GuiElementInst inst) {
        this.size = JsonUtils.getIntArray(data.get("size"), Range.is(2));
        this.text = JsonUtils.getStringVal(data.get("text"), "");
        this.placeholderText = JsonUtils.getStringVal(data.get("placeholderText"), "");
//        this.shadow = JsonUtils.getBoolVal(data.get("shadow"), true);
        this.canLoseFocus = JsonUtils.getBoolVal(data.get("canLoseFocus"), true);
        this.drawBackground = JsonUtils.getBoolVal(data.get("drawBackground"), true);
        this.color = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("textColor"), "0xFFE0E0E0"));
        this.placeholderColor = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("placeholderColor"), "0xFF707070"));
        this.disabledColor = MiscUtils.hexToInt(JsonUtils.getStringVal(data.get("disabledTextColor"), "0xFF707070"));

        JsonElement cstFont = data.get("font");
        if( cstFont == null ) {
            this.fontRenderer = new Text.Font("standard").get(gui.get());
        } else {
            this.fontRenderer = JsonUtils.GSON.fromJson(cstFont, Text.Font.class).get(gui.get());
        }

//        this.textfield = new TextFieldWidget(0, new FontRendererTF(gui.get().mc, this.fontRenderer), 0, 0, this.size[0], this.size[1]);
        this.textfield = new TextFieldWidget(this.fontRenderer, 0, 0, 0, 0, new StringTextComponent(""));
        this.textfield.setText(LangUtils.translate(this.text));
        this.textfield.setTextColor(this.color);
        this.textfield.setDisabledTextColour(this.disabledColor);
        this.textfield.setCanLoseFocus(this.canLoseFocus);
        this.textfield.setEnableBackgroundDrawing(this.drawBackground);
    }

    @Override
    public void update(IGui gui, JsonObject data) {
        this.textfield.tick();
    }

    @Override
    public void render(IGui gui, MatrixStack stack, float partTicks, int x, int y, double mouseX, double mouseY, JsonObject data) {
        this.textfield.x = x;
        this.textfield.y = y;
        this.textfield.renderButton(stack, x, y, partTicks);
        if( !this.isFocused() && !Strings.isNullOrEmpty(this.placeholderText) && Strings.isNullOrEmpty(this.getText()) ) {
            x += (this.drawBackground ? 4 : 0);
            y += (this.drawBackground ? (this.size[1] - 8) / 2 : 0);
            this.fontRenderer.drawString(stack, LangUtils.translate(this.placeholderText), x, y, this.placeholderColor);
        }
    }

    @Override
    public boolean mouseClicked(IGui gui, double mouseX, double mouseY, int mouseButton) {
        return this.textfield.mouseClicked(mouseX - gui.getScreenPosX(), mouseY - gui.getScreenPosY(), mouseButton);
    }

    @Override
    public boolean keyPressed(IGui gui, int keyCode, int u1, int u2) {
        return this.textfield.keyPressed(keyCode, u1, u2);
    }

    public void setText(String text) {
        this.textfield.setText(text);
    }

    public String getText() {
        return this.textfield.getText();
    }

    public void setMaxStringLength(int length) {
        this.textfield.setMaxStringLength(this.maxStringLength = length);
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public void setFocused(boolean isFocused) {
        this.textfield.setFocused2(isFocused);
    }

    public boolean isFocused() {
        return this.textfield.isFocused();
    }

    public void setCursorPosition(int pos) {
        this.textfield.setCursorPosition(pos);
    }

    public void setSelectionEnd(int pos) {
        this.textfield.setSelectionPos(pos);
    }

//    public int[] getSelectionPos() {
//        return new int[] { this.textfield.getCursorPosition(), this.textfield.getSelectedText() };
//    }

    public String getSelectedText() {
        return this.textfield.getSelectedText();
    }

    public void insertText(String text) {
        this.textfield.writeText(text);
    }

    public void setCursorPositionEnd() {
        this.textfield.setCursorPositionEnd();
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.textfield.setEnabled(enabled);
        this.enabled = enabled;
    }

    @Override
    public boolean isVisible() {
        return this.textfield.getVisible();
    }

    public void setValidator(Predicate<String> validator) {
        this.textfield.setValidator(validator);
    }

    public void setResponder(Consumer<String> responder) {
        this.textfield.setResponder(responder);
    }

    @Override
    public int getWidth() {
        return this.textfield.getWidth();
    }

    @Override
    public int getHeight() {
        return this.size[1] - (this.drawBackground ? 8 : 0);
    }

    //TODO: maybe needed???
//    private final class FontRendererTF
//            extends FontRenderer
//    {
//        private final FontRenderer fontRenderer;
//
//        private FontRendererTF(Minecraft mc, FontRenderer orig) {
//            super(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), mc.renderEngine, false);
//            this.fontRenderer = orig;
//        }
//
//        public void onResourceManagerReload(IResourceManager resourceManager) {
//            this.fontRenderer.onResourceManagerReload(resourceManager);
//        }
//
//        public int drawStringWithShadow(String text, float x, float y, int color) {
//            return this.drawString(text, x, y, color, TextField.this.shadow);
//        }
//
//        public int drawString(String text, int x, int y, int color) {
//            return this.fontRenderer.drawString(text, x, y, color);
//        }
//
//        public int drawString(String text, float x, float y, int color, boolean dropShadow) {
//            return this.fontRenderer.drawString(text, x, y, color, dropShadow);
//        }
//
//        public int getStringWidth(String text) {
//            return this.fontRenderer.getStringWidth(text);
//        }
//
//        public int getCharWidth(char character) {
//            return this.fontRenderer.getCharWidth(character);
//        }
//
//        public String trimStringToWidth(String text, int width) {
//            return this.fontRenderer.trimStringToWidth(text, width);
//        }
//
//        public String trimStringToWidth(String text, int width, boolean reverse) {
//            return this.fontRenderer.trimStringToWidth(text, width, reverse);
//        }
//
//        public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
//            this.fontRenderer.drawSplitString(str, x, y, wrapWidth, textColor);
//        }
//
//        public int getWordWrappedHeight(String str, int maxLength) {
//            return this.fontRenderer.getWordWrappedHeight(str, maxLength);
//        }
//
//        public void setUnicodeFlag(boolean unicodeFlagIn) {
//            this.fontRenderer.setUnicodeFlag(unicodeFlagIn);
//        }
//
//        public boolean getUnicodeFlag() {
//            return this.fontRenderer.getUnicodeFlag();
//        }
//
//        public void setBidiFlag(boolean bidiFlagIn) {
//            this.fontRenderer.setBidiFlag(bidiFlagIn);
//        }
//
//        public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
//            return this.fontRenderer.listFormattedStringToWidth(str, wrapWidth);
//        }
//
//        public boolean getBidiFlag() {
//            return this.fontRenderer.getBidiFlag();
//        }
//
//        public int getColorCode(char character) {
//            return this.fontRenderer.getColorCode(character);
//        }
//    }
}
