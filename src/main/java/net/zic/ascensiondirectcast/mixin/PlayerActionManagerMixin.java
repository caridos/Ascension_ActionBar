package net.zic.ascensiondirectcast.mixin;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.ascensiondirectcast.AscensionCastingBridge;
import net.zic.ascensiondirectcast.PlayerActionManagerAccess;
import net.zic.zenithlib.input.action.PlayerActionManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;

@Mixin(value = PlayerActionManager.class, remap = false)
public abstract class PlayerActionManagerMixin implements PlayerActionManagerAccess {
    @Shadow
    @Final
    private Player player;

    @Shadow
    @Final
    private HashSet<Identifier> activeActions;

    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true, remap = false)
    private void includeDirectCastInput(Identifier action, CallbackInfoReturnable<Boolean> callback) {
        if (!action.equals(AscensionSkillListener.skillCast)) {
            return;
        }

        Identifier owner = AscensionCastingBridge.getInputOwner(player);
        callback.setReturnValue(owner != null && activeActions.contains(owner));
    }

    @Override
    public boolean ascensionDirectCast$isRawActive(Identifier action) {
        return activeActions.contains(action);
    }
}
