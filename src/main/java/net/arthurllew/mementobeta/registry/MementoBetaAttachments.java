package net.arthurllew.mementobeta.registry;

import com.mojang.serialization.Codec;
import net.arthurllew.mementobeta.MementoBeta;
import net.arthurllew.mementobeta.attachments.BetaPlayerAttachment;
import net.arthurllew.mementobeta.attachments.data.BetaSeasonData;
import net.arthurllew.mementobeta.attachments.data.BetaSeedData;
import net.arthurllew.mementobeta.attachments.data.BetaTimeData;
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
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Long>> BETA_SEED_ATTACHMENT =
            ATTACHMENTS.register(BetaSeedData.ID, () -> AttachmentType.builder(() -> 0L)
                    .serialize(Codec.LONG).build());

    /**
     * Beta dimension time attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaTimeData>> BETA_TIME_ATTACHMENT =
            ATTACHMENTS.register(BetaTimeData.ID, () -> AttachmentType.builder(BetaTimeData::new)
                    .serialize(BetaTimeData.CODEC).build());

    /**
     * Beta dimension season attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<BetaSeasonData>> BETA_SEASON_ATTACHMENT =
            ATTACHMENTS.register(BetaSeasonData.ID, () -> AttachmentType.builder(BetaSeasonData::new)
                    .serialize(BetaSeasonData.CODEC).build());
}
