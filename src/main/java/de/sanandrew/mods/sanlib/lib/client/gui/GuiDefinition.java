package de.sanandrew.mods.sanlib.lib.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.sanandrew.mods.sanlib.SanLib;
import de.sanandrew.mods.sanlib.lib.util.JsonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "WeakerAccess", "MismatchedQueryAndUpdateOfCollection"})
public class GuiDefinition
        implements ISelectiveResourceReloadListener
{
    static final Map<ResourceLocation, Supplier<IGuiElement>> TYPES = new HashMap<>();
    static {
        TYPES.put(TextGuiElement.ID, TextGuiElement::new);
        TYPES.put(TextureGuiElement.ID, TextureGuiElement::new);
        TYPES.put(RectangleGuiElement.ID, RectangleGuiElement::new);
        TYPES.put(ScrollAreaGuiElement.ID, ScrollAreaGuiElement::new);
    }

    public int width;
    public int height;

    GuiElementInst[] foregroundElements;
    GuiElementInst[] backgroundElements;

    private Map<Integer, Button> buttons;

    private final ResourceLocation data;

    private GuiDefinition(ResourceLocation data) throws IOException {
        this.data = data;
        ((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(this);
        this.reloadDefinition();
    }

    public static GuiDefinition getNewDefinition(ResourceLocation data) throws IOException {
        return new GuiDefinition(data);
    }

    private void reloadDefinition() throws IOException {
        try(IResource r = Minecraft.getInstance().getResourceManager().getResource(this.data);
            InputStreamReader reader = new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8) )
        {
            JsonElement json = new JsonParser().parse(reader);
            if( !json.isJsonObject() ) {
                throw new IOException(String.format("Cannot read JSON of data-driven GUI %s as it isn't an object", this.data));
            }
            JsonObject jObj = json.getAsJsonObject();

            this.width = JsonUtils.getIntVal(jObj.get("width"));
            this.height = JsonUtils.getIntVal(jObj.get("height"));

            this.backgroundElements = JsonUtils.GSON.fromJson(jObj.get("backgroundElements"), GuiElementInst[].class);
            this.foregroundElements = JsonUtils.GSON.fromJson(jObj.get("foregroundElements"), GuiElementInst[].class);
        }
    }

    public void initGui(IGui gui) {
        Consumer<GuiElementInst> f = e -> e.get().bakeData(gui, e.data);
        Arrays.stream(this.backgroundElements).forEach(f);
        Arrays.stream(this.foregroundElements).forEach(f);
    }

    public void drawBackground(IGui gui, int mouseX, int mouseY, float partialTicks) {
        Arrays.stream(this.backgroundElements).forEach(e -> e.get().render(gui, partialTicks, e.pos[0], e.pos[1], mouseX, mouseY, e.data));
    }

    public void drawForeground(IGui gui, int mouseX, int mouseY, float partialTicks) {
        Arrays.stream(this.foregroundElements).forEach(e -> e.get().render(gui, partialTicks, e.pos[0], e.pos[1], mouseX, mouseY, e.data));
    }

    public void onMouseScroll(IGui gui, double scroll) {
        Function<GuiElementInst, Boolean> f = e -> e.get().onMouseScroll(gui, scroll);

        for( GuiElementInst elem : this.backgroundElements) {
            if( f.apply(elem) ) {
                break;
            }
        }
        for( GuiElementInst elem : this.foregroundElements) {
            if( f.apply(elem) ) {
                break;
            }
        }
    }

    public GuiButton injectData(GuiButton button) {
        Button btn = this.buttons == null ? null : this.buttons.get(button.id);

        if( btn != null ) {
            button.x = btn.x;
            button.y = btn.y;
            button.width = btn.width;
            button.height = btn.height;
        }

        return button;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        try {
            this.reloadDefinition();
        } catch( IOException ex ) {
            SanLib.LOG.log(Level.ERROR, "Error whilst reloading GUI definition", ex);
        }
    }

    static final class Button
    {
        int x;
        int y;
        int width;
        int height;
    }

    @SuppressWarnings("ExceptionClassNameDoesntEndWithException")
    private static class IOExceptionWrapper
            extends RuntimeException
    {
        private static final long serialVersionUID = 8878021439168468744L;

        public final IOException ioex;

        IOExceptionWrapper(IOException ex) {
            this.ioex = ex;
        }
    }
}
