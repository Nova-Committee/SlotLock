package committee.nova.mods.slotlock.mixin;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public class ScreenHandlerMixin {

    @Shadow @Final public NonNullList<Slot> slots;

    @Inject(at = @At("HEAD"), method = "clicked", cancellable = true)
    public void onSlotClick(int slotIndex, int j, ClickType clickType, Player player, CallbackInfo ci) {
        if(!Minecraft.getInstance().isSameThread()) return;
        if(slotIndex >= 0 && slotIndex < this.slots.size()) {
            Slot finalSlot = this.slots.get(slotIndex);
            if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
                finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
            }
            if (finalSlot.container == player.getInventory() && SlotLock.isLocked(((SlotAccessor) finalSlot).getIndex())) {
                ci.cancel();
            }
        }
    }


}
