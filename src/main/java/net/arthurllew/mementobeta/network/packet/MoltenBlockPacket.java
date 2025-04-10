package net.arthurllew.mementobeta.network.packet;

import net.arthurllew.mementobeta.network.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MoltenBlockPacket {
    /**
     * Molten block position.
     */
    private final BlockPos pos;

    /**
     * Packet constructor.
     */
    public MoltenBlockPacket(BlockPos pos) {
        this.pos = pos;
    }

    /**
     * Packet decoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public MoltenBlockPacket(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
    }

    /**
     * Packet encoder.
     * @param buffer data buffer.
     */
    @SuppressWarnings("unused")
    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos);
    }

    /**
     * Packet consumer.
     * @param supplier network context supplier.
     */
    public void consume(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                // Execute code only on physical client
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleMoltenBlockPacket(this.pos)));
        context.setPacketHandled(true);
    }
}
