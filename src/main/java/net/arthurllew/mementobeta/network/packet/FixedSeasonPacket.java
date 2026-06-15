package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record FixedSeasonPacket(long fixedSeason) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final Type<FixedSeasonPacket> TYPE =
            new Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_fixed_season_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, FixedSeasonPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            FixedSeasonPacket::fixedSeason,
            FixedSeasonPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
