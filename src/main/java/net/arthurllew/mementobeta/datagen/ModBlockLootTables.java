package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBetaContent;
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
        this.dropSelf(MementoBetaContent.REINFORCED_BEDROCK.get());

        // Drop their solid counterpart
        this.dropOther(MementoBetaContent.MOLTEN_BEDROCK.get(), Items.BEDROCK);
        this.dropOther(MementoBetaContent.MOLTEN_REINFORCED_DEEPSLATE.get(), Items.REINFORCED_DEEPSLATE);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return MementoBetaContent.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
