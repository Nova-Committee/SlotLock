package committee.nova.mods.slotlock.forge;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(SlotLock.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class SlotLockForge {
    public SlotLockForge() {
    }


    @SubscribeEvent
    static void registerKeyMappingsEvent(RegisterKeyMappingsEvent event) {
        event.register(SlotLock.lockBinding);
    }

}
