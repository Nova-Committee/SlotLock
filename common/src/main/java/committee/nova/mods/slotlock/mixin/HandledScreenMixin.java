package committee.nova.mods.slotlock.mixin;

import committee.nova.mods.slotlock.SlotLock;
import committee.nova.mods.slotlock.mixed.HandledScreenMixed;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(AbstractContainerScreen.class)
public abstract class HandledScreenMixin<T extends AbstractContainerMenu> extends net.minecraft.client.gui.screens.Screen implements HandledScreenMixed {

    protected HandledScreenMixin(Component title) {
        super(title);
    }

    private static final ResourceLocation SLOT_LOCK_TEXTURE = ResourceLocation.tryBuild(SlotLock.MOD_ID, "textures/gui/lock_overlay.png");
    @Shadow @Nullable protected Slot hoveredSlot;
    @Shadow @Final protected T menu;

    private Inventory slotlock$playerInventory;

    @Override
    public Inventory slotlock$getPlayerInventory() {
        return slotlock$playerInventory;
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void onInit(T handler, Inventory inventory, Component title, CallbackInfo ci) {
        slotlock$playerInventory = inventory;
    }

    @Inject(at = @At("HEAD"), method = "slotClicked", cancellable = true)
    public void onMouseClick(Slot slot, int invSlot, int clickData, ClickType actionType, CallbackInfo info) {
        SlotLock.handleMouseClick(menu, slotlock$playerInventory, slot, null, invSlot, clickData, actionType, info);
    }

    @Inject(at = @At("HEAD"), method = "keyPressed", cancellable = true)
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> info) {
        SlotLock.handleKeyPressed(hoveredSlot, slotlock$playerInventory, keyCode, scanCode, info);
    }

    @Inject(at = @At("HEAD"), method = "checkHotbarKeyPressed", cancellable = true)
    public void handleHotbarKeyPressed(int keyCode, int scanCode, CallbackInfoReturnable<Boolean> info) {
        SlotLock.handleHotbarKeyPressed(hoveredSlot, slotlock$playerInventory, info);
    }

    @Inject(at = @At("HEAD"), method = "renderTooltip", cancellable = true)
    public void drawMouseoverTooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        if (menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.container == this.slotlock$playerInventory) {
            Slot finalSlot = hoveredSlot;
            if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
                finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
            }
            if(finalSlot != null && SlotLock.isLocked(((SlotAccessor) finalSlot).getIndex())) {
                ItemStack stack = finalSlot.hasItem() ? this.hoveredSlot.getItem() : ItemStack.EMPTY;
                List<Component> tooltip = getTooltipFromItem(minecraft, stack);
                tooltip.add(Component.translatable("slotlock.locked"));
                tooltip.add(Component.translatable("slotlock.press1").append(SlotLock.lockBinding.getTranslatedKeyMessage().copy().append(Component.translatable("slotlock.press2"))));
                guiGraphics.renderTooltip(font, tooltip, Optional.empty(), x, y);
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "renderSlot")
    public void drawSlot(GuiGraphics matrices, Slot slot, CallbackInfo info) {
        Slot finalSlot = slot;
        if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
            finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
        }
        if(this.minecraft != null && slot.container == slotlock$playerInventory && SlotLock.isLocked(((SlotAccessor) finalSlot).getIndex())) {
            if (!finalSlot.hasItem()) {
                SlotLock.unlockSlot(((SlotAccessor) finalSlot).getIndex());
                return;
            }
            matrices.blit(SLOT_LOCK_TEXTURE, slot.x, slot.y, 0, 0, 16, 16);
        }
    }

}
