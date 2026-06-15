package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record SeasonDataSyncPacket(long season, boolean isSeasonLocked, long fixedSeason) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final Type<SeasonDataSyncPacket> TYPE =
            new Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_season_data_sync_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, SeasonDataSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            SeasonDataSyncPacket::season,
            ByteBufCodecs.BOOL,
            SeasonDataSyncPacket::isSeasonLocked,
            ByteBufCodecs.VAR_LONG,
            SeasonDataSyncPacket::fixedSeason,
            SeasonDataSyncPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
