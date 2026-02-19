package com.leclowndu93150.chisel.inventory;

import com.leclowndu93150.chisel.api.IChiselItem;
import com.leclowndu93150.chisel.api.block.ChiselBlockType;
import com.leclowndu93150.chisel.carving.CarvingHelper;
import com.leclowndu93150.chisel.init.ChiselBlocks;
import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory for the chisel selection grid.
 * The "special slot" (last slot) holds the target item being chiseled.
 * Selection slots (0 to size-1) display available variations.
 * All variants are stored in a separate list to support scrolling.
 */
public class InventoryChiselSelection implements Container {

    public final int size;
    public int activeVariations = 0;
    @Nullable
    ChiselMenu container;
    NonNullList<ItemStack> inventory;
    /** All available variants (may exceed visible slot count) */
    private List<ItemStack> allVariants = new ArrayList<>();
    /** When true, updateItems() loads ALL chisel blocks instead of just one carving group */
    private boolean debugShowAll = false;

    public InventoryChiselSelection(ChiselMenu container, int size) {
        this.size = size;
        this.container = container;
        this.inventory = NonNullList.withSize(size + 1, ItemStack.EMPTY);
    }

    @Override
    public int getContainerSize() {
        return size + 1;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= inventory.size()) {
            return ItemStack.EMPTY;
        }
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inventory.get(slot);
        if (!stack.isEmpty()) {
            if (stack.getCount() <= amount) {
                setItem(slot, ItemStack.EMPTY);
                return stack;
            } else {
                ItemStack split = stack.split(amount);
                if (stack.getCount() == 0) {
                    setItem(slot, ItemStack.EMPTY);
                }
                return split;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        inventory.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < inventory.size()) {
            inventory.set(slot, stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        if (container == null) return false;
        ItemStack held = player.getInventory().getItem(container.getChiselSlot());
        return !held.isEmpty() && held.getItem() instanceof IChiselItem chiselItem &&
               chiselItem.canOpenGui(player.level(), player, container.getHand());
    }

    @Override
    public void clearContent() {
        inventory.clear();
        for (int i = 0; i < size + 1; i++) {
            inventory.add(ItemStack.EMPTY);
        }
    }

    /**
     * Gets the item in the special slot (input/target slot).
     */
    public ItemStack getStackInSpecialSlot() {
        return inventory.get(size);
    }

    /**
     * Sets the item in the special slot (input/target slot).
     */
    public void setStackInSpecialSlot(ItemStack stack) {
        setItem(size, stack);
    }

    /**
     * Clears only the selection slots, not the special slot.
     */
    public void clearItems() {
        activeVariations = 0;
        allVariants.clear();
        for (int i = 0; i < size; i++) {
            setItem(i, ItemStack.EMPTY);
        }
    }

    public void setDebugShowAll(boolean debugShowAll) {
        this.debugShowAll = debugShowAll;
    }

    public boolean isDebugShowAll() {
        return debugShowAll;
    }

    /**
     * Updates the selection slots based on the current target item.
     * Populates with available variations for chiseling.
     * Stores all variants for scrolling support.
     */
    public void updateItems() {
        ItemStack target = getStackInSpecialSlot();
        clearItems();

        if (debugShowAll) {
            allVariants.clear();
            for (ChiselBlockType<?> blockType : ChiselBlocks.ALL_BLOCK_TYPES) {
                for (RegistryObject<? extends Block> reg : blockType.getAllBlocks()) {
                    allVariants.add(new ItemStack(reg.get()));
                }
            }
            activeVariations = allVariants.size();
            updateVisibleSlots(0);
            return;
        }

        if (target.isEmpty()) {
            return;
        }

        TagKey<Item> group = CarvingHelper.getCarvingGroupForItem(target);
        if (group == null) {
            return;
        }

        List<Item> variations = CarvingHelper.getItemsInGroup(group);
        allVariants.clear();
        for (Item item : variations) {
            allVariants.add(new ItemStack(item));
        }
        activeVariations = allVariants.size();

        updateVisibleSlots(0);
    }

    /**
     * Updates the visible slots based on the scroll offset (in rows).
     * @param scrollRow the first visible row index
     */
    public void updateVisibleSlots(int scrollRow) {
        int cols = container != null ? container.getSelectionCols() : 10;
        int startIndex = scrollRow * cols;
        for (int i = 0; i < size; i++) {
            int variantIndex = startIndex + i;
            if (variantIndex < allVariants.size()) {
                setItem(i, allVariants.get(variantIndex).copy());
            } else {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * @return total number of variants available (may exceed visible slot count)
     */
    public int getTotalVariants() {
        return allVariants.size();
    }

    /**
     * Gets a variant by its absolute index (not slot index).
     */
    public ItemStack getVariant(int absoluteIndex) {
        if (absoluteIndex < 0 || absoluteIndex >= allVariants.size()) {
            return ItemStack.EMPTY;
        }
        return allVariants.get(absoluteIndex);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot != size) {
            return false;
        }
        return CarvingHelper.canChisel(stack);
    }
}
