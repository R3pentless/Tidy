package pl.inh.tidy.compat;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class TidyCompat {

    private static final Map<String, Object> KEYBIND_CATEGORIES = new HashMap<>();

    private TidyCompat() {}

    public static Identifier id(String namespace, String path) {
        try {
            Method of = Identifier.class.getMethod("of", String.class, String.class);
            return (Identifier) of.invoke(null, namespace, path);
        } catch (ReflectiveOperationException ignored) {
            try {
                Constructor<Identifier> constructor = Identifier.class.getDeclaredConstructor(String.class, String.class);
                constructor.setAccessible(true);
                return constructor.newInstance(namespace, path);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create identifier " + namespace + ":" + path, e);
            }
        }
    }

    public static Text literal(String text) {
        try {
            Method literal = Text.class.getMethod("literal", String.class);
            return (Text) literal.invoke(null, text);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method of = Text.class.getMethod("of", String.class);
                return (Text) of.invoke(null, text);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create literal text", e);
            }
        }
    }

    public static Text translatable(String key, Object... args) {
        try {
            Method translatable = Text.class.getMethod("translatable", String.class, Object[].class);
            return (Text) translatable.invoke(null, key, args);
        } catch (ReflectiveOperationException ignored) {
            try {
                Class<?> translatableTextClass = Class.forName("net.minecraft.text.TranslatableText");
                Constructor<?> constructor = translatableTextClass.getConstructor(String.class, Object[].class);
                return (Text) constructor.newInstance(key, args);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create translatable text for key " + key, e);
            }
        }
    }

    public static int selectedSlot(PlayerInventory inventory) {
        try {
            Field selectedSlot = PlayerInventory.class.getField("selectedSlot");
            return selectedSlot.getInt(inventory);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method getter = PlayerInventory.class.getMethod("getSelectedSlot");
                return (int) getter.invoke(inventory);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to read selected hotbar slot", e);
            }
        }
    }

    public static String itemId(Item item) {
        try {
            Class<?> registriesClass = Class.forName("net.minecraft.registry.Registries");
            Object itemRegistry = registriesClass.getField("ITEM").get(null);
            Method getId = itemRegistry.getClass().getMethod("getId", Object.class);
            return getId.invoke(itemRegistry, item).toString();
        } catch (ReflectiveOperationException ignored) {
            try {
                Class<?> registryClass = Class.forName("net.minecraft.util.registry.Registry");
                Object itemRegistry = registryClass.getField("ITEM").get(null);
                Method getId = itemRegistry.getClass().getMethod("getId", Object.class);
                return getId.invoke(itemRegistry, item).toString();
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to resolve item identifier", e);
            }
        }
    }

    public static String itemId(ItemStack stack) {
        return itemId(stack.getItem());
    }

    public static ItemStack copyWithCount(ItemStack stack, int count) {
        try {
            Method copyWithCount = ItemStack.class.getMethod("copyWithCount", int.class);
            return (ItemStack) copyWithCount.invoke(stack, count);
        } catch (ReflectiveOperationException ignored) {
            ItemStack copy = stack.copy();
            copy.setCount(count);
            return copy;
        }
    }

    public static boolean sameItemAndData(ItemStack left, ItemStack right) {
        if (left.isEmpty() || right.isEmpty()) {
            return false;
        }

        left = copyWithCount(left, 1);
        right = copyWithCount(right, 1);

        try {
            Method method = ItemStack.class.getMethod("areItemsAndComponentsEqual", ItemStack.class, ItemStack.class);
            return (boolean) method.invoke(null, left, right);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method method = ItemStack.class.getMethod("canCombine", ItemStack.class, ItemStack.class);
                return (boolean) method.invoke(null, left, right);
            } catch (ReflectiveOperationException e) {
                return ItemStack.areEqual(left, right);
            }
        }
    }

    public static PositionedSoundInstance buttonClick(float pitch) {
        Object sound = soundEventField("UI_BUTTON_CLICK");

        try {
            Class<?> registryEntryClass = Class.forName("net.minecraft.registry.entry.RegistryEntry");
            Method ui = PositionedSoundInstance.class.getMethod("ui", registryEntryClass, float.class);
            return (PositionedSoundInstance) ui.invoke(null, sound, pitch);
        } catch (ReflectiveOperationException ignored) {
            try {
                Method ui = PositionedSoundInstance.class.getMethod("ui", SoundEvent.class, float.class);
                return (PositionedSoundInstance) ui.invoke(null, sound, pitch);
            } catch (ReflectiveOperationException ignoredAgain) {
                try {
                    Method master = PositionedSoundInstance.class.getMethod("master", SoundEvent.class, float.class, float.class);
                    return (PositionedSoundInstance) master.invoke(null, soundValue(sound), 1.0f, pitch);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create button click sound", e);
                }
            }
        }
    }

    public static void playPling(PlayerEntity player, float volume, float pitch) {
        player.playSound(soundValue(soundEventField("BLOCK_NOTE_BLOCK_PLING")), volume, pitch);
    }

    public static KeyBinding createKeyBinding(String translationKey, int defaultKey, String categoryKey, Identifier categoryId) {
        //? if >=1.21.11 {
        Object existingCategory = KEYBIND_CATEGORIES.computeIfAbsent(categoryId.toString(), ignored -> KeyBinding.Category.create(categoryId));
        return new KeyBinding(translationKey, defaultKey, (KeyBinding.Category) existingCategory);
        //?} else {
        /*return new KeyBinding(translationKey, InputUtil.Type.KEYSYM, defaultKey, categoryKey);*/
        //?}
    }

    public static ButtonWidget createButton(int x, int y, int width, int height, Text text, ButtonWidget.PressAction onPress) {
        try {
            Method builderMethod = ButtonWidget.class.getMethod("builder", Text.class, ButtonWidget.PressAction.class);
            Object builder = builderMethod.invoke(null, text, onPress);
            builder.getClass().getMethod("dimensions", int.class, int.class, int.class, int.class)
                    .invoke(builder, x, y, width, height);
            return (ButtonWidget) builder.getClass().getMethod("build").invoke(builder);
        } catch (ReflectiveOperationException ignored) {
            try {
                Constructor<ButtonWidget> constructor = ButtonWidget.class.getConstructor(
                        int.class, int.class, int.class, int.class, Text.class, ButtonWidget.PressAction.class
                );
                return constructor.newInstance(x, y, width, height, text, onPress);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Failed to create button " + text.getString(), e);
            }
        }
    }

    public static ButtonWidget createSpriteButton(
            int x,
            int y,
            int size,
            Identifier texture,
            Identifier hoveredTexture,
            Text label,
            Text tooltip,
            Text fallbackLabel,
            ButtonWidget.PressAction onPress
    ) {
        //? if >=1.20.4 {
        try {
            Class<?> buttonTexturesClass = Class.forName("net.minecraft.client.gui.screen.ButtonTextures");
            Object buttonTextures = buttonTexturesClass
                    .getConstructor(Identifier.class, Identifier.class)
                    .newInstance(texture, hoveredTexture);
            Constructor<TexturedButtonWidget> constructor = TexturedButtonWidget.class.getConstructor(
                    int.class, int.class, int.class, int.class, buttonTexturesClass, ButtonWidget.PressAction.class, Text.class
            );
            ButtonWidget button = constructor.newInstance(x, y, size, size, buttonTextures, onPress, label);
            setTooltip(button, tooltip);
            return button;
        } catch (ReflectiveOperationException ignored) {
        }
        //?} else {
        /*ButtonWidget button = new LegacySpriteButtonWidget(x, y, size, texture, hoveredTexture, label, onPress);
        setTooltip(button, tooltip);
        return button;*/
        //?}

        //? if >=1.20.4 {
        ButtonWidget fallbackButton = createButton(x, y, size, size, fallbackLabel, onPress);
        setTooltip(fallbackButton, tooltip);
        return fallbackButton;
        //?}
    }

    public static boolean matchesKey(KeyBinding keyBinding, Object input) {
        try {
            Method matchesKey = KeyBinding.class.getMethod("matchesKey", input.getClass());
            return (boolean) matchesKey.invoke(keyBinding, input);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to match key input", e);
        }
    }

    public static boolean matchesKey(KeyBinding keyBinding, int keyCode, int scanCode) {
        try {
            Method matchesKey = KeyBinding.class.getMethod("matchesKey", int.class, int.class);
            return (boolean) matchesKey.invoke(keyBinding, keyCode, scanCode);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to match key code", e);
        }
    }

    public static void setTooltip(Object widget, Text tooltip) {
        try {
            Class<?> tooltipClass = Class.forName("net.minecraft.client.gui.tooltip.Tooltip");
            Object tooltipInstance = tooltipClass.getMethod("of", Text.class).invoke(null, tooltip);
            widget.getClass().getMethod("setTooltip", tooltipClass).invoke(widget, tooltipInstance);
        } catch (ReflectiveOperationException ignored) {
            // Old versions don't expose widget tooltips in the same way.
        }
    }

    public static void drawGuiTexture(Object context, Identifier texture, int x, int y, int width, int height) {
        try {
            Class<?> renderPipelineClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipeline");
            Object pipeline = Class.forName("net.minecraft.client.gl.RenderPipelines").getField("GUI_TEXTURED").get(null);
            Method drawGuiTexture = context.getClass().getMethod(
                    "drawGuiTexture",
                    renderPipelineClass,
                    Identifier.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawGuiTexture.invoke(context, pipeline, texture, x, y, width, height);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Class<?> renderLayerClass = Class.forName("net.minecraft.client.render.RenderLayer");
            Method getGuiTextured = renderLayerClass.getMethod("getGuiTextured", Identifier.class);
            Function<Identifier, Object> texturedLayer = id -> {
                try {
                    return getGuiTextured.invoke(null, id);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create GUI render layer", e);
                }
            };
            Method drawGuiTexture = context.getClass().getMethod(
                    "drawGuiTexture",
                    Function.class,
                    Identifier.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawGuiTexture.invoke(context, texturedLayer, texture, x, y, width, height);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method drawGuiTexture = context.getClass().getMethod(
                    "drawGuiTexture",
                    Identifier.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawGuiTexture.invoke(context, texture, x, y, width, height);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method drawTexture = context.getClass().getMethod(
                    "drawTexture",
                    Identifier.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawTexture.invoke(context, texture, x, y, 0, 0, width, height);
        } catch (ReflectiveOperationException ignored) {
            // MatrixStack-era screens use a different rendering pipeline. Legacy branches no-op here.
        }
    }

    public static void drawTextureRegion(Object context, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        try {
            Class<?> renderPipelineClass = Class.forName("com.mojang.blaze3d.pipeline.RenderPipeline");
            Object pipeline = Class.forName("net.minecraft.client.gl.RenderPipelines").getField("GUI_TEXTURED").get(null);
            Method drawTexture = context.getClass().getMethod(
                    "drawTexture",
                    renderPipelineClass,
                    Identifier.class,
                    int.class,
                    int.class,
                    float.class,
                    float.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawTexture.invoke(context, pipeline, texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Class<?> renderLayerClass = Class.forName("net.minecraft.client.render.RenderLayer");
            Method getGuiTextured = renderLayerClass.getMethod("getGuiTextured", Identifier.class);
            Function<Identifier, Object> texturedLayer = id -> {
                try {
                    return getGuiTextured.invoke(null, id);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException("Failed to create GUI render layer", e);
                }
            };
            Method drawTexture = context.getClass().getMethod(
                    "drawTexture",
                    Function.class,
                    Identifier.class,
                    int.class,
                    int.class,
                    float.class,
                    float.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawTexture.invoke(context, texturedLayer, texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method drawTexture = context.getClass().getMethod(
                    "drawTexture",
                    Identifier.class,
                    int.class,
                    int.class,
                    int.class,
                    float.class,
                    float.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawTexture.invoke(context, texture, x, y, 0, (float) u, (float) v, width, height, textureWidth, textureHeight);
            return;
        } catch (ReflectiveOperationException ignored) {
        }

        try {
            Method drawTexture = context.getClass().getMethod(
                    "drawTexture",
                    Identifier.class,
                    int.class,
                    int.class,
                    float.class,
                    float.class,
                    int.class,
                    int.class,
                    int.class,
                    int.class
            );
            drawTexture.invoke(context, texture, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to draw texture region for " + texture, e);
        }
    }

    public static Identifier legacySpriteTexture(Identifier spriteId) {
        return id(spriteId.getNamespace(), "textures/gui/sprites/" + spriteId.getPath() + ".png");
    }

    //? if <=1.19.4 {
    /*public static void drawLegacyGuiTexture(net.minecraft.client.util.math.MatrixStack matrices, Identifier texture, int x, int y, int width, int height) {
        Identifier legacyTexture = legacySpriteTexture(texture);
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, legacyTexture);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        matrices.push();
        matrices.translate(x, y, 0.0D);
        matrices.scale(width / 128.0F, height / 128.0F, 1.0F);
        net.minecraft.client.gui.DrawableHelper.drawTexture(matrices, 0, 0, 0.0F, 0.0F, 128, 128, 128, 128);
        matrices.pop();
    }
    *///?}

    //? if <=1.18.2 {
    /*public static void drawLegacyTextureRegion(net.minecraft.client.util.math.MatrixStack matrices, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.gui.DrawableHelper.drawTexture(matrices, x, y, (float) u, (float) v, width, height, textureWidth, textureHeight);
    }
    *///?}
    //? if 1.19.4 {
    /*public static void drawLegacyTextureRegion(net.minecraft.client.util.math.MatrixStack matrices, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        com.mojang.blaze3d.systems.RenderSystem.setShaderTexture(0, texture);
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        net.minecraft.client.gui.DrawableHelper.drawTexture(matrices, x, y, u, v, width, height);
    }
    *///?}

    //? if <=1.18.2 {
    /*private static final class LegacySpriteButtonWidget extends ButtonWidget {
        private final Identifier texture;
        private final Identifier hoveredTexture;

        private LegacySpriteButtonWidget(int x, int y, int size, Identifier texture, Identifier hoveredTexture, Text label, PressAction onPress) {
            super(x, y, size, size, label, onPress);
            this.texture = texture;
            this.hoveredTexture = hoveredTexture;
        }

        @Override
        public void renderButton(net.minecraft.client.util.math.MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (!this.visible) {
                return;
            }
            Identifier renderTexture = this.isHovered() ? this.hoveredTexture : this.texture;
            drawLegacyGuiTexture(matrices, renderTexture, this.x, this.y, this.width, this.height);
        }
    }
    *///?}
    //? if 1.19.4 {
    /*private static final class LegacySpriteButtonWidget extends ButtonWidget {
        private final Identifier texture;
        private final Identifier hoveredTexture;

        private LegacySpriteButtonWidget(int x, int y, int size, Identifier texture, Identifier hoveredTexture, Text label, PressAction onPress) {
            super(x, y, size, size, label, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.texture = texture;
            this.hoveredTexture = hoveredTexture;
        }

        @Override
        public void renderButton(net.minecraft.client.util.math.MatrixStack matrices, int mouseX, int mouseY, float delta) {
            if (!this.visible) {
                return;
            }
            Identifier renderTexture = this.isHovered() ? this.hoveredTexture : this.texture;
            drawLegacyGuiTexture(matrices, renderTexture, this.getX(), this.getY(), this.width, this.height);
        }
    }
    *///?}
    //? if 1.20.1 {
    /*private static final class LegacySpriteButtonWidget extends ButtonWidget {
        private final Identifier texture;
        private final Identifier hoveredTexture;

        private LegacySpriteButtonWidget(int x, int y, int size, Identifier texture, Identifier hoveredTexture, Text label, PressAction onPress) {
            super(x, y, size, size, label, onPress, DEFAULT_NARRATION_SUPPLIER);
            this.texture = legacySpriteTexture(texture);
            this.hoveredTexture = legacySpriteTexture(hoveredTexture);
        }

        @Override
        protected void renderButton(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
            if (!this.visible) {
                return;
            }
            Identifier renderTexture = this.isHovered() ? this.hoveredTexture : this.texture;
            drawTextureRegion(context, renderTexture, this.getX(), this.getY(), 0, 0, this.width, this.height, 128, 128);
        }
    }
    *///?}

    private static Object soundEventField(String name) {
        try {
            Class<?> soundEventsClass = Class.forName("net.minecraft.sound.SoundEvents");
            return soundEventsClass.getField(name).get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to resolve sound field " + name, e);
        }
    }

    private static SoundEvent soundValue(Object sound) {
        if (sound instanceof SoundEvent event) {
            return event;
        }

        try {
            Method value = sound.getClass().getMethod("value");
            return (SoundEvent) value.invoke(sound);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to unwrap sound event", e);
        }
    }
}
