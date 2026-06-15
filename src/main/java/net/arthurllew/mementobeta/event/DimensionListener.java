package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.arthurllew.mementobeta.mixin.ServerLevelAccessor;
import net.arthurllew.mementobeta.block.portal.BetaPortalUtil;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.arthurllew.mementobeta.world.biome.BetaBiomeSeasonHolder;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.properties.WrappedLevelProperties;
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
 * Handlers for dimension related server-side events.
 */
@EventBusSubscriber(modid = MementoBeta.MODID)
public class DimensionListener {
    /**
     * Inserts custom level properties into Beta dimension level and sets seed in Beta chunk generator.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Level belongs to correct dimension
        if (event.getLevel() instanceof Level level
                && level.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION))
        {
            // Level is server-side
            if (level instanceof ServerLevel serverLevel) {
                // Minecraft server instance
                MinecraftServer server = level.getServer();

                //=====================//
                // ==== Beta seed ==== //
                //=====================//

                // Get or create Beta dimension seed
                BetaSeedData seedData = serverLevel.getDataStorage()
                        .computeIfAbsent(BetaSeedData.FACTORY, BetaSeedData.ID)
                        // Init seed
                        .initSeed(level.getServer());

                // Get Beta dimension chunk generator
                BetaChunkGenerator betaChunkGenerator = (BetaChunkGenerator)
                        server.registries().compositeAccess()
                                .registryOrThrow(Registries.LEVEL_STEM)
                                .getOrThrow(MementoBetaDimension.BETA_DIMENSION)
                                .generator();
                // Inject world seed
                betaChunkGenerator.setSeed(seedData.getBetaSeed());

                //=====================//
                // ==== Beta time ==== //
                //=====================//

                // Get or create Beta dimension time data
                BetaTimeData timeData = serverLevel.getDataStorage().computeIfAbsent(BetaTimeData.FACTORY, BetaTimeData.ID);
                // Set current level
                timeData.setLevel(serverLevel);

                // Create dimension specific level properties
                WrappedLevelProperties levelProperties = new WrappedLevelProperties(server.getWorldData(),
                        server.getWorldData().overworldData(), timeData.getDayTime());
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
                LevelAccessor levelAccessor = (LevelAccessor) serverLevel;
                // Set new properties
                serverLevelAccessor.setServerWorldProperties(levelProperties);
                levelAccessor.setWorldProperties(levelProperties);

                //=======================//
                // ==== Beta season ==== //
                //=======================//

                // Get or create Beta dimension season data
                BetaSeasonData seasonData = serverLevel.getDataStorage()
                        .computeIfAbsent(BetaSeasonData.FACTORY, BetaSeasonData.ID);
                BetaBiomeSeasonHolder.setSavedBetaSeasonInstance(seasonData);
            }
            // Level is client-side
            else if (level instanceof ClientLevel clientLevel) {
                // Init data
                clientLevel.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);
                clientLevel.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
            }
        }
    }

    /**
     * Additional actions performed every server level tick.
     */
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Get access to level data
            ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
            LevelAccessor levelAccessor = (LevelAccessor) serverLevel;

            // Calculate and set new game time
            serverLevelAccessor.getServerWorldProperties()
                    .setGameTime(levelAccessor.getWorldProperties().getGameTime() + 1L);

            // Tick day time according to game rules
            if (serverLevelAccessor.getServerWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                BetaTimeData timeData = serverLevel.getDataStorage().get(BetaTimeData.FACTORY, BetaTimeData.ID);
                if (timeData != null) {
                    serverLevel.setDayTime(timeData.tickTime(serverLevel));
                }
            }

            // Tick season
            BetaSeasonData seasonData = serverLevel.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
            if (seasonData != null) {
                seasonData.tick();
            }
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

        if (BetaPortalUtil.createPortal(player, level, blockPos, direction, itemStack, interactionHand)) {
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

        if (BetaPortalUtil.detectInFrame(level, blockPos, blockState)) {
            event.setCanceled(true);
        }
    }

    /**
     * Called when players finished sleeping. If they finished sleeping in Beta dimension, its time and
     * weather should be updated.
     */
    @SubscribeEvent
    public static void onSleepFinish(SleepFinishedTimeEvent event) {
        // Level belongs to correct dimension
        if (event.getLevel() instanceof Level level
                && level.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Level is server-side
            if (level instanceof ServerLevel serverLevel
                    && serverLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) level;

                // Update weather
                serverLevelAccessor.getServerWorldProperties().setRainTime(0);
                serverLevelAccessor.getServerWorldProperties().setRaining(false);
                serverLevelAccessor.getServerWorldProperties().setThunderTime(0);
                serverLevelAccessor.getServerWorldProperties().setThundering(false);

                // Update season
                BetaSeasonData seasonData = serverLevel.getDataStorage().get(BetaSeasonData.FACTORY, BetaSeasonData.ID);
                if (seasonData != null) {
                    seasonData.setSeason(seasonData.getSeason()
                            + MementoBetaDimension.DAY_CYCLE_TOTAL_TIME - serverLevel.getDayTime());
                }
            }
            // Level is client-side
            else if (level instanceof ClientLevel clientLevel
                    && clientLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
                BetaSeasonData seasonData = clientLevel.getData(MementoBetaAttachments.BETA_SEASON_ATTACHMENT);
                seasonData.setSeason(seasonData.getSeason()
                        + MementoBetaDimension.DAY_CYCLE_TOTAL_TIME - clientLevel.getDayTime());
            }

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
                serverPlayer.level().dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Deny sleeping if time is locked
            if (serverPlayer.level() instanceof ServerLevel level) {
                BetaTimeData timeData = level.getDataStorage().get(BetaTimeData.FACTORY, BetaTimeData.ID);
                if (timeData != null) {
                    if (timeData.isTimeLocked()) {
                        event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
                    }
                }
            }
        }
    }
}
