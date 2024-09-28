package committee.nova.mods.slotlock.forge;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @Project: slotlock
 * @Author: cnlimiter
 * @CreateTime: 2024/9/22 15:07
 * @Description:
 */
@Mod.EventBusSubscriber(modid = SlotLock.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    // Tick events
    @SubscribeEvent
    public static void clientTickEvent(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        SlotLock.onClientTick(Minecraft.getInstance());
    }
}
