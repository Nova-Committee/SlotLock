package committee.nova.mods.slotlock.neoforge;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * @Project: slotlock
 * @Author: cnlimiter
 * @CreateTime: 2024/9/22 15:07
 * @Description:
 */
@EventBusSubscriber(modid = SlotLock.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    // Tick events
    @SubscribeEvent
    public static void clientTickEvent(ClientTickEvent.Post event) {
        SlotLock.onClientTick(Minecraft.getInstance());
    }
}
