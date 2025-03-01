package net.arthurllew.mementobeta.world;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class BiomeGenBase {
    public static final BiomeGenBase rainforest = (new BiomeGenBase()).setColor(588342).setBiomeName("Rainforest").func_4124_a(2094168);
    public static final BiomeGenBase swampland = (new BiomeGenBase()).setColor(522674).setBiomeName("Swampland").func_4124_a(9154376);
    public static final BiomeGenBase seasonalForest = (new BiomeGenBase()).setColor(10215459).setBiomeName("Seasonal Forest");
    public static final BiomeGenBase forest = (new BiomeGenBase()).setColor(353825).setBiomeName("Forest").func_4124_a(5159473);
    public static final BiomeGenBase savanna = (new BiomeGenBase()).setColor(14278691).setBiomeName("Savanna");
    public static final BiomeGenBase shrubland = (new BiomeGenBase()).setColor(10595616).setBiomeName("Shrubland");
    public static final BiomeGenBase taiga = (new BiomeGenBase()).setColor(3060051).setBiomeName("Taiga").setEnableSnow().func_4124_a(8107825);
    public static final BiomeGenBase desert = (new BiomeGenBase()).setColor(16421912).setBiomeName("Desert").setDisableRain();
    public static final BiomeGenBase plains = (new BiomeGenBase()).setColor(16767248).setBiomeName("Plains");
    public static final BiomeGenBase iceDesert = (new BiomeGenBase()).setColor(16772499).setBiomeName("Ice Desert").setEnableSnow().setDisableRain().func_4124_a(12899129);
    public static final BiomeGenBase tundra = (new BiomeGenBase()).setColor(5762041).setBiomeName("Tundra").setEnableSnow().func_4124_a(12899129);

    public String biomeName;
    public int color;
    public Block topBlock = Blocks.GRASS_BLOCK;
    public Block fillerBlock = Blocks.DIRT;
    public int field_6502_q = 5169201;
    private boolean enableSnow;
    private boolean enableRain = true;
    private static BiomeGenBase[] biomeLookupTable = new BiomeGenBase[4096];

    protected BiomeGenBase() {

    }

    private BiomeGenBase setDisableRain() {
        this.enableRain = false;
        return this;
    }

    public static void generateBiomeLookup() {
        for(int x = 0; x < 64; ++x) {
            for(int z = 0; z < 64; ++z) {
                biomeLookupTable[x + z * 64] = getBiome((float)x / 63.0F, (float)z / 63.0F);
            }
        }

        desert.topBlock = desert.fillerBlock = Blocks.SAND;
        iceDesert.topBlock = iceDesert.fillerBlock = Blocks.SAND;
    }

    protected BiomeGenBase setEnableSnow() {
        this.enableSnow = true;
        return this;
    }

    protected BiomeGenBase setBiomeName(String var1) {
        this.biomeName = var1;
        return this;
    }

    protected BiomeGenBase func_4124_a(int var1) {
        this.field_6502_q = var1;
        return this;
    }

    protected BiomeGenBase setColor(int var1) {
        this.color = var1;
        return this;
    }

    public static BiomeGenBase getBiomeFromLookup(double x, double y) {
        int var4 = (int)(x * 63.0D);
        int var5 = (int)(y * 63.0D);
        return biomeLookupTable[var4 + var5 * 64];
    }

    public static BiomeGenBase getBiome(float var0, float var1) {
        var1 *= var0;
        return var0 < 0.1F ? tundra : (var1 < 0.2F ? (var0 < 0.5F ? tundra : (var0 < 0.95F ? savanna : desert)) : (var1 > 0.5F && var0 < 0.7F ? swampland : (var0 < 0.5F ? taiga : (var0 < 0.97F ? (var1 < 0.35F ? shrubland : forest) : (var1 < 0.45F ? plains : (var1 < 0.9F ? seasonalForest : rainforest))))));
    }

    static {
        generateBiomeLookup();
    }
}
