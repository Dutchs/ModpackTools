package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import com.dutchs.modpacktools.util.RecipeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class RecipeMakerActionPacket implements NetworkManager.INetworkPacket {
    public enum RecipeMakerActionType {
        Shaped,
        Shapeless,
        ClearRecipe,
        ClearEverything
    }

    private RecipeMakerActionType actionType;
    private int[] inputIndexer;

    public RecipeMakerActionPacket() {
    }

    public RecipeMakerActionPacket(@NotNull RecipeMakerActionType type, int[] indexer) {
        actionType = type;
        inputIndexer = indexer;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        RecipeMakerActionPacket actionPacket = (RecipeMakerActionPacket) msg;
        packetBuffer.writeEnum(actionPacket.actionType);
        packetBuffer.writeVarIntArray(actionPacket.inputIndexer);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        RecipeMakerActionPacket result = new RecipeMakerActionPacket();
        result.actionType = packetBuffer.readEnum(RecipeMakerActionType.class);
        result.inputIndexer = packetBuffer.readVarIntArray();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer p = context.getSender();
            if (p != null) {
                RecipeMakerActionPacket action = (RecipeMakerActionPacket) msg;
                RecipeMakerActionType type = action.actionType;
                if (p.containerMenu instanceof RecipeMakerMenu menu) {
                    if (type == RecipeMakerActionType.Shapeless) {
                        ItemStack result = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        ModpackTools.NETWORK.toPlayer(new ClientRecipeMakerResultPacket(type, RecipeUtil.createShapelessJSON(result, input, action.inputIndexer), RecipeUtil.formatInput(input, action.inputIndexer), RecipeUtil.formatResult(result)), p);
                    } else if (type == RecipeMakerActionType.Shaped) {
                        ItemStack result = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        ModpackTools.NETWORK.toPlayer(new ClientRecipeMakerResultPacket(type, RecipeUtil.createShapedJSON(result, input, action.inputIndexer), RecipeUtil.formatInput(input, action.inputIndexer), RecipeUtil.formatResult(result)), p);
                    } else if (type == RecipeMakerActionType.ClearRecipe) {
                        menu.clearCraftingContent();
                    } else if (type == RecipeMakerActionType.ClearEverything) {
                        menu.clearCraftingContent();
                        p.getInventory().items.clear();
                    }
                }
            }
        });
        context.setPacketHandled(true);
    }
}
