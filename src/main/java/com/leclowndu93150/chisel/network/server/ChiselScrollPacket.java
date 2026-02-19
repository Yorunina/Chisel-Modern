package com.leclowndu93150.chisel.network.server;

import com.leclowndu93150.chisel.inventory.ChiselMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ChiselScrollPacket {

    private final int scrollRow;

    public ChiselScrollPacket(int scrollRow) {
        this.scrollRow = scrollRow;
    }

    public static void encode(ChiselScrollPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.scrollRow);
    }

    public static ChiselScrollPacket decode(FriendlyByteBuf buf) {
        return new ChiselScrollPacket(buf.readVarInt());
    }

    public static void handle(ChiselScrollPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null && player.containerMenu instanceof ChiselMenu menu) {
                menu.setScrollRow(packet.scrollRow);
            }
        });
        context.setPacketHandled(true);
    }
}
