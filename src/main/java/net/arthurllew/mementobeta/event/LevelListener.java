package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.arthurllew.mementobeta.block.portal.BetaPortalUtil;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.arthurllew.mementobeta.mixin.ServerLevelAccessor;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasonHolder;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.properties.WrappedLevelProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Handlers for level related events.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class LevelListener {
    /**
     * Inits attachments of Beta dimension level when it loads.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Level is server-side and belongs to correct dimension
        if (event.getLevel() instanceof Level level
                && level.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Init Beta dimension time data on both server and client
            BetaLevelTimeAttachment betaLevelTime = level
                    .getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);

            // Init Beta dimension season data on both server and client
            BetaLevelSeasonAttachment betLevelSeason = level
                    .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
            BetaBiomeSeasonHolder.setSavedBetaSeasonInstance(betLevelSeason);

            // Level is server-side
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                // Init Beta dimension seed data
                BetaLevelSeedAttachment betaLevelSeed = serverLevel
                        .getData(MementoBetaAttachments.BETA_SEED_ATTACHMENT);
                betaLevelSeed.initSeed(serverLevel.getServer());

                // Minecraft server instance
                MinecraftServer server = serverLevel.getServer();

                // Get Beta dimension chunk generator
                BetaChunkGenerator betaChunkGenerator = (BetaChunkGenerator)
                        server.registries().compositeAccess()
                                .registryOrThrow(Registries.LEVEL_STEM)
                                .getOrThrow(MementoBetaDimension.BETA_DIMENSION)
                                .generator();
                // Inject beta dimension seed
                betaChunkGenerator.setSeed(betaLevelSeed.getBetaSeed());

                // Create dimension specific level properties
                WrappedLevelProperties levelProperties = new WrappedLevelProperties(server.getWorldData(),
                        server.getWorldData().overworldData(), betaLevelTime.getDayTime());
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
                LevelAccessor levelAccessor = (LevelAccessor) serverLevel;
                // Set new properties
                serverLevelAccessor.setServerWorldProperties(levelProperties);
                levelAccessor.setWorldProperties(levelProperties);
            }
        }
    }

    /**
     * Additional actions performed every level tick.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        // Level belongs to correct dimension
        if (event.getLevel() instanceof Level level
                && level.hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
            // Level is server-side
            if (event.getLevel() instanceof ServerLevel serverLevel) {
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
                LevelAccessor levelAccessor = (LevelAccessor) serverLevel;

                // Calculate and set new game time
                serverLevelAccessor.getServerWorldProperties()
                        .setGameTime(levelAccessor.getWorldProperties().getGameTime() + 1L);

                // Tick day time according to game rules
                if (serverLevelAccessor.getServerWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    serverLevel.setDayTime(serverLevel.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)
                            .tickTime(serverLevel));
                }
            }
            // Level is client-side
            else if (level instanceof ClientLevel clientLevel && !Minecraft.getInstance().isPaused()) {
                // Get access to level data
                LevelAccessor levelAccessor = (LevelAccessor) clientLevel;

                // Tick day time according to game rules
                if (levelAccessor.getWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    // Even if server time is not ticking, client always increments time by 1 every tick
                    clientLevel.setDayTime(clientLevel.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)
                            .tickTime(clientLevel) - 1);
                }
            }

            // Tick season
            level.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT).tick();
        }
    }

    /**
     * Marks attachments as "dirty" when server saves data.
     */
    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        // Level is server-side and belongs to correct dimension
        if (event.getLevel() instanceof ServerLevel serverLevel
                && serverLevel.hasData(MementoBetaAttachments.BETA_SEED_ATTACHMENT)) {

            // Mark required attachments as dirty
            serverLevel.setData(MementoBetaAttachments.BETA_SEED_ATTACHMENT,
                    serverLevel.getData(MementoBetaAttachments.BETA_SEED_ATTACHMENT));
            serverLevel.setData(MementoBetaAttachments.BETA_TIME_ATTACHMENT,
                    serverLevel.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT));
            serverLevel.setData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT,
                    serverLevel.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT));
        }
    }

    /**
     * Fires when a player right-clicks a block. This can create a Beta dimension portal.
     *
     * @param event block right-click event
     */
    @SubscribeEvent
    public static void onInteractWithPortalFrame(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos blockPos = event.getPos();
        Direction direction = event.getFace();
        ItemStack itemStack = event.getItemStack();
        InteractionHand interactionHand = event.getHand();

        if (BetaPortalUtil.createPortal(MementoBetaBlocks.BETA_PORTAL.get(),
                player, level, blockPos, direction, itemStack, interactionHand)) {
            event.setCanceled(true);
        }
        else if (BetaPortalUtil.createPortal(MementoBetaBlocks.BETA_PORTAL_NETHER.get(),
                player, level, blockPos, direction, itemStack, interactionHand)) {
            event.setCanceled(true);
        }
    }

    /**
     * Fires when a block receives neighbor update. This can create a Beta dimension portal.
     *
     * @param event neighbor update event
     */
    @SubscribeEvent
    public static void onFlameExistsInsidePortalFrame(BlockEvent.NeighborNotifyEvent event) {
        net.minecraft.world.level.LevelAccessor level = event.getLevel();
        BlockPos blockPos = event.getPos();
        BlockState blockState = level.getBlockState(blockPos);

        if (BetaPortalUtil.detectInFrame(MementoBetaBlocks.BETA_PORTAL.get(),
                level, blockPos, blockState)) {
            event.setCanceled(true);
        }
        else if (BetaPortalUtil.detectInFrame(MementoBetaBlocks.BETA_PORTAL_NETHER.get(),
                level, blockPos, blockState)) {
            event.setCanceled(true);
        }
    }

    /**
     * Called when players finished sleeping. If they finished sleeping in Beta dimension, its data
     * (time, season and weather) should be updated.
     */
    @SubscribeEvent
    public static void onSleepFinish(SleepFinishedTimeEvent event) {
        // Level belongs to correct dimension
        if (event.getLevel() instanceof Level level
                && level.hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
            // Level is server-side
            if (level instanceof ServerLevel serverLevel) {
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;

                // Update weather
                serverLevelAccessor.getServerWorldProperties().setRainTime(0);
                serverLevelAccessor.getServerWorldProperties().setRaining(false);
                serverLevelAccessor.getServerWorldProperties().setThunderTime(0);
                serverLevelAccessor.getServerWorldProperties().setThundering(false);
            }

            // Update season
            BetaLevelSeasonAttachment betaLevelSeason = level
                    .getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
            betaLevelSeason.setSeason(betaLevelSeason.getSeason()
                    + MementoBetaDimension.DAY_CYCLE_TOTAL_TIME - level.getDayTime());

            // Set new time after sleep
            event.setTimeAddition(MementoBetaDimension.DAY_CYCLE_TOTAL_TIME);
        }
    }

    /**
     * Called when player tries to sleep. If it was done in Beta dimension, result depends on the time lock.
     */
    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onTriedToSleep(CanPlayerSleepEvent event) {
        // Player is server-side and he is in correct dimension
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().hasData(MementoBetaAttachments.BETA_TIME_ATTACHMENT)) {
            // Deny sleeping if time is locked
            if (serverPlayer.level().getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT).isTimeLocked()) {
                event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
            }
        }
    }
}
