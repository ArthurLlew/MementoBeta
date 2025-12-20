package net.arthurllew.mementobeta.network.packet;

import io.netty.buffer.ByteBuf;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

@MethodsReturnNonnullByDefault
public record MoltenBlockPacket(BlockPos pos) implements CustomPacketPayload {
    /**
     * Packet type.
     */
    public static final CustomPacketPayload.Type<MoltenBlockPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation
                    .fromNamespaceAndPath(MementoBeta.MODID, "beta_molten_block_packet"));

    /**
     * Packet codec.
     */
    public static final StreamCodec<ByteBuf, MoltenBlockPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MoltenBlockPacket::pos,
            MoltenBlockPacket::new
    );

    /**
     * Type getter.
     */
    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
