package net.zic.ascensiondirectcast;

import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.stream.IntStream;

public final class DirectCastActions {
    public static final int KEYBIND_COUNT = 6;
    public static final List<Identifier> SLOT_ACTIONS = IntStream.range(0, KEYBIND_COUNT)
            .mapToObj(slot -> Identifier.fromNamespaceAndPath(
                    AscensionDirectCast.MOD_ID,
                    "skill_slot_" + (slot + 1)
            ))
            .toList();

    private DirectCastActions() {
    }

    public static int slotFor(Identifier action) {
        return SLOT_ACTIONS.indexOf(action);
    }
}
