/* ******************************************************************************************************************
   * Authors:   SanAndreasP
   * Copyright: SanAndreasP
   * License:   Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International
   *                http://creativecommons.org/licenses/by-nc-sa/4.0/
   *******************************************************************************************************************/
package de.sanandrew.mods.sanlib.lib.util;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;
import java.util.Objects;

/**
 * An utility class for ItemStacks
 */
@SuppressWarnings("unused")
public final class ItemStackUtils
{
    /**
     * Checks if an ItemStack is a valid stack.
     * <p>An ItemStack is valid, if:
     * <ul>
     *     <li>the stack is not {@code null}</li>
     *     <li>the item of the stack is not {@code null}</li>
     *     <li>the item is not {@link Blocks#AIR}</li>
     *     <li>the stack size is &gt; 0</li>
     * </ul>
     * </p>
     * @param stack The ItemStack to be checked.
     * @return {@code true}, if the stack is valid, {@code false} otherwise
     */
    public static boolean isValid(ItemStack stack) {
        //noinspection ConstantConditions
        return stack != null && stack.getItem() != null && Block.getBlockFromItem(stack.getItem()) != Blocks.AIR && stack.stackSize > 0;
    }

    /**
     * Checks if an ItemStack is valid and contains a specific item.
     * @param stack The ItemStack to be checked.
     * @param item The item that should be held in the ItemStack
     * @return {@code true}, if the stack is valid and contains the specified item, {@code false} otherwise
     * @see #isValid(ItemStack)
     */
    public static boolean isItem(ItemStack stack, Item item) {
        return isValid(stack) && stack.getItem() == item;
    }

    /**
     * Checks if an ItemStack is valid and contains a specific block.
     * @param stack The ItemStack to be checked.
     * @param block The block that should be held in the ItemStack
     * @return {@code true}, if the stack is valid and contains the specified block, {@code false} otherwise
     * @see #isValid(ItemStack)
     */
    public static boolean isBlock(ItemStack stack, Block block) {
        return isValid(stack) && Block.getBlockFromItem(stack.getItem()) == block;
    }

    /**
     * Checks wether or not 2 ItemStacks are equal. This does not compare stack sizes! This compares NBTTagCompounds!
     * @param is1 The first ItemStack
     * @param is2 The second ItemStack
     * @return {@code true}, if the stacks are considered equal, {@code false} otherwise
     */
    public static boolean areEqual(ItemStack is1, ItemStack is2) {
        return areEqual(is1, is2, false, true);
    }

    /**
     * Checks wether or not 2 ItemStacks are equal. This does not compare stack sizes!
     * @param is1 The first ItemStack
     * @param is2 The second ItemStack
     * @param checkNbt A flag to determine wether or not to compare NBTTagCompounds
     * @return {@code true}, if the stacks are considered equal, {@code false} otherwise
     */
    public static boolean areEqual(ItemStack is1, ItemStack is2, boolean checkNbt) {
        return areEqual(is1, is2, false, checkNbt);
    }

    /**
     * Checks wether or not 2 ItemStacks are equal.
     * If one of the ItemStacks has a damage value equal to {@link OreDictionary#WILDCARD_VALUE}, their damage value isn't checked against eachother,
     * otherwise it'll check if both are equal.
     * @param is1 The first ItemStack
     * @param is2 The second ItemStack
     * @param checkStackSize A flag to determine wether or not to compare stack sizes
     * @param checkNbt A flag to determine wether or not to compare NBTTagCompounds
     * @return {@code true}, if the stacks are considered equal, {@code false} otherwise
     */
    public static boolean areEqual(ItemStack is1, ItemStack is2, boolean checkStackSize, boolean checkNbt) {
        return areEqual(is1, is2, checkStackSize, checkNbt, true);
    }

    public static boolean areEqual(ItemStack is1, ItemStack is2, boolean checkStackSize, boolean checkNbt, boolean checkDmg) {
        if( is1 == null && is2 == null ) {
            return true;
        }

        if( !isValid(is2) || !isItem(is1, is2.getItem()) ) {
            return false;
        }

        assert is1 != null;
        //noinspection SimplifiableIfStatement
        if( checkDmg && (is1.getItemDamage() != OreDictionary.WILDCARD_VALUE || is2.getItemDamage() != OreDictionary.WILDCARD_VALUE) && is1.getItemDamage() != is2.getItemDamage() ) {
            return false;
        }

        return !(checkStackSize && is1.stackSize != is2.stackSize) && (!checkNbt || Objects.equals(is1.getTagCompound(), is2.getTagCompound()));
    }

    /**
     * Writes the ItemStack as a new NBTTagCompound to the specified NBTTagCompound with the tagName as key.
     * @param stack The ItemStack to write.
     * @param tag The NBTTagCompound to be written.
     * @param tagName The key for the tag.
     */
    public static void writeStackToTag(ItemStack stack, NBTTagCompound tag, String tagName) {
        NBTTagCompound stackTag = new NBTTagCompound();
        stack.writeToNBT(stackTag);
        tag.setTag(tagName, stackTag);
    }

    /**
     * Checks wether or not the given ItemStack can be found in the provided ItemStack array.
     * @param stack The ItemStack it should search for.
     * @param stacks The ItemStack array which should be checked.
     * @return true, if the ItemStack can be found, false otherwise.
     */
    public static boolean isStackInArray(ItemStack stack, ItemStack... stacks) {
        return Arrays.stream(stacks).anyMatch(currStack -> ItemStackUtils.areEqual(stack, currStack));
    }

    public static boolean canStack(ItemStack stack1, ItemStack stack2, boolean consumeAll) {
        return stack1 == null || stack2 == null
                || (stack1.isStackable() && areEqual(stack1, stack2, false, true, !stack2.getHasSubtypes())
                    && (!consumeAll || stack1.stackSize + stack2.stackSize <= stack1.getMaxStackSize()));
    }
}
