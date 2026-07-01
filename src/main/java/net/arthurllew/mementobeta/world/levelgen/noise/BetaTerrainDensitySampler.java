package net.arthurllew.mementobeta.world.levelgen.noise;

import net.minecraft.util.Mth;

public abstract class BetaTerrainDensitySampler {
    /**
     * Samples density.
     * @param localX chunk local X [0,15]
     * @param localY chunk local Y [0,127]
     * @param localZ chunk local Z [0,15]
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density
     */
    public static double sampleDensity(int localX, int localY, int localZ,
                                       double[] terrainNoise, int sizeY, int sizeZ) {
        // 4x8x4 noise grid cell coordinates
        int iX = localX >> 2;
        int iY = localY >> 3;
        int iZ = localZ >> 2;

        // Interpolation weights (0.0 to 1.0) inside the cell
        double fadeX = (localX & 3) * 0.25;
        double fadeY = (localY & 7) * 0.125;
        double fadeZ = (localZ & 3) * 0.25;

        // Use already existing 3D linear interpolations
        return Mth.lerp3(
                fadeX, fadeY, fadeZ,
                terrainNoise[getNoiseFlatIndex(iX, iY, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ + 1, sizeY, sizeZ)],
                terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ + 1, sizeY, sizeZ)]
        );
    }

    /**
     * Samples density column.
     * @param localX chunk local X [0,15]
     * @param localZ chunk local Z [0,15]
     * @param height density column size
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density column
     */
    public static double[] sampleDensityColumn(int localX, int localZ, int height,
                                               double[] terrainNoise, int sizeY, int sizeZ) {
        // 4x8x4 noise grid cell coordinates
        int iX = localX >> 2;
        int iZ = localZ >> 2;

        // Interpolation weights (0.0 to 1.0) inside the cell
        double fadeX = (localX & 3) * 0.250;
        double fadeZ = (localZ & 3) * 0.250;

        // Helper noise arrays
        double[] lowYNoise = new double[sizeY];
        double[] highYNoise = new double[sizeY];

        // Fill helper arrays with noise interpolation
        for (int iY = 0; iY < sizeY - 1; iY++) {
            lowYNoise[iY] = Mth.lerp2(
                    fadeX, fadeZ,
                    terrainNoise[getNoiseFlatIndex(iX, iY, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX, iY, iZ + 1, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY, iZ + 1, sizeY, sizeZ)]
            );
            highYNoise[iY] = Mth.lerp2(
                    fadeX, fadeZ,
                    terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX, iY + 1, iZ + 1, sizeY, sizeZ)],
                    terrainNoise[getNoiseFlatIndex(iX + 1, iY + 1, iZ + 1, sizeY, sizeZ)]
            );
        }

        // Fill in and return density column
        double[] density = new double[height];
        for (int localY = 0; localY < height; localY++) {
            // Coarse 4x8x4 noise grid cell coordinate
            int iY = localY >> 3;
            // Interpolation weight (0.0 to 1.0) inside the cell
            double fadeY = (localY & 7) * 0.125;

            density[localY] = Mth.lerp(fadeY, lowYNoise[iY], highYNoise[iY]);
        }
        return density;
    }

    /**
     * Samples density column.
     * @param localX chunk local X [0,15]
     * @param localZ chunk local Z [0,15]
     * @param terrainNoise terrain coarse noise
     * @param sizeY noise Y size
     * @param sizeZ noise Z size
     * @return sampled density column
     */
    public static double[] sampleDensityColumn(int localX, int localZ, double[] terrainNoise, int sizeY, int sizeZ) {
        return sampleDensityColumn(localX, localZ, 128, terrainNoise, sizeY, sizeZ);
    }

    /**
     * Helper to calculate flat array index from 3D grid coordinates.
     * @param iX array X index [0,sizeX-1]
     * @param iY array Y index [0,sizeY-1]
     * @param iZ array Z index [0,sizeZ-1]
     * @param sizeY array Y size
     * @param sizeZ array Z size
     * @return array index
     */
    private static int getNoiseFlatIndex(int iX, int iY, int iZ, int sizeY, int sizeZ) {
        return (iX * sizeZ + iZ) * sizeY + iY;
    }
}
