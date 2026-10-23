package com.custommusic.client.screen.widget;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class VerticalSlider extends AbstractSliderButton {
    private static final Field DRAGGING_FIELD;

    static {
        try {
            DRAGGING_FIELD = AbstractSliderButton.class.getDeclaredField("dragging");
            DRAGGING_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final Consumer<Double> onChange;

    public VerticalSlider(int x, int y, int width, int height, Component message, double initialValue, Consumer<Double> onChange) {
        super(x, y, width, height, message, initialValue);
        this.onChange = onChange;
        this.updateMessage();
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = this.getX();
        int y = this.getY();
        int w = this.getWidth();
        int h = this.getHeight();

        // Track background
        graphics.fill(x, y, x + w, y + h, 0xFF2C2C2C);

        // Filled portion from the bottom up (same green as the song scrubber)
        int fillHeight = (int) (this.value * h);
        int fillY = y + h - fillHeight;
        graphics.fill(x, fillY, x + w, y + h, com.custommusic.client.MusicScrubber.SCRUB_GREEN);

        // Horizontal handle, kept within the track width
        int handleY = Mth.clamp(fillY - 2, y, y + h - 4);
        int handleColor = this.isHovered() ? 0xFFFFFFFF : 0xFFA0A0A0;
        graphics.fill(x, handleY, x + w, handleY + 4, handleColor);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        try {
            DRAGGING_FIELD.setBoolean(this, this.active);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        this.setFocused(true);
        this.setValueFromMouse(event);
    }

    @Override
    protected void onDrag(MouseButtonEvent event, double dx, double dy) {
        this.setValueFromMouse(event);
    }

    private void setValueFromMouse(MouseButtonEvent event) {
        double clampedY = Mth.clamp(event.y(), this.getY(), this.getY() + this.getHeight());
        double newValue = 1.0 - ((double) (clampedY - this.getY()) / this.getHeight());
        this.setValue(newValue);
    }

    @Override
    protected void setValue(double value) {
        double oldValue = this.value;
        this.value = Mth.clamp(value, 0.0, 1.0);
        this.updateMessage();
        if (Math.abs(oldValue - this.value) > 1.0E-5) {
            this.applyValue();
        }
    }

    /**
     * Updates the slider value without notifying the change listener.
     * Use this when syncing from server state to avoid packet loops.
     */
    public void setValueFromServer(double value) {
        double oldValue = this.value;
        this.value = Mth.clamp(value, 0.0, 1.0);
        this.updateMessage();
    }

    public double getCurrentValue() {
        return this.value;
    }

    public boolean isDragging() {
        try {
            return DRAGGING_FIELD.getBoolean(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.isSelection()) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        }

        if (this.canChangeValue) {
            boolean up = event.isUp() || event.isRight();
            boolean down = event.isDown() || event.isLeft();
            if (up || down) {
                float direction = up ? 1.0F : -1.0F;
                this.setValue(this.value + direction * (4.0 / this.getHeight()));
                return true;
            }
        }

        return false;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.literal((int) (this.value * 100) + "%"));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this.value);
    }
}
