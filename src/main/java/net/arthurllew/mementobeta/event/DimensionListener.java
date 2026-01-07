package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaAttachments;
import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.arthurllew.mementobeta.mixin.ServerLevelAccessor;
import net.arthurllew.mementobeta.block.portal.BetaPortalUtil;
import net.arthurllew.mementobeta.registry.MementoBetaDimension;
import net.arthurllew.mementobeta.world.levelgen.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.levelgen.util.BetaSeedHolder;
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
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldOptions;
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
     * Inserts custom level properties into beta dimension level and sets seed in beta chunk generator.
     * @param event level load event.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        net.minecraft.world.level.LevelAccessor level = event.getLevel();
        MinecraftServer server = level.getServer();

        // Level is server-side and belongs to correct dimension
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {

            // ==== Custom time handling ==== //

            // Get or create beta level time data
            BetaTimeData timeData = serverLevel.getDataStorage().computeIfAbsent(
                    BetaTimeData.FACTORY, "betaworld_time");
            // Set current level
            timeData.setLevel(serverLevel);

            // Get access to level data
            ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
            LevelAccessor levelAccessor = (LevelAccessor) serverLevel;

            // Create dimension specific level properties
            WrappedLevelProperties levelProperties = new WrappedLevelProperties(server.getWorldData(),
                    server.getWorldData().overworldData(), timeData.getDayTime());

            // Set new properties
            serverLevelAccessor.setServerWorldProperties(levelProperties);
            levelAccessor.setWorldProperties(levelProperties);

            // ==== Custom seed handling ==== //

            // Get or create seed data
            BetaSeedData seedData = serverLevel.getDataStorage().computeIfAbsent(
                    BetaSeedData.FACTORY, "betaworld_seed");

            // If data was absent
            if (seedData.wasAbsent()) {
                seedData.setBetaSeed(WorldOptions.parseSeed(BetaSeedHolder.getSeed())
                        .orElse(server.getWorldData().worldGenOptions().seed()));
            }

            // Get chunk generator from beta dimension options
            LevelStem betaDimensionOptions =
                    server.registries().compositeAccess()
                            .registryOrThrow(Registries.LEVEL_STEM).getOrThrow(MementoBetaDimension.BETA_DIMENSION);
            BetaChunkGenerator betaChunkGenerator = (BetaChunkGenerator)betaDimensionOptions.generator();

            // Inject world seed
            betaChunkGenerator.setSeed(seedData.getBetaSeed());
        }
        else if (level instanceof ClientLevel clientLevel
                && clientLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            clientLevel.getData(MementoBetaAttachments.BETA_TIME_ATTACHMENT);
        }
    }

    /**
     * Additional actions performed every server level tick.
     * @param event level tick event.
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
            long i = levelAccessor.getWorldProperties().getGameTime() + 1L;
            serverLevelAccessor.getServerWorldProperties().setGameTime(i);

            // Tick day time according to game rules
            if (serverLevelAccessor.getServerWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                BetaTimeData timeData = serverLevel.getDataStorage().get(BetaTimeData.FACTORY, "betaworld_time");
                if (timeData != null) {
                    serverLevel.setDayTime(timeData.tickTime(serverLevel));
                }
            }
        }
    }

    /**
     * Fires when a player right-clicks a block. This can create a Beta dimension portal.
     * @param event block right-click event.
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
     * @param event neighbor update event.
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
     * Called when players finished sleeping. If they finished sleeping in custom dimension, its time and
     * weather should be updated.
     * @param event sleep finished event.
     */
    @SubscribeEvent
    public static void onSleepFinish(SleepFinishedTimeEvent event) {
        // Level is server-side and belongs to correct dimension
        net.minecraft.world.level.LevelAccessor level = event.getLevel();
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Get access to level data
            ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) level;

            // Update weather
            serverLevelAccessor.getServerWorldProperties().setRainTime(0);
            serverLevelAccessor.getServerWorldProperties().setRaining(false);
            serverLevelAccessor.getServerWorldProperties().setThunderTime(0);
            serverLevelAccessor.getServerWorldProperties().setThundering(false);

            // Set new time (vanilla code is kinda weird in this place; performs some calculations to always
            // get the same result).
            event.setTimeAddition(MementoBetaDimension.DAY_CYCLE_TOTAL_TIME);
        }
    }

    /**
     * Called when player tries to sleep. If it was done in custom dimension, result depends on time lock.
     * @param event sleep check event.
     */
    @SubscribeEvent
    public static void onTriedToSleep(CanPlayerSleepEvent event) {
        // Player is server-side and he is in correct dimension
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().dimensionTypeRegistration().is(MementoBetaDimension.DIMENSION_NAME_RESOURCE_LOCATION)) {
            // Deny sleeping if time is locked
            if (serverPlayer.level() instanceof ServerLevel level) {
                BetaTimeData timeData = level.getDataStorage().get(BetaTimeData.FACTORY, "betaworld_time");
                if (timeData != null) {
                    if (timeData.isTimeLocked()) {
                        event.setProblem(Player.BedSleepingProblem.NOT_POSSIBLE_NOW);
                    }
                }
            }
        }
    }
}
