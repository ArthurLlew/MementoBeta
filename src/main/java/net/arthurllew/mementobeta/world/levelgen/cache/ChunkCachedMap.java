package net.arthurllew.mementobeta.world.levelgen.cache;

public abstract class ChunkCachedMap<T> {
    /**
     * @param idx flat array index
     * @return stored data
     */
    public abstract T get(int idx);

    /**
     * @return entire stored data
     */
    public abstract T[] getAll();

    /**
     * @param localX chunk local X coordinate
     * @param localZ chunk local Z coordinate
     * @return stored data
     */
    public T get(int localX, int localZ) {
        return this.get(this.getIndex(localX, localZ));
    }

    /**
     * Maps local chunk coordinates to flat index
     * @param localX chunk local X coordinate
     * @param localZ chunk local Z coordinate
     * @return flat array index
     */
    protected int getIndex(int localX, int localZ) {
        return localX * 16 + localZ;
    }
}
