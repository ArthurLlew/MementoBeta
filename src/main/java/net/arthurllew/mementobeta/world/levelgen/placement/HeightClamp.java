package net.arthurllew.mementobeta.world.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HeightClamp extends PlacementFilter {
    /**
     * Codec.
     */
    public static final Codec<HeightClamp> CODEC = RecordCodecBuilder.create((values)
            -> values.group(
                    Codec.INT.optionalFieldOf("min_inclusive", Integer.MIN_VALUE)
                            .forGetter((heightClamp) -> heightClamp.minInclusive),
                    Codec.INT.optionalFieldOf("max_inclusive", Integer.MAX_VALUE)
                            .forGetter((heightClamp) -> heightClamp.maxInclusive)).apply(values, HeightClamp::new));

    /**
     * Minimum allowed height.
     */
    private final int minInclusive;
    /**
     * Maximum allowed height.
     */
    private final int maxInclusive;

    /**
     * Constructor.
     */
    public HeightClamp(int minInclusive, int maxInclusive) {
        this.minInclusive = minInclusive;
        this.maxInclusive = maxInclusive;
    }

    /**
     * @return whether provided position is within clamp range.
     */
    protected boolean shouldPlace(PlacementContext pContext, RandomSource pRandom, BlockPos pPos) {
        return this.minInclusive <= pPos.getY() && pPos.getY() <= this.maxInclusive;
    }

    /**
     * @return placement modifier type.
     */
    public PlacementModifierType<?> type() {
        return MementoBetaPlacements.HEIGHT_CLAMP;
    }
}
