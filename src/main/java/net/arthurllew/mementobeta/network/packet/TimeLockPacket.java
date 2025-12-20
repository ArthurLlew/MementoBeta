package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Handles time lock packet.
 */
@MethodsReturnNonnullByDefault
public record TimeLockPacket(boolean isTimeLocked) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final CustomPacketPayload.Type<TimeLockPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_time_lock_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, TimeLockPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            TimeLockPacket::isTimeLocked,
            TimeLockPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
