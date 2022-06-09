package com.dutchs.modpacktools.gui;

import com.dutchs.modpacktools.registry.ContainerRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecipeMakerMenu extends AbstractContainerMenu {
    public static final int RESULT_SLOT = 0;
    private static final int CRAFT_SLOT_START = 1;
    private static final int CRAFT_SLOT_END = 10;
    private static final int INV_SLOT_START = 10;
    private static final int INV_SLOT_END = 37;
    private static final int USE_ROW_SLOT_START = 37;
    private static final int USE_ROW_SLOT_END = 46;
    private final InputContainer craftSlots = new InputContainer(this, 3, 3);
    private final OutputContainer resultSlots = new OutputContainer();
    private final Player player;

    public RecipeMakerMenu(int id, Inventory inventory, Player player) {
        super(ContainerRegistry.RECIPE_MAKER.get(), id);
        this.player = player;
        this.addSlot(new Slot(this.resultSlots, 0, 124, 35));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(this.craftSlots, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(inventory, l, 8 + l * 18, 142));
        }

    }

    public void clearCraftingContent() {
        this.craftSlots.clearContent();
        this.resultSlots.clearContent();
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void removed(@NotNull Player pPlayer) {
        super.removed(pPlayer);
        if (!pPlayer.level.isClientSide) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                if (!serverPlayer.isAlive() || serverPlayer.hasDisconnected()) {
                    for (int j = 0; j < craftSlots.getContainerSize(); ++j) {
                        serverPlayer.drop(craftSlots.removeItemNoUpdate(j), false);
                    }
                    serverPlayer.drop(resultSlots.removeItemNoUpdate(0), false);
                } else {
                    Inventory inventory = serverPlayer.getInventory();
                    for (int i = 0; i < craftSlots.getContainerSize(); ++i) {
                        inventory.placeItemBackInInventory(craftSlots.removeItemNoUpdate(i));
                    }
                    inventory.placeItemBackInInventory(resultSlots.removeItemNoUpdate(0));
                }
            }
        }
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean stillValid(@NotNull Player pPlayer) {
        return true;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public @NotNull ItemStack quickMoveStack(@NotNull Player pPlayer, int pIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (pIndex == RESULT_SLOT) {
                if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
                    return ItemStack.EMPTY;
                }
                //slot.onQuickCraft(itemstack1, itemstack);
            } else if (pIndex >= INV_SLOT_START && pIndex < USE_ROW_SLOT_END) {
                if (!this.moveItemStackTo(itemstack1, RESULT_SLOT, 10, false)) {
                    if (pIndex < 37) {
                        if (!this.moveItemStackTo(itemstack1, 37, 46, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (!this.moveItemStackTo(itemstack1, 10, 37, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!this.moveItemStackTo(itemstack1, 10, 46, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(pPlayer, itemstack1);
            if (pIndex == RESULT_SLOT) {
                pPlayer.drop(itemstack1, false);
            }
        }

        return itemstack;
    }

    /**
     * Called to determine if the current slot is valid for the stack merging (double-click) code. The stack passed in is
     * null for the initial slot that was double-clicked.
     */
    public boolean canTakeItemForPickAll(@NotNull ItemStack pStack, @NotNull Slot pSlot) {
        return super.canTakeItemForPickAll(pStack, pSlot);
    }

    public int getResultSlotIndex() {
        return RESULT_SLOT;
    }

    public ItemStack getOutputItemStack() {
        return getSlot(getResultSlotIndex()).getItem();
    }

    public List<ItemStack> getInputItemStacks() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = CRAFT_SLOT_START; i < CRAFT_SLOT_END; i++) {
            result.add(slots.get(i).getItem());
        }
        return result;
    }

    public int getGridWidth() {
        return this.craftSlots.getWidth();
    }

    public int getGridHeight() {
        return this.craftSlots.getHeight();
    }

    public int getSize() {
        return 10;
    }
}