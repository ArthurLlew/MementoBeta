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

import java.util.function.Supplier;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MementoBeta.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Block with side and top textures
        blockRotatedPillarWithItem(MementoBetaBlocks.REINFORCED_BEDROCK);

        // Cube with the same texture on all sides
        blockWithItem(MementoBetaBlocks.MOLTEN_BEDROCK);
        // Block with bottom, side and top textures
        blockSideBottomTopWithItem(MementoBetaBlocks.MOLTEN_REINFORCED_DEEPSLATE);
    }

    private void blockRotatedPillarWithItem(Supplier<RotatedPillarBlock> blockRegistryObject) {
        RotatedPillarBlock block = blockRegistryObject.get();
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

    private void blockWithItem(Supplier<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockSideBottomTopWithItem(Supplier<Block> blockRegistryObject) {
        String name = getBlockName(blockRegistryObject.get());
        simpleBlockWithItem(blockRegistryObject.get(), models().cubeBottomTop(name,
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_side"),
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_side"),
                ResourceLocation.fromNamespaceAndPath(MementoBeta.MODID, "block/" + name + "_top")));
    }

    private String getBlockName(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }
}
