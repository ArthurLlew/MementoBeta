package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record SeasonPacket(long season) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final Type<SeasonPacket> TYPE =
            new Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_season_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, SeasonPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SeasonPacket::season,
            SeasonPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
