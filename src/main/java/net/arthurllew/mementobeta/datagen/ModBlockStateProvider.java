package net.arthurllew.mementobeta.datagen;

import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.MementoBetaContent;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.model.generators.BlockStateProvider;
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
        blockSideTopWithItem(MementoBetaContent.REINFORCED_BEDROCK);

        // Cube with the same texture on all sides
        blockWithItem(MementoBetaContent.MOLTEN_BEDROCK);
        // Block with bottom, side and top textures
        blockSideBottomTopWithItem(MementoBetaContent.MOLTEN_REINFORCED_DEEPSLATE);
    }

    private void blockWithItem(RegistryObject<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(), cubeAll(blockRegistryObject.get()));
    }

    private void blockSideTopWithItem(RegistryObject<Block> blockRegistryObject) {
        String name = ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        simpleBlockWithItem(blockRegistryObject.get(), models().cubeTop(name,
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_side"),
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_top")));
    }

    private void blockSideBottomTopWithItem(RegistryObject<Block> blockRegistryObject) {
        String name = ForgeRegistries.BLOCKS.getKey(blockRegistryObject.get()).getPath();
        simpleBlockWithItem(blockRegistryObject.get(), models().cubeBottomTop(name,
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_side"),
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_side"),
                new ResourceLocation(MementoBeta.MODID, "block/" + name + "_top")));
    }
}
