package com.dutchs.modpacktools.network;

import com.dutchs.modpacktools.ConfigHandler;
import com.dutchs.modpacktools.util.ComponentUtil;
import com.dutchs.modpacktools.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringUtil;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ClientRecipeMakerResultPacket implements NetworkManager.INetworkPacket {
    private RecipeMakerActionPacket.RecipeMakerActionType recipeType;
    private String recipeJSON;
    private String recipeInput;
    private String recipeOutput;

    public ClientRecipeMakerResultPacket() {
    }

    public ClientRecipeMakerResultPacket(@NotNull RecipeMakerActionPacket.RecipeMakerActionType type, @NotNull String items, @NotNull String input, @NotNull String output) {
        recipeType = type;
        recipeJSON = items;
        recipeInput = input;
        recipeOutput = output;
    }

    @Override
    public void encode(Object msg, FriendlyByteBuf packetBuffer) {
        ClientRecipeMakerResultPacket blockPacket = (ClientRecipeMakerResultPacket) msg;
        packetBuffer.writeEnum(blockPacket.recipeType);
        packetBuffer.writeUtf(blockPacket.recipeJSON);
        packetBuffer.writeUtf(blockPacket.recipeInput);
        packetBuffer.writeUtf(blockPacket.recipeOutput);
    }

    @Override
    public <MESSAGE> MESSAGE decode(FriendlyByteBuf packetBuffer) {
        ClientRecipeMakerResultPacket result = new ClientRecipeMakerResultPacket();
        result.recipeType = packetBuffer.readEnum(RecipeMakerActionPacket.RecipeMakerActionType.class);
        result.recipeJSON = packetBuffer.readUtf();
        result.recipeInput = packetBuffer.readUtf();
        result.recipeOutput = packetBuffer.readUtf();
        return (MESSAGE) result;
    }

    @Override
    public void handle(Object msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient((ClientRecipeMakerResultPacket) msg, contextSupplier));
        });
        contextSupplier.get().setPacketHandled(true);
    }

    private void handleClient(ClientRecipeMakerResultPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        if (!StringUtil.isNullOrEmpty(msg.recipeJSON)) {
            String newLinesItems = msg.recipeJSON.replace("\n", System.lineSeparator());

            if (ConfigHandler.autoCopyItems)
                Minecraft.getInstance().keyboardHandler.setClipboard(newLinesItems);

            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContentWithCopy("RecipeMaker(" + msg.recipeType + ")", msg.recipeInput + " ->\n  " + msg.recipeOutput, newLinesItems));
        } else {
            PlayerUtil.sendClientMessage(ComponentUtil.formatTitleContent("RecipeMaker(" + msg.recipeType + ")", "Nothing to print"));
        }
    }
}
