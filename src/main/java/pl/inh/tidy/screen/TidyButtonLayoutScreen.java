package pl.inh.tidy.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
import pl.inh.tidy.Tidy;
import pl.inh.tidy.compat.TidyCompat;

public class TidyButtonLayoutScreen extends Screen {

    private static final Identifier PLAYER_TEXTURE = TidyCompat.id("minecraft", "textures/gui/container/inventory.png");
    private static final Identifier SINGLE_STORAGE_TEXTURE = TidyCompat.id("minecraft", "textures/gui/container/shulker_box.png");
    private static final Identifier DOUBLE_STORAGE_TEXTURE = TidyCompat.id("minecraft", "textures/gui/container/generic_54.png");

    private final Screen parent;
    private TidyScreens.LayoutContext activeLayout = TidyScreens.LayoutContext.PLAYER;
    private TidyScreens.ButtonKind draggingButton;
    private TidyScreens.ButtonKind hoveredButton;
    private int dragOffsetX;
    private int dragOffsetY;
    private ButtonWidget playerLayoutButton;
    private ButtonWidget singleStorageLayoutButton;
    private ButtonWidget doubleStorageLayoutButton;

    public TidyButtonLayoutScreen(Screen parent) {
        super(TidyCompat.translatable("screen.tidy.button_layout"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int selectorY = 56;
        int selectorWidth = 110;
        int selectorGap = 6;
        int selectorTotalWidth = selectorWidth * 3 + selectorGap * 2;
        int selectorStartX = this.width / 2 - selectorTotalWidth / 2;

        this.playerLayoutButton = this.addDrawableChild(TidyCompat.createButton(
                selectorStartX,
                selectorY,
                selectorWidth,
                20,
                TidyCompat.translatable("button.tidy.layout_player"),
                button -> selectLayout(TidyScreens.LayoutContext.PLAYER)
        ));
        this.singleStorageLayoutButton = this.addDrawableChild(TidyCompat.createButton(
                selectorStartX + selectorWidth + selectorGap,
                selectorY,
                selectorWidth,
                20,
                TidyCompat.translatable("button.tidy.layout_single_storage"),
                button -> selectLayout(TidyScreens.LayoutContext.SINGLE_STORAGE)
        ));
        this.doubleStorageLayoutButton = this.addDrawableChild(TidyCompat.createButton(
                selectorStartX + (selectorWidth + selectorGap) * 2,
                selectorY,
                selectorWidth,
                20,
                TidyCompat.translatable("button.tidy.layout_double_storage"),
                button -> selectLayout(TidyScreens.LayoutContext.DOUBLE_STORAGE)
        ));

        int footerY = this.height - 28;
        this.addDrawableChild(TidyCompat.createButton(
                this.width / 2 - 224,
                footerY,
                118,
                20,
                TidyCompat.translatable("button.tidy.reset_button_layout"),
                button -> resetLayout()
        ));
        this.addDrawableChild(TidyCompat.createButton(
                this.width / 2 - 100,
                footerY,
                36,
                20,
                TidyCompat.translatable("button.tidy.scale_down"),
                button -> adjustScale(-5)
        ));
        this.addDrawableChild(TidyCompat.createButton(
                this.width / 2 - 58,
                footerY,
                36,
                20,
                TidyCompat.translatable("button.tidy.scale_up"),
                button -> adjustScale(5)
        ));
        this.addDrawableChild(TidyCompat.createButton(
                this.width / 2 + 24,
                footerY,
                118,
                20,
                TidyCompat.translatable("gui.done"),
                button -> close()
        ));

        refreshLayoutButtons();
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }

    private void selectLayout(TidyScreens.LayoutContext layoutContext) {
        this.activeLayout = layoutContext;
        this.draggingButton = null;
        refreshLayoutButtons();
    }

    private void refreshLayoutButtons() {
        if (this.playerLayoutButton != null) {
            this.playerLayoutButton.active = this.activeLayout != TidyScreens.LayoutContext.PLAYER;
        }
        if (this.singleStorageLayoutButton != null) {
            this.singleStorageLayoutButton.active = this.activeLayout != TidyScreens.LayoutContext.SINGLE_STORAGE;
        }
        if (this.doubleStorageLayoutButton != null) {
            this.doubleStorageLayoutButton.active = this.activeLayout != TidyScreens.LayoutContext.DOUBLE_STORAGE;
        }
    }

    //? if <=1.21.4 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        TidyScreens.ButtonKind buttonKind = buttonAt(mouseX, mouseY);
        if (button == 0 && buttonKind != null) {
            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            this.draggingButton = buttonKind;
            this.dragOffsetX = (int) mouseX - placement.x();
            this.dragOffsetY = (int) mouseY - placement.y();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.draggingButton != null && button == 0) {
            TidyScreens.updateDraggedButton(
                    this.draggingButton,
                    this.activeLayout,
                    this.width,
                    this.height,
                    previewX(),
                    previewY(),
                    this.activeLayout.backgroundWidth(),
                    (int) mouseX - this.dragOffsetX,
                    (int) mouseY - this.dragOffsetY
            );
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.draggingButton != null) {
            this.draggingButton = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    *///?} else {
    @Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        TidyScreens.ButtonKind buttonKind = buttonAt(click.x(), click.y());
        if (click.button() == 0 && buttonKind != null) {
            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            this.draggingButton = buttonKind;
            this.dragOffsetX = (int) click.x() - placement.x();
            this.dragOffsetY = (int) click.y() - placement.y();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double deltaX, double deltaY) {
        if (this.draggingButton != null && click.button() == 0) {
            TidyScreens.updateDraggedButton(
                    this.draggingButton,
                    this.activeLayout,
                    this.width,
                    this.height,
                    previewX(),
                    previewY(),
                    this.activeLayout.backgroundWidth(),
                    (int) click.x() - this.dragOffsetX,
                    (int) click.y() - this.dragOffsetY
            );
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        if (click.button() == 0 && this.draggingButton != null) {
            this.draggingButton = null;
            return true;
        }
        return super.mouseReleased(click);
    }
    //?}

    //? if <=1.19.4 {
    /*@Override
    public void render(net.minecraft.client.util.math.MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.hoveredButton = buttonAt(mouseX, mouseY);
        fill(matrices, 0, 0, this.width, this.height, 0xF0101018);

        int previewX = previewX();
        int previewY = previewY();
        renderLegacyPreviewBackground(matrices, previewX, previewY);

        drawLegacyCenteredText(matrices, this.title, this.width / 2, 16, 0xFFFFFF);
        drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_instructions"), this.width / 2, 32, 0xD9E2F2);
        drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_hint"), this.width / 2, 44, 0xAAB7CC);
        drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_current_layout", this.activeLayout.label()), this.width / 2, 84, 0xE7EEF9);

        renderLegacyPreviewButtons(matrices);
        renderLegacyPreviewMessages(matrices, previewY);

        net.minecraft.text.Text focusedLabel = TidyScreens.buttonLabel(this.draggingButton != null ? this.draggingButton : this.hoveredButton != null ? this.hoveredButton : TidyScreens.ButtonKind.SORT_DEFAULT);
        drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_status", focusedLabel, Tidy.CONFIG.buttonScalePercent), this.width / 2, this.height - 42, 0xD9E2F2);

        super.render(matrices, mouseX, mouseY, delta);
    }
    *///?} else {
    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.hoveredButton = buttonAt(mouseX, mouseY);
        context.fill(0, 0, this.width, this.height, 0xF0101018);

        int previewX = previewX();
        int previewY = previewY();
        renderPreviewBackground(context, previewX, previewY);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, TidyCompat.translatable("message.tidy.button_layout_instructions"), this.width / 2, 32, 0xD9E2F2);
        context.drawCenteredTextWithShadow(this.textRenderer, TidyCompat.translatable("message.tidy.button_layout_hint"), this.width / 2, 44, 0xAAB7CC);
        context.drawCenteredTextWithShadow(this.textRenderer, TidyCompat.translatable("message.tidy.button_layout_current_layout", this.activeLayout.label()), this.width / 2, 84, 0xE7EEF9);

        renderPreviewButtons(context);
        renderPreviewMessages(context, previewY);

        net.minecraft.text.Text focusedLabel = TidyScreens.buttonLabel(this.draggingButton != null ? this.draggingButton : this.hoveredButton != null ? this.hoveredButton : TidyScreens.ButtonKind.SORT_DEFAULT);
        context.drawCenteredTextWithShadow(
                this.textRenderer,
                TidyCompat.translatable("message.tidy.button_layout_status", focusedLabel, Tidy.CONFIG.buttonScalePercent),
                this.width / 2,
                this.height - 42,
                0xD9E2F2
        );

        super.render(context, mouseX, mouseY, delta);
    }
    //?}

    //? if >=1.20.4 {
    private void renderPreviewBackground(net.minecraft.client.gui.DrawContext context, int x, int y) {
        switch (this.activeLayout) {
            case PLAYER -> TidyCompat.drawTextureRegion(context, PLAYER_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case SINGLE_STORAGE -> TidyCompat.drawTextureRegion(context, SINGLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case DOUBLE_STORAGE -> TidyCompat.drawTextureRegion(context, DOUBLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 222, 256, 256);
            case UNSUPPORTED -> {
            }
        }
    }

    private void renderPreviewButtons(net.minecraft.client.gui.DrawContext context) {
        for (TidyScreens.ButtonKind buttonKind : TidyScreens.ButtonKind.values()) {
            if (!isButtonVisible(buttonKind)) {
                continue;
            }
            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            TidyCompat.drawGuiTexture(
                    context,
                    TidyScreens.buttonTexture(buttonKind, this.hoveredButton == buttonKind || this.draggingButton == buttonKind),
                    placement.x(),
                    placement.y(),
                    placement.size(),
                    placement.size()
            );
        }
    }

    private void renderPreviewMessages(net.minecraft.client.gui.DrawContext context, int previewY) {
        if (!hasAnyVisibleButtons()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    TidyCompat.translatable("message.tidy.button_layout_empty"),
                    this.width / 2,
                    previewY + this.activeLayout.backgroundHeight() + 10,
                    0xFFD27D
            );
        } else if (!Tidy.CONFIG.showContainerButtons) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    TidyCompat.translatable("message.tidy.button_layout_master_off"),
                    this.width / 2,
                    previewY + this.activeLayout.backgroundHeight() + 10,
                    0xFFD27D
            );
        }
    }
    //?}
    //? if 1.20.1 {
    /*private void renderPreviewBackground(net.minecraft.client.gui.DrawContext context, int x, int y) {
        switch (this.activeLayout) {
            case PLAYER -> TidyCompat.drawTextureRegion(context, PLAYER_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case SINGLE_STORAGE -> TidyCompat.drawTextureRegion(context, SINGLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case DOUBLE_STORAGE -> TidyCompat.drawTextureRegion(context, DOUBLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 222, 256, 256);
            case UNSUPPORTED -> {
            }
        }
    }

    private void renderPreviewButtons(net.minecraft.client.gui.DrawContext context) {
        for (TidyScreens.ButtonKind buttonKind : TidyScreens.ButtonKind.values()) {
            if (!isButtonVisible(buttonKind)) {
                continue;
            }
            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            TidyCompat.drawTextureRegion(
                    context,
                    TidyCompat.legacySpriteTexture(TidyScreens.buttonTexture(buttonKind, this.hoveredButton == buttonKind || this.draggingButton == buttonKind)),
                    placement.x(),
                    placement.y(),
                    0,
                    0,
                    placement.size(),
                    placement.size(),
                    128,
                    128
            );
        }
    }

    private void renderPreviewMessages(net.minecraft.client.gui.DrawContext context, int previewY) {
        if (!hasAnyVisibleButtons()) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    TidyCompat.translatable("message.tidy.button_layout_empty"),
                    this.width / 2,
                    previewY + this.activeLayout.backgroundHeight() + 10,
                    0xFFD27D
            );
        } else if (!Tidy.CONFIG.showContainerButtons) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    TidyCompat.translatable("message.tidy.button_layout_master_off"),
                    this.width / 2,
                    previewY + this.activeLayout.backgroundHeight() + 10,
                    0xFFD27D
            );
        }
    }
    *///?}

    //? if <=1.19.4 {
    /*private void renderLegacyPreviewBackground(net.minecraft.client.util.math.MatrixStack matrices, int x, int y) {
        switch (this.activeLayout) {
            case PLAYER -> TidyCompat.drawLegacyTextureRegion(matrices, PLAYER_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case SINGLE_STORAGE -> TidyCompat.drawLegacyTextureRegion(matrices, SINGLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 166, 256, 256);
            case DOUBLE_STORAGE -> TidyCompat.drawLegacyTextureRegion(matrices, DOUBLE_STORAGE_TEXTURE, x, y, 0, 0, 176, 222, 256, 256);
            case UNSUPPORTED -> {
            }
        }
    }

    private void renderLegacyPreviewButtons(net.minecraft.client.util.math.MatrixStack matrices) {
        for (TidyScreens.ButtonKind buttonKind : TidyScreens.ButtonKind.values()) {
            if (!isButtonVisible(buttonKind)) {
                continue;
            }
            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            TidyCompat.drawLegacyGuiTexture(
                    matrices,
                    TidyScreens.buttonTexture(buttonKind, this.hoveredButton == buttonKind || this.draggingButton == buttonKind),
                    placement.x(),
                    placement.y(),
                    placement.size(),
                    placement.size()
            );
        }
    }

    private void renderLegacyPreviewMessages(net.minecraft.client.util.math.MatrixStack matrices, int previewY) {
        if (!hasAnyVisibleButtons()) {
            drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_empty"), this.width / 2, previewY + this.activeLayout.backgroundHeight() + 10, 0xFFD27D);
        } else if (!Tidy.CONFIG.showContainerButtons) {
            drawLegacyCenteredText(matrices, TidyCompat.translatable("message.tidy.button_layout_master_off"), this.width / 2, previewY + this.activeLayout.backgroundHeight() + 10, 0xFFD27D);
        }
    }

    private void drawLegacyCenteredText(net.minecraft.client.util.math.MatrixStack matrices, net.minecraft.text.Text text, int centerX, int y, int color) {
        this.textRenderer.drawWithShadow(matrices, text, centerX - this.textRenderer.getWidth(text) / 2.0f, y, color);
    }
    *///?}

    private boolean hasAnyVisibleButtons() {
        for (TidyScreens.ButtonKind buttonKind : TidyScreens.ButtonKind.values()) {
            if (isButtonVisible(buttonKind)) {
                return true;
            }
        }
        return false;
    }

    private boolean isButtonVisible(TidyScreens.ButtonKind buttonKind) {
        if (buttonKind == TidyScreens.ButtonKind.CONFIG) {
            return Tidy.CONFIG.showConfigButton;
        }
        return this.activeLayout.usesPlayerSortButtons() ? Tidy.CONFIG.showPlayerSortButton : Tidy.CONFIG.showStorageSortButton;
    }

    private TidyScreens.ButtonKind buttonAt(double mouseX, double mouseY) {
        for (TidyScreens.ButtonKind buttonKind : TidyScreens.ButtonKind.values()) {
            if (!isButtonVisible(buttonKind)) {
                continue;
            }

            TidyScreens.ButtonPlacement placement = previewPlacement(buttonKind);
            if (mouseX >= placement.x()
                    && mouseX < placement.x() + placement.size()
                    && mouseY >= placement.y()
                    && mouseY < placement.y() + placement.size()) {
                return buttonKind;
            }
        }
        return null;
    }

    private TidyScreens.ButtonPlacement previewPlacement(TidyScreens.ButtonKind buttonKind) {
        return TidyScreens.resolveButtonPlacement(
                buttonKind,
                this.activeLayout,
                this.width,
                this.height,
                previewX(),
                previewY(),
                this.activeLayout.backgroundWidth()
        );
    }

    private int previewX() {
        return (this.width - this.activeLayout.backgroundWidth()) / 2;
    }

    private int previewY() {
        return TidyScreens.clamp((this.height - this.activeLayout.backgroundHeight()) / 2 - 2, 98, Math.max(98, this.height - this.activeLayout.backgroundHeight() - 56));
    }

    private void resetLayout() {
        TidyScreens.resetButtonLayout(this.activeLayout);
    }

    private void adjustScale(int delta) {
        Tidy.CONFIG.buttonScalePercent += delta;
        Tidy.CONFIG.sanitize();
    }
}
