package net.arthurllew.mementobeta.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

/**
 * Record used for creating basic capability providers. Is serializable (writeable) to NBT.
 * @param registeredCapability The registered {@link Capability} field.
 * @param capabilityInterface The {@link INBTSerializable}<{@link CompoundTag}> capability class.
 */
public record CapabilityProvider(Capability<?> registeredCapability, INBTSerializable<CompoundTag> capabilityInterface)
        implements ICapabilitySerializable<CompoundTag> {
    /**
     * @return serialized capability.
     */
    @Override
    public CompoundTag serializeNBT() {
        return this.capabilityInterface().serializeNBT();
    }

    /**
     * @param compound compound to deserialize into capability.
     */
    @Override
    public void deserializeNBT(CompoundTag compound) {
        this.capabilityInterface().deserializeNBT(compound);
    }

    /**
     * Warning for "unchecked" is suppressed because the generic cast is fine for capabilities.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> capability, Direction side) {
        if (capability == this.registeredCapability()) {
            return LazyOptional.of(() -> (T) this.capabilityInterface());
        }
        return LazyOptional.empty();
    }
}
