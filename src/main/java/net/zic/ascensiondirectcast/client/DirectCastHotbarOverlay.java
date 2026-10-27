package net.zic.ascensiondirectcast.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.ascensiondirectcast.AscensionDirectCast;
import net.zic.ascensiondirectcast.DirectCastActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DirectCastHotbarOverlay {
    private static final Identifier LAYER = Identifier.fromNamespaceAndPath(
            AscensionDirectCast.MOD_ID,
            "skill_hotbar"
    );
    private static final Identifier FALLBACK_ICON = Identifier.fromNamespaceAndPath(
            "ascension",
            "textures/spells/icon/placeholder.png"
    );
    private static final Map<Identifier, Identifier> ICONS = new HashMap<>();
    private static final int SLOT_SIZE = 44;
    private static final int SLOT_HEIGHT = 44;
    private static final int ICON_SIZE = 24;
    private static final int ICON_TOP = 17;
    private static final int SLOT_GAP = 2;
    private static final int LABEL_TOP = 3;
    private static final float LABEL_SCALE = 0.8f;
    private static final int LABEL_PADDING = 4;
    private static final int BAR_RAISE = 5;


    private DirectCastHotbarOverlay() {
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER, DirectCastHotbarOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || minecraft.level == null
                || minecraft.player.isSpectator()
                || minecraft.options.hideGui) {
            return;
        }

        SkillCastHandler handler = minecraft.player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        int slotCount = Math.min(handler.getMaxSlots(), DirectCastActions.KEYBIND_COUNT);
        if (slotCount <= 0) {
            return;
        }

        int width = slotCount * SLOT_SIZE + (slotCount - 1) * SLOT_GAP;
        int left = (minecraft.getWindow().getGuiScaledWidth() - width) / 2;
        int top = minecraft.getWindow().getGuiScaledHeight() - 80 - (SLOT_HEIGHT - SLOT_SIZE) - BAR_RAISE;

        for (int slot = 0; slot < slotCount; slot++) {
            renderSlot(graphics, minecraft, handler, slot, left + slot * (SLOT_SIZE + SLOT_GAP), top);
        }
    }

    private static void renderSlot(
            GuiGraphicsExtractor graphics,
            Minecraft minecraft,
            SkillCastHandler handler,
            int slot,
            int x,
            int y
    ) {
        boolean selected = handler.getSelectedSlot() == slot;
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_HEIGHT, selected ? 0xD0D19A32 : 0xB0101010);
        graphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_HEIGHT - 1, 0xD0202020);

        KeyMapping mapping = DirectCastKeybinds.get(slot);
        Component keyName = mapping.getTranslatedKeyMessage();
        int labelWidth = Math.round((SLOT_SIZE - 2 * LABEL_PADDING) / LABEL_SCALE);

        List<FormattedCharSequence> lines = minecraft.font.split(keyName, labelWidth);

        int renderedLines = Math.min(2, lines.size());

        graphics.pose().pushMatrix();

        try {
            graphics.pose().translate(x + SLOT_SIZE / 2.0f, y + LABEL_TOP);
            graphics.pose().scale(LABEL_SCALE, LABEL_SCALE);

            for (int lineIndex = 0; lineIndex < renderedLines; lineIndex++) {
                FormattedCharSequence line = lines.get(lineIndex);
                int lineX = -minecraft.font.width(line) / 2;

                graphics.text(minecraft.font, line, lineX, lineIndex * minecraft.font.lineHeight, 0xFFFFFFFF, true);
            }
        } finally {
            graphics.pose().popMatrix();
        }

        Identifier skill = handler.getSkill(slot);
        if (skill == null) {
            return;
        }

        Identifier icon = ICONS.computeIfAbsent(
                skill,
                DirectCastHotbarOverlay::resolveIcon
        );

        graphics.blit(
                net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,
                icon,
                x + (SLOT_SIZE - ICON_SIZE) / 2,
                y + ICON_TOP,
                0,
                0,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE,
                ICON_SIZE
        );
    }

    private static Identifier resolveIcon(Identifier skill) {
        Identifier candidate = Identifier.fromNamespaceAndPath(
                skill.getNamespace(),
                "textures/spells/icon/" + skill.getPath() + ".png"
        );
        return Minecraft.getInstance().getResourceManager().getResource(candidate).isPresent()
                ? candidate
                : FALLBACK_ICON;
    }
}
