package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModBlockLootTables extends BlockLootSubProvider {
    protected ModBlockLootTables(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    public void generate() {
        // Drop themselves
        this.dropSelf(MementoBetaBlocks.REINFORCED_BEDROCK.get());
        this.dropSelf(MementoBetaBlocks.PACKED_DIRT.get());
        this.dropSelf(MementoBetaBlocks.BTA_BUSH_SAPLING.get());

        // Drop their solid counterpart
        this.dropOther(MementoBetaBlocks.MOLTEN_BEDROCK.get(), Blocks.BEDROCK);
        this.dropOther(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get(), Blocks.REINFORCED_DEEPSLATE);

        // Leaves drops
        this.add(MementoBetaBlocks.BTA_BUSH_LEAVES.get(), this.createLeavesDrops(
                        MementoBetaBlocks.BTA_BUSH_LEAVES.get(),
                        MementoBetaBlocks.BTA_BUSH_SAPLING.get(),
                        0.05F, 0.0625F, 0.083333336F, 0.1F));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // Get blocks collection
        return MementoBetaBlocks.BLOCKS.getEntries().stream().map(Holder::value)
                // Filter out unwanted blocks
                .filter(block ->
                           block != MementoBetaBlocks.BETA_PORTAL.value()
                        && block != MementoBetaBlocks.BETA_PORTAL_NETHER.value()
                        && block != MementoBetaBlocks.BETA_FIRE.value()
                        && block != MementoBetaBlocks.BETA_lAVA.value()
                )::iterator;
    }
}
