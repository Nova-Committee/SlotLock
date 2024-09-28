package committee.nova.mods.slotlock.mixin;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.Minecraft;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Shadow @Final public Container container;

    @Shadow
    public int index;

    @Inject(at = @At("HEAD"), method = "mayPlace", cancellable = true)
    public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        if(Minecraft.getInstance().player != null) {
            Inventory playerInventory = Minecraft.getInstance().player.getInventory();
            if(container == playerInventory && SlotLock.isLocked(index)) {
                info.setReturnValue(false);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "mayPickup", cancellable = true)
    public void canTakeItems(Player playerEntity, CallbackInfoReturnable<Boolean> info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        if(container == playerEntity.getInventory() && SlotLock.isLocked(index)) {
            info.setReturnValue(false);
        }
    }

}
