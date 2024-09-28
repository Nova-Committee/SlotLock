package committee.nova.mods.slotlock.fabric.client;

import committee.nova.mods.slotlock.SlotLock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;

public final class SlotLockFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(SlotLock.lockBinding);
        ClientTickEvents.END_CLIENT_TICK.register(SlotLock::onClientTick);
    }
}
