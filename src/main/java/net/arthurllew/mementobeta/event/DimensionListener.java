package net.arthurllew.mementobeta.event;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.capabilities.BetaTimeCapability;
import net.arthurllew.mementobeta.mixin.LevelAccessor;
import net.arthurllew.mementobeta.mixin.ServerLevelAccessor;
import net.arthurllew.mementobeta.portal.BetaPortalUtil;
import net.arthurllew.mementobeta.world.BetaChunkGenerator;
import net.arthurllew.mementobeta.world.BetaDimension;
import net.arthurllew.mementobeta.world.properties.WrappedLevelProperties;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Handlers for dimension related server-side events.
 */
@Mod.EventBusSubscriber(modid = MementoBeta.MODID)
public class DimensionListener {
    /**
     * Handles server about to start event. Injects world seed into chunk generator.
     * @param event server starting event.
     */
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event) {
        // Get minecraft server
        MinecraftServer server = event.getServer();

        // World seed
        long seed = server.getWorldData().worldGenOptions().seed();

        // Get chunk generator from beta dimension options
        LevelStem betaDimensionOptions =
                server.registries().compositeAccess()
                        .registryOrThrow(Registries.LEVEL_STEM).getOrThrow(BetaDimension.BETA_DIMENSION);
        BetaChunkGenerator betaChunkGenerator = (BetaChunkGenerator)betaDimensionOptions.generator();
        // Inject world seed
        betaChunkGenerator.setSeed(seed);
    }

    /**
     * Replaces vanilla properties for custom dimension level with new ones.
     * @param event level load event.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        net.minecraft.world.level.LevelAccessor level = event.getLevel();
        MinecraftServer server = level.getServer();

        // Level is server-side and belongs to correct dimension
        if (level instanceof ServerLevel serverLevel
                && serverLevel.dimensionTypeId().location().getPath().equals("betaworld")) {
            BetaTimeCapability.get(serverLevel).ifPresent(time -> {
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
                LevelAccessor levelAccessor = (LevelAccessor) serverLevel;

                // Create dimension specific level properties
                WrappedLevelProperties levelProperties = new WrappedLevelProperties(server.getWorldData(),
                        server.getWorldData().overworldData(), time.getDayTime());

                // Set new properties
                serverLevelAccessor.setServerWorldProperties(levelProperties);
                levelAccessor.setWorldProperties(levelProperties);
            });
        }
    }

    /**
     * Additional actions performed every server level tick.
     * @param event level tick event.
     */
    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        // Event is server-side and corresponds to tick end
        if (event.side == LogicalSide.SERVER && event.phase == TickEvent.Phase.END) {
            // Level is server-side and belongs to correct dimension
            Level level = event.level;
            if (level instanceof ServerLevel serverLevel
                    && serverLevel.dimensionTypeId().location().getPath().equals("betaworld")) {
                // Get access to level data
                ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) serverLevel;
                LevelAccessor levelAccessor = (LevelAccessor) serverLevel;

                // Calculate and set new time
                long i = levelAccessor.getWorldProperties().getGameTime() + 1L;
                serverLevelAccessor.getServerWorldProperties().setGameTime(i);

                // Tick day time according to game rules
                if (serverLevelAccessor.getServerWorldProperties().getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
                    BetaTimeCapability.get(serverLevel).ifPresent(time -> serverLevel.setDayTime(time.tickTime(serverLevel)));
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
                && serverLevel.dimensionTypeId().location().getPath().equals("betaworld")) {
            // Get access to level data
            ServerLevelAccessor serverLevelAccessor = (ServerLevelAccessor) level;

            // Update weather
            serverLevelAccessor.getServerWorldProperties().setRainTime(0);
            serverLevelAccessor.getServerWorldProperties().setRaining(false);
            serverLevelAccessor.getServerWorldProperties().setThunderTime(0);
            serverLevelAccessor.getServerWorldProperties().setThundering(false);

            // Set new time (vanilla code is kinda weird in this place; performs some calculations to always
            // get the same result).
            event.setTimeAddition(BetaDimension.DAY_CYCLE_TOTAL_TIME);
        }
    }

    /**
     * Called when player tries to sleep. If it was done in custom dimension, result depends on time lock.
     * @param event sleep check event.
     */
    @SubscribeEvent
    public static void onTriedToSleep(SleepingTimeCheckEvent event) {
        // Player is server-side and he is in correct dimension
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer &&
                serverPlayer.level().dimensionTypeId().location().getPath().equals("betaworld")) {
            // Deny sleeping if tf time is locked
            BetaTimeCapability.get(serverPlayer.level()).ifPresent((time) -> {
                if (time.isTimeLocked()) {
                    event.setResult(Event.Result.DENY);
                }
            });
        }
    }
}
