package com.custommusic.client.screen;

import com.custommusic.CustomMusicMod;
import com.custommusic.blockentity.JukeboxAlbumBlockEntity;
import com.custommusic.menu.JukeboxAlbumMenu;
import com.custommusic.client.screen.widget.VerticalSlider;
import com.custommusic.client.audio.MusicManager;
import com.custommusic.network.ButtonClickPayload;
import com.custommusic.network.JukeboxVolumePayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class JukeboxAlbumScreen extends AbstractContainerScreen<JukeboxAlbumMenu> {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int SLOT_SIZE = 18;
    private static final int SLOTS_PER_ROW = 9;
    private static final int ROWS = 6;
    
    private Button playPauseButton;
    private Button nextSongButton;
    private Button previousSongButton;
    private Button nextPageButton;
    private Button previousPageButton;
    private VerticalSlider volumeSlider;
    private float lastSentVolume = -1.0F;
    private long lastSliderTouchTime = 0L;

    private int lastKnownPage = -1;

    // Scrub bar state (bar drawn below the panel)
    private boolean scrubbing = false;
    private float scrubFraction = 0F;
    private int scrubLengthTicks = 0;
    // Last seek position sent to the server; keeps the bar from snapping back
    // to the old position while the state update is in flight.
    private int pendingSeekTicks = -1;
    private long pendingSeekTime = 0L;

    public JukeboxAlbumScreen(JukeboxAlbumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 176, 222);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        
        int rightEdge = this.leftPos + this.imageWidth;
        int topEdge = this.topPos;
        
        this.playPauseButton = Button.builder(Component.literal("▶"), button -> {
            sendButtonClick(ButtonClickPayload.ButtonType.PLAY_PAUSE, -1);
        }).bounds(rightEdge + 5, topEdge + 10, 20, 20).build();
        this.addRenderableWidget(this.playPauseButton);
        
        this.previousSongButton = Button.builder(Component.literal("<<"), button -> {
            sendButtonClick(ButtonClickPayload.ButtonType.PREVIOUS_SONG, -1);
        }).bounds(rightEdge + 5, topEdge + 35, 20, 20).build();
        this.addRenderableWidget(this.previousSongButton);
        
        this.nextSongButton = Button.builder(Component.literal(">>"), button -> {
            sendButtonClick(ButtonClickPayload.ButtonType.NEXT_SONG, -1);
        }).bounds(rightEdge + 30, topEdge + 35, 20, 20).build();
        this.addRenderableWidget(this.nextSongButton);
        
        this.previousPageButton = Button.builder(Component.literal("<"), button -> {
            sendButtonClick(ButtonClickPayload.ButtonType.PREVIOUS_PAGE, -1);
        }).bounds(rightEdge + 5, topEdge + 60, 20, 20).build();
        this.addRenderableWidget(this.previousPageButton);
        
        this.nextPageButton = Button.builder(Component.literal(">"), button -> {
            sendButtonClick(ButtonClickPayload.ButtonType.NEXT_PAGE, -1);
        }).bounds(rightEdge + 30, topEdge + 60, 20, 20).build();
        this.addRenderableWidget(this.nextPageButton);

        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            this.lastSentVolume = blockEntity.getVolume();
            this.volumeSlider = new VerticalSlider(
                rightEdge + 5,
                topEdge + 85,
                10,
                this.imageHeight - 85 - 10,
                Component.empty(),
                blockEntity.getVolume(),
                value -> sendVolume(value.floatValue())
            );
            this.addRenderableWidget(this.volumeSlider);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            if (blockEntity.isPlaying() && !blockEntity.isPaused()) {
                playPauseButton.setMessage(Component.literal("⏸"));
            } else {
                playPauseButton.setMessage(Component.literal("▶"));
            }
            
            previousPageButton.active = blockEntity.getCurrentPage() > 0;
            nextPageButton.active = blockEntity.getCurrentPage() < blockEntity.getMaxPages() - 1;
        }

        // Spotify-style seek bar below the panel, spanning from the left edge
        // to the right-aligned remaining-time text
        var playback = currentPlayback();
        if (playback != null && playback.lengthTicks() > 0) {
            int displayTicks;
            if (this.scrubbing) {
                displayTicks = (int) (this.scrubFraction * playback.lengthTicks());
            } else if (this.pendingSeekTicks >= 0
                    && System.currentTimeMillis() - this.pendingSeekTime < 1000
                    && Math.abs(playback.positionTicks() - this.pendingSeekTicks) > 10) {
                displayTicks = this.pendingSeekTicks;
            } else {
                this.pendingSeekTicks = -1;
                displayTicks = playback.positionTicks();
            }
            String text = com.custommusic.client.MusicScrubber.remainingText(playback.lengthTicks() - displayTicks);
            int textX = this.leftPos + this.imageWidth - this.font.width(text);
            int textY = this.topPos + this.imageHeight + 5;
            int barX0 = this.leftPos;
            int barX1 = textX - 4;
            com.custommusic.client.MusicScrubber.drawScrubber(guiGraphics, this.font,
                barX0, barX1, textY, 9,
                (float) displayTicks / (float) playback.lengthTicks(), text, textX, textY,
                playback.paused());
        } else {
            this.scrubbing = false;
            this.pendingSeekTicks = -1;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // Check if page has changed and refresh slots if needed
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            int currentPage = blockEntity.getCurrentPage();

            // Update button states
            previousPageButton.active = currentPage > 0;
            nextPageButton.active = currentPage < blockEntity.getMaxPages() - 1;

            // Update play/pause button
            if (blockEntity.isPlaying() && !blockEntity.isPaused()) {
                playPauseButton.setMessage(Component.literal("⏸"));
            } else {
                playPauseButton.setMessage(Component.literal("▶"));
            }

            // Sync volume slider from server state when the player is not dragging it
            // and has not touched it recently (prevents snapping back before the server ack)
            if (volumeSlider != null && !volumeSlider.isDragging()
                    && System.currentTimeMillis() - this.lastSliderTouchTime > 500) {
                float beVol = blockEntity.getVolume();
                if (Math.abs((float) volumeSlider.getCurrentValue() - beVol) > 0.01F) {
                    volumeSlider.setValueFromServer(beVol);
                    this.lastSentVolume = beVol;
                }
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            int currentPage = blockEntity.getCurrentPage();
            int startSlot = currentPage * 54;
            
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < SLOTS_PER_ROW; col++) {
                    int slotIndex = startSlot + row * SLOTS_PER_ROW + col;
                    int slotX = x + 8 + col * SLOT_SIZE;
                    int slotY = y + 18 + row * SLOT_SIZE;
                    
                    if (slotIndex == blockEntity.getCurrentlyPlayingSlot() && blockEntity.getCurrentlyPlayingSlot() >= 0) {
                        // Only show highlight if there's actually a disc in the slot
                        ItemStack slotStack = blockEntity.getItem(slotIndex);
                        if (!slotStack.isEmpty()) {
                            if (blockEntity.isPlaying() && !blockEntity.isPaused()) {
                                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x8000FF00);
                            } else if (blockEntity.isPaused()) {
                                guiGraphics.fill(slotX, slotY, slotX + 16, slotY + 16, 0x80FF0000);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        guiGraphics.text(this.font, this.title, this.titleLabelX, this.titleLabelY, -12566464, false);
        guiGraphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, -12566464, false);
        
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            String pageText = (blockEntity.getCurrentPage() + 1) + "/" + blockEntity.getMaxPages();
            int textWidth = this.font.width(pageText);
            guiGraphics.text(this.font, pageText, this.imageWidth - textWidth - 8, this.inventoryLabelY, -12566464, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Scrub bar click below the panel (left button is 1 in this version)
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && isInScrubber(event.x(), event.y())) {
            var playback = currentPlayback();
            if (playback != null && playback.lengthTicks() > 0) {
                this.scrubbing = true;
                this.scrubLengthTicks = playback.lengthTicks();
                this.scrubFraction = scrubFractionAt(event.x());
                return true;
            }
        }

        // Check for shift-click on jukebox slots
        if (event.hasShiftDown()) {
            // Convert mouse coordinates to slot position
            double slotX = event.x() - this.leftPos - 8;
            double slotY = event.y() - this.topPos - 18;
            
            if (slotX >= 0 && slotY >= 0 && slotX < 9 * 18 && slotY < 6 * 18) {
                int slotXIndex = (int) (slotX / 18);
                int slotYIndex = (int) (slotY / 18);
                int slotOnPage = slotYIndex * 9 + slotXIndex;
                int absoluteSlot = menu.getBlockEntity().getCurrentPage() * 54 + slotOnPage;
                
                // Check if this slot contains a disc
                JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
                if (blockEntity != null) {
                    ItemStack slotStack = blockEntity.getItem(absoluteSlot);
                    if (!slotStack.isEmpty()) {
                        // Check if this is the currently playing disc
                        boolean isCurrentlyPlaying = blockEntity.isPlaying() && 
                            blockEntity.getCurrentlyPlayingSlot() == absoluteSlot;
                        
                        // Send the shift-click action to extract item and stop music if playing
                        sendButtonClick(ButtonClickPayload.ButtonType.SHIFT_CLICK_SLOT, absoluteSlot);
                        return true;
                    }
                }
            }
        }
        
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.scrubbing) {
            this.scrubFraction = scrubFractionAt(event.x());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.scrubbing) {
            this.scrubbing = false;
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
                if (blockEntity != null) {
                    int seek = com.custommusic.client.MusicScrubber.sendSeek(false, blockEntity.getBlockPos(),
                        this.scrubFraction, this.scrubLengthTicks);
                    if (seek >= 0) {
                        this.pendingSeekTicks = seek;
                        this.pendingSeekTime = System.currentTimeMillis();
                    }
                }
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    private @org.jetbrains.annotations.Nullable com.custommusic.client.MusicScrubber.Playback currentPlayback() {
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity == null) return null;
        return com.custommusic.client.MusicScrubber.blockPlayback(
            net.minecraft.client.Minecraft.getInstance(), blockEntity);
    }

    private boolean isInScrubber(double mouseX, double mouseY) {
        return currentPlayback() != null
            && mouseX >= this.leftPos && mouseX <= this.leftPos + this.imageWidth
            && mouseY >= this.topPos + this.imageHeight + 3 && mouseY <= this.topPos + this.imageHeight + 16;
    }

    private float scrubFractionAt(double mouseX) {
        int barX0 = this.leftPos;
        int barX1 = this.leftPos + this.imageWidth - this.font.width("0:00") - 4;
        return (float) net.minecraft.util.Mth.clamp((mouseX - barX0) / (double) Math.max(1, barX1 - barX0), 0.0, 1.0);
    }

    private void sendButtonClick(ButtonClickPayload.ButtonType buttonType, int slot) {
        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            ClientPacketDistributor.sendToServer(new ButtonClickPayload(blockEntity.getBlockPos(), buttonType, slot));
        }
    }

    private void sendVolume(float volume) {
        if (Math.abs(volume - this.lastSentVolume) < 0.01F) {
            return;
        }
        this.lastSentVolume = volume;
        this.lastSliderTouchTime = System.currentTimeMillis();

        JukeboxAlbumBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            ClientPacketDistributor.sendToServer(new JukeboxVolumePayload(blockEntity.getBlockPos(), net.minecraft.util.Mth.clamp(volume, 0.0F, 1.0F)));
            MusicManager.setVolume("block:" + blockEntity.getBlockPos().asLong(), volume);
        }
    }
}
