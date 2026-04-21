package net.arthurllew.mementobeta.registry;

import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class MementoBetaSounds {
    /**
     * Deferred Register for sound events.
     */
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MementoBeta.MODID);

    /**
     * Lake music sound event.
     */
    public static final Holder<SoundEvent> LAKE_MUSIC = SOUND_EVENTS.register(
            "music.beta_lake",
            // Takes in the registry name
            SoundEvent::createVariableRangeEvent
    );
}
