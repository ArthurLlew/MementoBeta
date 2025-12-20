package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record FixedTimePacket(long fixedTime) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final CustomPacketPayload.Type<FixedTimePacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_fixed_time_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, FixedTimePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            FixedTimePacket::fixedTime,
            FixedTimePacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
