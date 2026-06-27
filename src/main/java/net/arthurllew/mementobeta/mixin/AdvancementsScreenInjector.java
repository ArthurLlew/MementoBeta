package net.arthurllew.mementobeta.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.arthurllew.mementobeta.MementoBeta;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.lang.reflect.Field;

@SuppressWarnings({"AddedMixinMembersNamePattern", "DataFlowIssue"})
@Mixin(AdvancementsScreen.class)
public abstract class AdvancementsScreenInjector {
    /**
     * ID of advancements tab.
     */
    @Unique
    private static final ResourceLocation TARGET_TAB_ID = ResourceLocation
            .fromNamespaceAndPath(MementoBeta.MODID, "achievements");

    // Текстуры блоков для рендеринга сетки
    @Unique
    private static final ResourceLocation DIAMOND_ORE = ResourceLocation.withDefaultNamespace("textures/block/diamond_ore.png");
    @Unique
    private static final ResourceLocation REDSTONE_ORE = ResourceLocation.withDefaultNamespace("textures/block/redstone_ore.png");
    @Unique
    private static final ResourceLocation IRON_ORE = ResourceLocation.withDefaultNamespace("textures/block/iron_ore.png");
    @Unique
    private static final ResourceLocation COAL_ORE = ResourceLocation.withDefaultNamespace("textures/block/coal_ore.png");
    @Unique
    private static final ResourceLocation STONE = ResourceLocation.withDefaultNamespace("textures/block/stone.png");
    @Unique
    private static final ResourceLocation DIRT = ResourceLocation.withDefaultNamespace("textures/block/dirt.png");
    @Unique
    private static final ResourceLocation BEDROCK = ResourceLocation.withDefaultNamespace("textures/block/bedrock.png");

    /**
     * Access to currently selected tab
     */
    @Shadow
    private @Nullable AdvancementTab selectedTab;

    /**
     * Injects code into {@link AdvancementsScreen}. Allows to render custom advancement tab background.
     */
    @Inject(method = "renderInside", at = @At("HEAD"))
    private void injectRenderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int offsetX, int offsetY,
                                    CallbackInfo ci) {
        // If there is a selected tab
        if (this.selectedTab != null) {
            // Ignore exceptions
            try {
                // Try to get root node of advancement tab via reflection
                Field rootNodeField = AdvancementTab.class.getDeclaredField("rootNode");
                rootNodeField.setAccessible(true);
                AdvancementNode rootNode =
                        (AdvancementNode) rootNodeField.get(this.selectedTab);

                // If it exists and
                if (rootNode != null && rootNode.holder().id().equals(TARGET_TAB_ID)) {
                    // Get X scrolling coordinate via reflection
                    Field scrollXField = AdvancementTab.class.getDeclaredField("scrollX");
                    scrollXField.setAccessible(true);
                    double scrollX = scrollXField.getDouble(this.selectedTab);
                    // Get Y scrolling coordinate via reflection
                    Field scrollYField = AdvancementTab.class.getDeclaredField("scrollY");
                    scrollYField.setAccessible(true);
                    double scrollY = scrollYField.getDouble(this.selectedTab);

                    // Screen coordinates
                    int screenWidth = (((Screen)(Object)this).width - 252) / 2;
                    int screenHeight = (((Screen)(Object)this).height - 140) / 2;

                    // Size of inner rendering window
                    int insideX = screenWidth + 9;
                    int insideY = screenHeight + 18;
                    int insideWidth = 234;
                    int insideHeight = 113;

                    // Enable scissor (do not render outside of widget)
                    guiGraphics.enableScissor(insideX, insideY, insideX + insideWidth, insideY + insideHeight);

                    // Number of blocks visible inside the advancements window
                    int visibleBlocksX = (insideWidth / 16) + (insideWidth % 16 == 0 ? 0 : 1) + 2;
                    int visibleBlocksY = (insideHeight / 16) + (insideHeight % 16 == 0 ? 0 : 1) + 2;

                    // User scrolling in pixels (scrolling values are <= 0)
                    int pixScrollX = -Mth.floor(scrollX);
                    int pixScrollY = -Mth.floor(scrollY);

                    // "Camera" pixel position (position of the root node + scroll in pixels)
                    int cameraX = 400 - 8 + pixScrollX;
                    int cameraY = 48 - 8 + pixScrollY;

                    // Scrolling (shift) of "camera" in blocks
                    int scrollXBlocks = Math.floorDiv(cameraX, 16);
                    int scrollYBlocks = Math.floorDiv(cameraY, 16);

                    // Scrolling (shift) of "camera" in pixels inside one block
                    int renderScrollX = Math.floorMod(cameraX, 16);
                    int renderScrollY = Math.floorMod(cameraY, 16);

                    // ================================================================================================
                    // God bless Notch for (kinda) this code in Vanilla Beta 1.7.3    :)
                    // ================================================================================================

                    // Modern random
                    RandomSource random = RandomSource.create();

                    // For row inside box of visible blocks
                    for (int row = 0; row < visibleBlocksY; ++row) {
                        // Current block global Y
                        int currentBlockY = scrollYBlocks + row;

                        // In beta 1.7.3 brightness of background depended on depth
                        float brightness = 0.6F - (float)currentBlockY / 25.0F * 0.3F;
                        brightness = Mth.clamp(brightness, 0.15F, 0.6F);
                        RenderSystem.setShaderColor(brightness, brightness, brightness, 1.0F);

                        // For columns inside box of visible blocks
                        for (int column = 0; column < visibleBlocksX; ++column) {
                            // Current block global X
                            int currentBlockX = scrollXBlocks + column;

                            // Each column has random, but predictable (same random) ore generation
                            random.setSeed(1234 + currentBlockX);
                            random.nextInt();

                            // Random choice of block
                            int randomBlockValue = random.nextInt(1 + currentBlockY) + currentBlockY / 2;
                            ResourceLocation blockTexture = DIRT;
                            if (randomBlockValue <= 37 && currentBlockY != 35) {
                                if (randomBlockValue == 22) {
                                    if (random.nextInt(2) == 0) {
                                        blockTexture = DIAMOND_ORE;
                                    } else {
                                        blockTexture = REDSTONE_ORE;
                                    }
                                } else if (randomBlockValue == 10) {
                                    blockTexture = IRON_ORE;
                                } else if (randomBlockValue == 8) {
                                    blockTexture = COAL_ORE;
                                } else if (randomBlockValue > 4) {
                                    blockTexture = STONE;
                                } else if (randomBlockValue > 0) {
                                    blockTexture = DIRT;
                                }
                            } else {
                                blockTexture = BEDROCK;
                            }

                            // Pixel coordinates of current block
                            int renderX = insideX + (column * 16) - 16 - renderScrollX;
                            int renderY = insideY + (row * 16) - 16 - renderScrollY;

                            // Draw current block
                            guiGraphics.blit(blockTexture, renderX, renderY, 0.0F, 0.0F,
                                    16, 16, 16, 16);
                        }
                    }

                    // Reset shader brightness
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                    // Disable scissor
                    guiGraphics.disableScissor();
                }
            }
            catch (Exception ignored) {}
        }
    }
}
