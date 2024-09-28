package committee.nova.mods.slotlock.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class InGameHudMixin{

    private static final ResourceLocation SLOT_LOCK_TEXTURE = ResourceLocation.tryBuild(SlotLock.MOD_ID, "textures/gui/lock_overlay.png");
    private int slotIndex = 0;

    @Inject(at = @At("HEAD"), method = "renderItemHotbar")
    public void renderHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        slotIndex = 0;
    }

    @Inject(at = @At("HEAD"), method = "renderSlot")
    public void renderHotbarItem(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int k, CallbackInfo ci) {
        if (SlotLock.isLocked(slotIndex)) {
            if (player.getInventory().getItem(slotIndex).isEmpty()) {
                SlotLock.unlockSlot(slotIndex);
            }
            else {
                RenderSystem.setShaderTexture(0, SLOT_LOCK_TEXTURE);
                guiGraphics.blit(SLOT_LOCK_TEXTURE, x, y, 0, 0, 16, 16);
            }
        }
        slotIndex++;
        if(slotIndex == 9) {
            slotIndex = 40;
        }
    }

}
