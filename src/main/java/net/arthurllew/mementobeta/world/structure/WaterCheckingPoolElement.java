package net.arthurllew.mementobeta.world.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.registry.MementoBetaStructures;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class WaterCheckingPoolElement extends SinglePoolElement {
    // Codec required for data packs / JSON integration
    public static final MapCodec<WaterCheckingPoolElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(templateCodec(), processorsCodec(), projectionCodec())
                    .apply(instance, WaterCheckingPoolElement::new)
    );

    protected WaterCheckingPoolElement(Either<ResourceLocation, StructureTemplate> template,
                                       Holder<StructureProcessorList> processors,
                                       StructureTemplatePool.Projection projection) {
        super(template, processors, projection, Optional.empty());
    }

    @Override
    public StructurePoolElementType<?> getType() {
        // We will register this in Step 2
        return MementoBetaStructures.WATER_CHECKING_TYPE.get();
    }

    @Override
    public boolean place(StructureTemplateManager structureTemplateManager,
                         WorldGenLevel level,
                         StructureManager structureManager,
                         ChunkGenerator generator,
                         BlockPos offset,
                         BlockPos pos,
                         Rotation rotation,
                         BoundingBox box,
                         RandomSource random,
                         LiquidSettings liquidSettings,
                         boolean keepJigsaws) {
        // 1. Get the bounding box size of the house being placed
        StructureTemplate template = structureTemplateManager.getOrCreate(this.template.orThrow());
        Vec3i size = template.getSize(rotation);

        int waterBlocks = 0;
        int totalChecked = 0;

        // 2. Scan the footprint area under the house (checking every 2 blocks for performance)
        for (int x = 0; x < size.getX(); x += 2) {
            for (int z = 0; z < size.getZ(); z += 2) {
                BlockPos checkPos = pos.offset(x, -1, z); // Check block directly below the house floor
                totalChecked++;

                if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                    waterBlocks++;
                }
            }
        }

        // 3. If more than 20% of the foundation is water, abort generating this specific structure
        if (totalChecked > 0 && ((double) waterBlocks / totalChecked) > 0.2) {
            return false; // Skip placement
        }

        // 4. Otherwise, proceed with vanilla house placement logic
        return super.place(structureTemplateManager, level, structureManager, generator,
                pos, offset, rotation, box, random, liquidSettings, keepJigsaws);
    }
}
