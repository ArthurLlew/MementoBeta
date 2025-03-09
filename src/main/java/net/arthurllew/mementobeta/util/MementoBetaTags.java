package net.arthurllew.mementobeta.util;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MementoBetaTags {
    /**
     * Resonance tools tag.
     */
    public static final TagKey<Block> NEEDS_RESONANCE_TOOL =
            BlockTags.create(new ResourceLocation(MementoBeta.MODID, "needs_resonance_tool"));
}
