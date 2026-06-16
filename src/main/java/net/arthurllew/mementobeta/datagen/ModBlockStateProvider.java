package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.registry.MementoBetaBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

@SuppressWarnings("SameParameterValue")
public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MementoBeta.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Block with side and top textures
        blockRotatedPillarWithItem(MementoBetaBlocks.REINFORCED_BEDROCK.get());

        // Cube with the same texture on all sides
        blockWithItem(MementoBetaBlocks.MOLTEN_BEDROCK.get());
        // Block with bottom, side and top textures
        blockSideBottomTopWithItem(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE.get());

        // Terrain blocks
        blockWithItem(MementoBetaBlocks.PACKED_DIRT.get());

        // Sapling and leaves
        leavesWithItem(MementoBetaBlocks.BTA_BUSH_LEAVES.get());
        sapling(MementoBetaBlocks.BTA_BUSH_SAPLING.get());
    }

    private void blockRotatedPillarWithItem(RotatedPillarBlock block) {
        String name = getBlockName(block);

        // Textures
        ResourceLocation side = blockTexture(block);
        ResourceLocation top = ResourceLocation.fromNamespaceAndPath(side.getNamespace(), side.getPath() + "_top");

        // Models
        ModelFile modelSide = models().cubeColumn(name, side, top);
        ModelFile modelTop = models().cubeColumnHorizontal(name + "_horizontal", side, top);

        // Block state and item
        axisBlock(block, modelSide, modelTop);
        simpleBlockItem(block, modelSide);
    }

    private void blockWithItem(Block block) {
        simpleBlockWithItem(block, cubeAll(block));
    }

    private void blockSideBottomTopWithItem(Block block) {
        String name = getBlockName(block);
        simpleBlockWithItem(block, models().cubeBottomTop(name,
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_side"),
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_side"),
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_top")));
    }

    private void leavesWithItem(Block block) {
        String name = getBlockName(block);
        simpleBlockWithItem(block, models().leaves(name,
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name)));
    }

    private void sapling(Block block) {
        String name = getBlockName(block);
        simpleBlock(block, models().cross(name,
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name))
                .renderType("cutout"));
    }

    private String getBlockName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
