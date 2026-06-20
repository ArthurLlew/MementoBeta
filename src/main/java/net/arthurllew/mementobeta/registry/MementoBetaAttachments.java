package net.arthurllew.mementobeta.registry;

import com.mojang.serialization.Codec;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.BetaPlayerAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelSeasonAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelSeedAttachment;
import net.arthurllew.mementobeta.attachments.BetaLevelTimeAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@SuppressWarnings("unused")
public abstract class MementoBetaAttachments {
    /**
     * Deferred Register for attachments.
     */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, MementoBeta.MODID);

    /**
     * Beta dimension related player attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaPlayerAttachment>> BETA_PLAYER_ATTACHMENT =
            ATTACHMENTS.register(BetaPlayerAttachment.ID, () -> AttachmentType.builder(BetaPlayerAttachment::new)
                    .serialize(Codec.unit(BetaPlayerAttachment::new)).copyOnDeath().build());

    /**
     * Beta dimension seed attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaLevelSeedAttachment>> BETA_SEED_ATTACHMENT =
            ATTACHMENTS.register(BetaLevelSeedAttachment.ID, () -> AttachmentType.builder(BetaLevelSeedAttachment::new)
                    .serialize(BetaLevelSeedAttachment.CODEC).build());

    /**
     * Beta dimension time attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaLevelTimeAttachment>> BETA_TIME_ATTACHMENT =
            ATTACHMENTS.register(BetaLevelTimeAttachment.ID, () -> AttachmentType.builder(BetaLevelTimeAttachment::new)
                    .serialize(BetaLevelTimeAttachment.CODEC).build());

    /**
     * Beta dimension season attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaLevelSeasonAttachment>> BETA_SEASON_ATTACHMENT =
            ATTACHMENTS.register(BetaLevelSeasonAttachment.ID, () -> AttachmentType.builder(BetaLevelSeasonAttachment::new)
                    .serialize(BetaLevelSeasonAttachment.CODEC).build());
}
