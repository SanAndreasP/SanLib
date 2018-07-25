/* ******************************************************************************************************************
 * Authors:   SanAndreasP
 * Copyright: SanAndreasP
 * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
 *                http://creativecommons.org/licenses/by-nc-sa/4.0/
 *******************************************************************************************************************/
package de.sanandrew.mods.sanlib.client.lexicon;

import de.sanandrew.mods.sanlib.SanLib;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexicon;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconGroup;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconInst;
import de.sanandrew.mods.sanlib.api.client.lexicon.ILexiconPageRender;
import joptsimple.internal.Strings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public final class LexiconInstance
        implements ILexiconInst
{
    private final Map<String, ILexiconGroup> idToGroupMap;
    private final List<ILexiconGroup> groups;
    private final List<ILexiconGroup> groupsRO;
    private final Map<String, ILexiconPageRender> idToPageRenderMap;
    private final ILexicon lexiconDef;

    public static final String RENDER_ID_CRAFTING = SanLib.ID + ".crafting";
    public static final String RENDER_ID_STANDARD = SanLib.ID + ".standard";

    LexiconInstance(ILexicon lexiconDef) {
        this.idToGroupMap = new HashMap<>();
        this.groups = new ArrayList<>();
        this.groupsRO = Collections.unmodifiableList(this.groups);
        this.idToPageRenderMap = new HashMap<>();
        this.lexiconDef = lexiconDef;

        this.registerPageRender(new LexiconRenderCraftingGrid());
    }

    @Override
    public boolean registerGroup(ILexiconGroup group) {
        if( group == null ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register null as lexicon group for mod %s!", this.lexiconDef.getModId()));
            return false;
        }

        String id = group.getId();
        if( Strings.isNullOrEmpty(id) ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register a lexicon group without ID for mod %s!", this.lexiconDef.getModId()));
            return false;
        }
        if( this.idToGroupMap.containsKey(id) ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register a lexicon group with an already registered ID => \"%s\" for mod %s!", id, this.lexiconDef.getModId()));
            return false;
        }

        this.idToGroupMap.put(id, group);
        this.groups.add(group);

        return true;
    }

    @Override
    public List<ILexiconGroup> getGroups() {
        return this.groupsRO;
    }

    @Override
    public ILexiconGroup getGroup(String id) {
        return this.idToGroupMap.get(id);
    }

    @Override
    public ILexiconGroup removeGroup(String id) {
        ILexiconGroup group = this.idToGroupMap.get(id);
        if( group != null ) {
            this.idToGroupMap.remove(id);
            this.groups.remove(group);
        }
        return group;
    }

    @Override
    public boolean registerPageRender(ILexiconPageRender render) {
        if( render == null ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register null as lexicon page render for mod %s!", this.lexiconDef.getModId()));
            return false;
        }

        String id = render.getId();
        if( Strings.isNullOrEmpty(id) ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register a lexicon page render without ID for mod %s!", this.lexiconDef.getModId()));
            return false;
        }
        if( this.idToGroupMap.containsKey(id) ) {
            SanLib.LOG.log(Level.ERROR, String.format("Cannot register a lexicon page render with an already registered ID => \"%s\" for mod %s!", id, this.lexiconDef.getModId()));
            return false;
        }

        this.idToPageRenderMap.put(id, render);

        return true;
    }

    @Override
    public ILexiconPageRender getPageRender(String id) {
        return this.idToPageRenderMap.get(id);
    }

    @Override
    public ILexiconPageRender removePageRender(String id) {
        return this.idToPageRenderMap.remove(id);
    }

    @Override
    public ILexicon getLexicon() {
        return this.lexiconDef;
    }

    @Override
    public String getCraftingRenderID() {
        return RENDER_ID_CRAFTING;
    }

    @Override
    public String getStandardRenderID() {
        return RENDER_ID_STANDARD;
    }
}