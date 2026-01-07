package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
                                    @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, MementoBeta.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Diamond tools tag
        this.tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(MementoBetaBlocks.MOLTEN_BEDROCK.get())
                .add(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get());
        // Netherite tools tag
        this.tag(Tags.Blocks.NEEDS_NETHERITE_TOOL)
                .add(MementoBetaBlocks.REINFORCED_BEDROCK.get());

        // Pickaxe tool tag
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaBlocks.MOLTEN_BEDROCK.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaBlocks.REINFORCED_BEDROCK.get());
    }
}
