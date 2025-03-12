package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, MementoBeta.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        // Block with side and top textures
        blockRotatedPillarWithItem(MementoBetaContent.REINFORCED_BEDROCK);

        // Cube with the same texture on all sides
        blockWithItem(MementoBetaContent.MOLTEN_BEDROCK);
        // Block with bottom, side and top textures
        blockSideBottomTopWithItem(MementoBetaContent.MOLTEN_REINFORCED_DEEPSLATE);
    }

    private void blockRotatedPillarWithItem(RegistryObject<RotatedPillarBlock> blockRegistryObject) {
        RotatedPillarBlock block = blockRegistryObject.get();
        String name = getBlockName(block);

        // Textures
        ResourceLocation side = blockTexture(block);
        ResourceLocation top = new ResourceLocation(side.getNamespace(), side.getPath() + "_top");

        // Models
        ModelFile modelSide = models().cubeColumn(name, side, top);
        ModelFile modelTop = models().cubeColumnHorizontal(name + "_horizontal", side, top);

        // Block state and item
        axisBlock(block, modelSide, modelTop);
        simpleBlockItem(block, modelSide);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockSideBottomTopWithItem(RegistryObject<Block> blockRegistryObject) {
        String name = getBlockName(blockRegistryObject.get());
        simpleBlockWithItem(blockRegistryObject.get(), models().cubeBottomTop(name,
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_side"),
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_side"),
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_top")));
    }

    private String getBlockName(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).getPath();
    }
}
