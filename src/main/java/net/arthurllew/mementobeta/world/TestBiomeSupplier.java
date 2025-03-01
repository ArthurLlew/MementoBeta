package net.arthurllew.mementobeta.world;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Heightmap;

public class TestBiomeSupplier implements BiomeResolver {
    Holder<Biome> biome1;
    Holder<Biome> biome2;
    Heightmap heightmap;

    public TestBiomeSupplier(Heightmap heightmap, Registry<Biome> registry) {
        this.heightmap = heightmap;
        this.biome1 = registry.getHolder(Biomes.PLAINS).get();
        this.biome2 = registry.getHolder(Biomes.OCEAN).get();
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        int height = this.heightmap.getFirstAvailable(SectionPos.sectionRelative(x), SectionPos.sectionRelative(z));
        if (height < 64) {
            return biome2;
        }
        return biome1;
    }
}
