package net.zic.ascensiondirectcast.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.zic.ascensiondirectcast.AscensionDirectCast;

@Mod(value = AscensionDirectCast.MOD_ID, dist = Dist.CLIENT)
public final class DirectCastClient {
    public DirectCastClient(IEventBus modEventBus) {
        DirectCastKeybinds.register();
        modEventBus.addListener(DirectCastHotbarOverlay::registerGuiLayers);
    }
}
