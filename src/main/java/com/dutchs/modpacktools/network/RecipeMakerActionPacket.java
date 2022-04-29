package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ModpackTools;
import com.dutchs.modpacktools.gui.RecipeMakerMenu;
import com.dutchs.modpacktools.util.RecipeUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
                        ItemStack output = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        String json = RecipeUtil.makeShapelessJSONRecipe(output, input);
                        ClientRecipeMakerResultPacket result = new ClientRecipeMakerResultPacket(type, json, String.join(", ", input.stream().filter(i -> i.getItem() != Items.AIR).map(i -> i.getItem().getRegistryName().toString()).toList()), output.getItem().getRegistryName().toString() + "(" + output.getCount() + ")");
                        ModpackTools.NETWORK.toPlayer(result, p);
                    } else if (type == RecipeMakerActionType.Shaped) {
                        ItemStack output = menu.getOutputItemStack();
                        List<ItemStack> input = menu.getInputItemStacks();
                        String json = RecipeUtil.makeShapedJSONRecipe(output, input);
                        ClientRecipeMakerResultPacket result = new ClientRecipeMakerResultPacket(type, json, String.join(", ", input.stream().map(i -> i.getItem().getRegistryName().toString()).toList()), output.getItem().getRegistryName().toString()+ "(" + output.getCount() + ")");
                        ModpackTools.NETWORK.toPlayer(result, p);
                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }
}
