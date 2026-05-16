package net.arthurllew.mementobeta.network;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.network.handlers.*;
import net.arthurllew.mementobeta.network.packet.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * Registers custom network packets.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public abstract class MementoBetaNetwork {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Initiate network registering
        final PayloadRegistrar registrar = event.registrar("1");

        // Time lock packet
        registrar.playToClient(TimeLockPacket.TYPE, TimeLockPacket.STREAM_CODEC,
                new TimeLockPacketHandler());

        // Fixed time packet
        registrar.playToClient(FixedTimePacket.TYPE, FixedTimePacket.STREAM_CODEC,
                new FixedTimePacketHandler());

        // Time data sync packet
        registrar.playToClient(TimeDataSyncPacket.TYPE, TimeDataSyncPacket.STREAM_CODEC,
                new TimeDataSyncPacketHandler());

        // Portal travel sound packet
        registrar.playToClient(BetaTravelSoundPacket.TYPE, BetaTravelSoundPacket.STREAM_CODEC,
                new BetaTravelSoundPacketHandler());

        // Molten block packet
        registrar.playToClient(MoltenBlockPacket.TYPE, MoltenBlockPacket.STREAM_CODEC,
                new MoltenBlockPacketHandler());
    }

    /**
     * Sends message to specified player.
     *
     * @param player player
     * @param message packet
     */
    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player, message);
    }

    /**
     * Sends message to all players in provided dimension.
     *
     * @param message packet
     */
    public static void sendToPlayersInDimension(ServerLevel level, CustomPacketPayload message) {
        PacketDistributor.sendToPlayersInDimension(level, message);
    }
}
