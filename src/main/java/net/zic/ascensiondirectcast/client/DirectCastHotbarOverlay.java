package net.zic.ascensiondirectcast.client;

import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.ascensiondirectcast.AscensionDirectCast;
import net.zic.ascensiondirectcast.DirectCastActions;

import java.util.HashMap;
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
    private static final int SLOT_SIZE = 30;
    private static final int SLOT_GAP = 2;
    private static final int ICON_SIZE = 16;

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
        int top = minecraft.getWindow().getGuiScaledHeight() - 72;

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
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, selected ? 0xD0D19A32 : 0xB0101010);
        graphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, 0xD0202020);

        KeyMapping mapping = DirectCastKeybinds.get(slot);
        Component keyName = mapping.getTranslatedKeyMessage();
        int keyX = x + (SLOT_SIZE - minecraft.font.width(keyName)) / 2;
        graphics.text(minecraft.font, keyName, keyX, y + 2, 0xFFFFFFFF, true);

        Identifier skill = handler.getSkill(slot);
        if (skill == null) {
            return;
        }

        Identifier icon = ICONS.computeIfAbsent(skill, DirectCastHotbarOverlay::resolveIcon);
        new TextureData(icon, ICON_SIZE, ICON_SIZE).renderAt(
                graphics,
                x + (SLOT_SIZE - ICON_SIZE) / 2,
                y + 12
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
