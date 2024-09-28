package committee.nova.mods.slotlock.mixin;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CreativeModeInventoryScreen.SlotWrapper.class)
public interface CreativeSlotAccessor {

    @Accessor
    Slot getTarget();

}
