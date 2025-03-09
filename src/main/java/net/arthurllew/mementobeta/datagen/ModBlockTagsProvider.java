package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.arthurllew.mementobeta.util.MementoBetaTags;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
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
        // Resonance tools tag
        this.tag(MementoBetaTags.NEEDS_RESONANCE_TOOL)
                .add(MementoBetaContent.REINFORCED_BEDROCK.get())
                .addTag(Tags.Blocks.ORES);

        // Pickaxe tool tag
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaContent.MOLTEN_BEDROCK.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaContent.MOLTEN_REINFORCED_DEEPSLATE.get());
        this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(MementoBetaContent.REINFORCED_BEDROCK.get());
    }
}
