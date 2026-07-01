package net.arthurllew.mementobeta.world.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.arthurllew.mementobeta.registry.MementoBetaStructures;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
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
public class HeightCheckingPoolElement extends SinglePoolElement {
    public static final MapCodec<HeightCheckingPoolElement> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(templateCodec(), processorsCodec(), projectionCodec())
                    .apply(instance, HeightCheckingPoolElement::new)
    );

    protected HeightCheckingPoolElement(Either<ResourceLocation, StructureTemplate> template,
                                        Holder<StructureProcessorList> processors,
                                        StructureTemplatePool.Projection projection) {
        super(template, processors, projection, Optional.empty());
    }

    @Override
    public StructurePoolElementType<?> getType() {
        return MementoBetaStructures.HEIGHT_CHECKING_TYPE.get();
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
        // Check placement height
        if (pos.getY() < generator.getSeaLevel() + 5) {
            // Skip placement
            return false;
        }

        // Proceed with vanilla house placement logic
        return super.place(structureTemplateManager, level, structureManager, generator,
                pos, offset, rotation, box, random, liquidSettings, keepJigsaws);
    }
}
