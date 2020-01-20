package de.sanandrew.mods.sanlib.lib.client.gui;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@SuppressWarnings({ "RedundantThrows", "unused" })
public interface IGuiElement
{
    void bakeData(IGui gui, JsonObject data);

    default void update(IGui gui, JsonObject data) {}

    void render(IGui gui, float partTicks, int x, int y, int mouseX, int mouseY, JsonObject data);

    default void handleMouseInput(IGui gui) throws IOException { }

    default boolean mouseClicked(IGui gui, int mouseX, int mouseY, int mouseButton) throws IOException { return false; }

    default void mouseReleased(IGui gui, int mouseX, int mouseY, int state) { }

    default void mouseClickMove(IGui gui, int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) { }

    default void guiClosed(IGui gui) {}

    int getWidth();

    int getHeight();

    default boolean keyTyped(IGui gui, char typedChar, int keyCode) throws IOException { return false; }

    boolean isVisible();

    void setVisible(boolean visible);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Priorities
    {
        Priority[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Priorities.class)
    @Target(ElementType.TYPE)
    @interface Priority
    {
        EventPriority value();
        PriorityTarget target();
    }

    enum PriorityTarget
    {
        MOUSE_INPUT,
        KEY_INPUT;

        public static final PriorityTarget[] VALUES = values();
    }

    static boolean isHovering(IGui gui, int x, int y, int mouseX, int mouseY, int width, int height) {
        mouseX -= gui.getScreenPosX();
        mouseY -= gui.getScreenPosY();
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
