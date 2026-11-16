package net.zic.ascensiondirectcast.client;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.network.SelectSkillSlotPacket;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.input.action.ActionChangedPacket;
import net.zic.zenithlib.input.action.ActionManager;

/**
 * Client-only keybindings over Ascension's existing server protocols.
 * The dedicated server does not need Ascension Direct Cast installed.
 */
public final class ClientDirectCast {
    private static final Identifier CAST_ACTION = CastingSkillInstance.CAST_INPUT;
    private static int owningSlot = -1;

    private ClientDirectCast() {}

    public static void onDown(int slot) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null || minecraft.player.isSpectator()
                || minecraft.screen != null) {
            return;
        }

        SkillCastHandler handler = minecraft.player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        if (slot < 0 || slot >= handler.getMaxSlots() || handler.getSkill(slot) == null) {
            return;
        }

        if (owningSlot >= 0) {
            if (owningSlot == slot) return;
            release(minecraft);
        }

        // The selection packet must reach the server before the normal cast-start packet.
        handler.select(slot);
        ClientPacketDistributor.sendToServer(new SelectSkillSlotPacket(slot));
        owningSlot = slot;

        ClientPacketDistributor.sendToServer(new ActionChangedPacket(CAST_ACTION, true));
        ActionManager manager = minecraft.player.getData(ZenithAttachments.ACTION_MANAGER);
        manager.actionStart(CAST_ACTION);
    }

    public static void onUp(int slot) {
        if (slot != owningSlot) return;
        Minecraft minecraft = Minecraft.getInstance();
        release(minecraft);
    }

    private static void release(Minecraft minecraft) {
        owningSlot = -1;
        if (minecraft.player == null || minecraft.level == null) return;

        ClientPacketDistributor.sendToServer(new ActionChangedPacket(CAST_ACTION, false));
        minecraft.player.getData(ZenithAttachments.ACTION_MANAGER).actionEnd(CAST_ACTION);
    }
}
