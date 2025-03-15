package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.block.MementoBetaBlocks;
import net.arthurllew.mementobeta.fluid.MementoBetaFluids;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Set;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModBlockLootTables extends BlockLootSubProvider {
    protected ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    public void generate() {
        // Drop themselves
        this.dropSelf(MementoBetaBlocks.REINFORCED_BEDROCK.get());

        // Drop their solid counterpart
        this.dropOther(MementoBetaBlocks.MOLTEN_BEDROCK.get(), Items.BEDROCK);
        this.dropOther(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get(), Items.REINFORCED_DEEPSLATE);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        // All blocks except portal and flame
        return MementoBetaBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)
                .filter(block -> block != MementoBetaBlocks.BETA_PORTAL.get()
                        && block != MementoBetaBlocks.BETA_FIRE.get()
                        && block != MementoBetaFluids.BETA_lAVA_BLOCK.get())::iterator;
    }
}
