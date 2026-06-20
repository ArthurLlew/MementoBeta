package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record TimeDataSyncPacket(long dayTime, boolean isTimeLocked, long fixedTime) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final CustomPacketPayload.Type<TimeDataSyncPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_time_data_sync_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, TimeDataSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            TimeDataSyncPacket::dayTime,
            ByteBufCodecs.BOOL,
            TimeDataSyncPacket::isTimeLocked,
            ByteBufCodecs.VAR_LONG,
            TimeDataSyncPacket::fixedTime,
            TimeDataSyncPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
