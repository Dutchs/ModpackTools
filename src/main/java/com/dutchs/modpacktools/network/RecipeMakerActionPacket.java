package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import com.dutchs.modpacktools.util.RecipeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Supplier;

public class RecipeMakerActionPacket implements NetworkManager.INetworkPacket {
    public enum RecipeMakerActionType {
        Shaped,
        Shapeless
    }

    private RecipeMakerActionType actionType;

    public RecipeMakerActionPacket() {
    }

    public RecipeMakerActionPacket(@NotNull RecipeMakerActionType type) {
        actionType = type;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        RecipeMakerActionPacket actionPacket = (RecipeMakerActionPacket) msg;
        packetBuffer.writeEnum(actionPacket.actionType);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        RecipeMakerActionPacket result = new RecipeMakerActionPacket();
        result.actionType = packetBuffer.readEnum(RecipeMakerActionType.class);
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer p = contextSupplier.get().getSender();
            if (p != null) {
                RecipeMakerActionPacket action = (RecipeMakerActionPacket) msg;
                RecipeMakerActionType type = action.actionType;
                if (p.containerMenu instanceof RecipeMakerMenu menu) {
                    if (type == RecipeMakerActionType.Shapeless) {
                        ItemStack result = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        ModpackTools.NETWORK.toPlayer(new ClientRecipeMakerResultPacket(type, RecipeUtil.createShapelessJSON(result, input), RecipeUtil.formatInput(input), RecipeUtil.formatResult(result)), p);
                    } else if (type == RecipeMakerActionType.Shaped) {
                        ItemStack result = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        ModpackTools.NETWORK.toPlayer(new ClientRecipeMakerResultPacket(type, RecipeUtil.createShapedJSON(result, input), RecipeUtil.formatInput(input), RecipeUtil.formatResult(result)), p);
                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
