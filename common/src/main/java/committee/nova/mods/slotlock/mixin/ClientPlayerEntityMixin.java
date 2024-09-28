package committee.nova.mods.slotlock.mixin;

import com.mojang.authlib.GameProfile;
import committee.nova.mods.slotlock.SlotLock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {


    public ClientPlayerEntityMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(at = @At("HEAD"), method = "drop", cancellable = true)
    public void dropSelectedItem(boolean dropEntireStack, CallbackInfoReturnable<Boolean> info){
        SlotLock.handleDropSelectedItem(this.getInventory(), info);
    }

//    @Inject(at = @At("HEAD"), method = "tick")
//    public void tick(CallbackInfo info) {
//        Iterator<Integer> it =  Slotlock.getLockedSlots().iterator();
//        while (it.hasNext()) {
//            int index = it.next();
//            if(this.inventory.getStack(index).isEmpty()) {
//                it.remove();
//                Slotlock.unlockSlot(index);
//            }
//        }
//    }

}
