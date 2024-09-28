package committee.nova.mods.slotlock.mixin;

import committee.nova.mods.slotlock.SlotLock;
import committee.nova.mods.slotlock.mixed.HandledScreenMixed;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow private Slot destroyItemSlot;

    public CreativeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }


    @Inject(at = @At("HEAD"), method = "slotClicked", cancellable = true)
    public void onMouseClick(Slot slot, int invSlot, int clickData, ClickType actionType, CallbackInfo info) {
        SlotLock.handleMouseClick(menu, ((HandledScreenMixed) this).slotlock$getPlayerInventory(), slot, destroyItemSlot, invSlot, clickData, actionType, info);
    }

}
