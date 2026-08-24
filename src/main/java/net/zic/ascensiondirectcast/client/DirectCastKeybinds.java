package net.zic.ascensiondirectcast.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.ascensiondirectcast.AscensionDirectCast;
import net.zic.ascensiondirectcast.DirectCastActions;
import net.zic.zenithlib.input.InputHandler;
import net.zic.zenithlib.input.MappingHandler;
import org.lwjgl.glfw.GLFW;

public final class DirectCastKeybinds {
    private static final int[] DEFAULT_KEYS = {
            GLFW.GLFW_KEY_Z,
            GLFW.GLFW_KEY_X,
            GLFW.GLFW_KEY_C,
            GLFW.GLFW_KEY_B,
            GLFW.GLFW_KEY_N,
            GLFW.GLFW_KEY_M
    };
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(AscensionDirectCast.MOD_ID, "direct_cast")
    );
    private static final MappingHandler[] HANDLERS = new MappingHandler[DirectCastActions.KEYBIND_COUNT];

    private DirectCastKeybinds() {
    }

    public static void register() {
        if (HANDLERS[0] != null) {
            return;
        }

        for (int slot = 0; slot < HANDLERS.length; slot++) {
            KeyMapping mapping = new KeyMapping(
                    "key.ascension_direct_cast.slot_" + (slot + 1), KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM,
                    DEFAULT_KEYS[slot], CATEGORY);
            HANDLERS[slot] = InputHandler.registerAction(DirectCastActions.SLOT_ACTIONS.get(slot), mapping);
        }
    }

    public static KeyMapping get(int slot) {
        return HANDLERS[slot].getMapping();
    }
}
