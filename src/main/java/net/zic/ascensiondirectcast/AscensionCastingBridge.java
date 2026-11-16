package net.zic.ascensiondirectcast;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.input.action.ActionEvent;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = AscensionDirectCast.MOD_ID)
public final class AscensionCastingBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger(AscensionCastingBridge.class);
    private static final Map<Player, CastInputState> INPUT_STATES =
            Collections.synchronizedMap(new WeakHashMap<>());

    private AscensionCastingBridge() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onActionStart(ActionEvent.Start event) {
        Identifier action = event.getAction();
        int slot = DirectCastActions.slotFor(action);
        if (slot < 0 && !action.equals(AscensionSkillListener.skillCast)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        CastInputState state = state(player);

        // An interrupted cast can disappear before either tick listener observes its end.
        if (state.sawLongCast && handler.getActiveCastVisualState() == null) {
            handler.requireCastInputRelease();
            state.releaseRequired = true;
            state.sawLongCast = false;
        }
        if (handler.isWaitingForCastRelease()) {
            state.releaseRequired = true;
        }

        state.ownerAction = action;
        if (slot < 0) {
            return;
        }
        if (slot >= handler.getMaxSlots()) {
            LOGGER.warn("Direct Cast slot {} out of bounds (max {})", slot, handler.getMaxSlots());
            return;
        }

        LOGGER.info("Direct Cast DOWN side={} action={} slot={} skill={} waitingForRelease={}",
                player.level().isClientSide() ? "client" : "server", action, slot,
                handler.getSkill(slot), handler.isWaitingForCastRelease());
        handler.select(slot);
        handler.castSelectedSkill();
        LOGGER.info("Direct Cast RESULT side={} slot={} selected={} casting={} activeInput={}",
                player.level().isClientSide() ? "client" : "server", slot,
                handler.getSelectedSlot(), handler.getCastingSkill(),
                player.getData(ZenithAttachments.ACTION_MANAGER).isActive(action));
        if (handler.getActiveCastVisualState() != null) {
            state.sawLongCast = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onActionEnd(ActionEvent.End event) {
        Identifier action = event.getAction();
        if (DirectCastActions.slotFor(action) < 0 && !action.equals(AscensionSkillListener.skillCast)) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        CastInputState state = INPUT_STATES.get(player);
        if (state == null) {
            return;
        }

        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        LOGGER.info("Direct Cast UP side={} action={} owner={} casting={}",
                player.level().isClientSide() ? "client" : "server", action,
                state.ownerAction, handler.getCastingSkill());
        if (action.equals(state.ownerAction)) {
            state.ownerAction = null;
            state.releaseRequired = false;
            handler.releaseCastInput();
        } else if (state.releaseRequired && isEffectiveCastInputActive(player)) {
            // Ascension clears its latch whenever V ends, even if a direct input owns the cast.
            handler.requireCastInputRelease();
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        CastInputState state = INPUT_STATES.get(player);
        if (state == null) {
            return;
        }

        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        if (handler.isWaitingForCastRelease()) {
            state.releaseRequired = true;
        }

        if (handler.getActiveCastVisualState() != null) {
            state.sawLongCast = true;
            return;
        }

        if (state.sawLongCast) {
            state.sawLongCast = false;
            if (isEffectiveCastInputActive(player)) {
                handler.requireCastInputRelease();
                state.releaseRequired = true;
            }
        }
    }

    public static Identifier getInputOwner(Player player) {
        CastInputState state = INPUT_STATES.get(player);
        return state == null ? null : state.ownerAction;
    }

    private static CastInputState state(Player player) {
        return INPUT_STATES.computeIfAbsent(player, ignored -> new CastInputState());
    }

    private static boolean isEffectiveCastInputActive(Player player) {
        Identifier owner = getInputOwner(player);
        if (owner == null) {
            return false;
        }

        return player.getData(ZenithAttachments.ACTION_MANAGER).isActive(owner);
    }

    private static final class CastInputState {
        private Identifier ownerAction;
        private boolean sawLongCast;
        private boolean releaseRequired;
    }
}
