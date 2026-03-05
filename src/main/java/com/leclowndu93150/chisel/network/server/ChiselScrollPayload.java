package com.leclowndu93150.chisel.network.server;

import com.leclowndu93150.chisel.Chisel;
import com.leclowndu93150.chisel.inventory.ChiselMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChiselScrollPayload(int scrollRow) implements CustomPacketPayload {

    public static final Type<ChiselScrollPayload> TYPE = new Type<>(Chisel.id("chisel_scroll"));

    public static final StreamCodec<FriendlyByteBuf, ChiselScrollPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ChiselScrollPayload::scrollRow,
            ChiselScrollPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof ChiselMenu menu) {
                    menu.setScrollRow(scrollRow);
                }
            }
        });
    }
}
