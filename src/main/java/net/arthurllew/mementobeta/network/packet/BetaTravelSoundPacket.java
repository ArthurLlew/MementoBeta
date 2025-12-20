package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record BetaTravelSoundPacket() implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final CustomPacketPayload.Type<BetaTravelSoundPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_travel_sound_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, BetaTravelSoundPacket> STREAM_CODEC = StreamCodec.unit(
            new BetaTravelSoundPacket());

    /**
     * Type getter.
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
