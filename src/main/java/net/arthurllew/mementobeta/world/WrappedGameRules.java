package net.arthurllew.mementobeta.world;

import net.minecraft.world.level.GameRules;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Wrapper for level properties. Allows more control over game rules in custom dimension.
 */
public class WrappedGameRules extends GameRules {
    /**
     * Wrapped game rules.
     */
    private final GameRules wrappedGameRules;
    /**
     * Boolean game rules blacklist. Those rules return false regardless of wrapped rules.
     */
    private final Set<Key<GameRules.BooleanValue>> blacklist;

    /**
     * Constructor
     * @param gameRules game rules to wrap.
     * @param keys blacklist.
     */
    public WrappedGameRules(GameRules gameRules, Set<GameRules.Key<GameRules.BooleanValue>> keys) {
        this.wrappedGameRules = gameRules;
        this.blacklist = keys;
    }

    /**
     * @param key game rule key.
     * @return game rule.
     * @param <T> Game rule type.
     */
    @Override
    public <T extends GameRules.Value<T>> @NotNull T getRule(@NotNull Key<T> key) {
        return this.wrappedGameRules.getRule(key);
    }

    /**
     * @param key boolean game rule key.
     * @return boolean game rule.
     */
    @Override
    public boolean getBoolean(@NotNull Key<GameRules.BooleanValue> key) {
        return !this.blacklist.contains(key) && this.getRule(key).get();
    }
}
