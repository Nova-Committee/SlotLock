package committee.nova.mods.slotlock.neoforge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import committee.nova.mods.slotlock.SlotLock;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@Mod(SlotLock.MOD_ID)
public final class SlotLockNeoForge {
    public SlotLockNeoForge() {
    }

    @SubscribeEvent
    static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(SlotLock.lockBinding);
    }

}
