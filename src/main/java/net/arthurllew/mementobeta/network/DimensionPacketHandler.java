package net.arthurllew.mementobeta.network;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.network.packet.FixedTimePacket;
import net.arthurllew.mementobeta.network.packet.TimeDataSyncPacket;
import net.arthurllew.mementobeta.network.packet.TimeLockPacket;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Handler for custom dimension network packets.
 */
public class DimensionPacketHandler {
    /**
     * Network channel instance.
     */
    private static SimpleChannel INSTANCE;

    /**
     * Initiates network channel.
     */
    public static void register() {
        // Create channel
        INSTANCE = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(MementoBeta.MODID, "beta_time"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        // Time lock packet
        INSTANCE.messageBuilder(TimeLockPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TimeLockPacket::new)
                .encoder(TimeLockPacket::encoder)
                .consumerMainThread(TimeLockPacket::consume)
                .add();

        // Fixed time packet
        INSTANCE.messageBuilder(FixedTimePacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(FixedTimePacket::new)
                .encoder(FixedTimePacket::encoder)
                .consumerMainThread(FixedTimePacket::consume)
                .add();

        // Time data sync packet
        INSTANCE.messageBuilder(TimeDataSyncPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .decoder(TimeDataSyncPacket::new)
                .encoder(TimeDataSyncPacket::encoder)
                .consumerMainThread(TimeDataSyncPacket::consume)
                .add();
    }

    /**
     * Sends message to specified player.
     * @param player player.
     * @param message packet.
     * @param <MSG> packet type.
     */
    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    /**
     * Sends message to all players in beta dimension.
     * @param message packet.
     * @param <MSG> packet type.
     */
    public static <MSG> void sendToPlayersInDimension(MSG message) {
        INSTANCE.send(PacketDistributor.DIMENSION.with(() -> BetaDimension.BETA_DIMENSION_LEVEL), message);
    }
}
