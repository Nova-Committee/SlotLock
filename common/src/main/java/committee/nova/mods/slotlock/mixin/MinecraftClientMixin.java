package committee.nova.mods.slotlock.mixin;

import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow @Final public Options options;

    @Shadow @Nullable public LocalPlayer player;

    @SuppressWarnings("ConstantConditions")
    @Inject(at = @At("HEAD"), method = "setLevel")
    public void joinWorld(ClientLevel world, CallbackInfo info) {
        SlotLock.handleJoinWorld(((Minecraft) ((Object) this)));
    }

    @Inject(at = @At("HEAD"), method = "handleKeybinds")
    public void handleInputEvents(CallbackInfo info) {
        SlotLock.handleInputEvents(options, player);
    }


    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;findSlotMatchingItem(Lnet/minecraft/world/item/ItemStack;)I"), method = "pickBlock", cancellable = true)
    public void handleItemPick(CallbackInfo ci) {
        SlotLock.handleItemPick(this.player.getInventory().selected, ci);
    }

}
