package committee.nova.mods.slotlock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import committee.nova.mods.slotlock.mixin.CreativeSlotAccessor;
import committee.nova.mods.slotlock.mixin.KeyBindingAccessor;
import committee.nova.mods.slotlock.mixin.ServerWorldAccessor;
import committee.nova.mods.slotlock.mixin.SlotAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;

public class SlotLock {

    public final static String MOD_ID = "slotlock";
    public final static Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static KeyMapping lockBinding = new KeyMapping(
            "key.slotlock",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.inventory"
    );
    public static String currentKey = "world";
    public static boolean isSaveDirty = false;

    private static long lastDirtyCheck = System.currentTimeMillis();

    public static void onClientTick(Minecraft client){
        long currentDirtyCheck = System.currentTimeMillis();
        if(currentDirtyCheck - lastDirtyCheck > 2000) {
            if(isSaveDirty) {
                File slotLockFile = new File(Minecraft.getInstance().gameDirectory.toPath().resolve("config").toFile(), "slotlock.json");
                Path slotLockPath = Paths.get(slotLockFile.getAbsolutePath());
                String json = "{ }";
                try {
                    json = Files.readString(slotLockPath);
                }catch (Exception ignored) { }
                JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
                JsonArray jsonArray = new JsonArray();
                lockedSlots.forEach(jsonArray::add);
                jsonObject.add(currentKey, jsonArray);
                try {
                    Files.writeString(slotLockPath, jsonObject.toString());
                    SlotLock.LOGGER.info("Successfully updated slotlock file");
                }catch (Exception e) {
                    SlotLock.LOGGER.error("Failed to update slotlock file");
                }
                isSaveDirty = false;
            }
            lastDirtyCheck = currentDirtyCheck;
        }
    }

    private static LinkedHashSet<Integer> lockedSlots = new LinkedHashSet<>();

    @SuppressWarnings({"unchecked", "unused"})
    public static LinkedHashSet<Integer> getLockedSlots() {
        return (LinkedHashSet<Integer>) lockedSlots.clone();
    }

    public static boolean isLocked(int slot) {
        return lockedSlots.contains(slot);
    }

    public static void lockSlot(int slot) {
        lockedSlots.add(slot);
        isSaveDirty = true;
    }

    public static void unlockSlot(int slot) {
        lockedSlots.remove(slot);
        isSaveDirty = true;
    }

    public static void handleJoinWorld(Minecraft client) {
        String key = "world";
        if(client.isRunning()) {
            IntegratedServer server = client.getSingleplayerServer();
            if(server != null) {
                key = ((ServerWorldAccessor) server.overworld()).getServerLevelData().getLevelName();
            }
        }else{
            ServerData info = client.getCurrentServer();
            if(info != null) {
                key = info.ip;
            }
        }
        SlotLock.LOGGER.info("Loading slotlock file");
        currentKey = key;
        SlotLock.lockedSlots = new LinkedHashSet<>();
        File slotLockFile = new File(Minecraft.getInstance().gameDirectory, "slotlock.json");
        Path slotLockPath = Paths.get(slotLockFile.getAbsolutePath());
        if(Files.notExists(slotLockPath)) {
            try{
                SlotLock.LOGGER.info("File not found! Creating new slotlock file");
                Files.writeString(slotLockPath, "{ }");
                SlotLock.LOGGER.info("Successfully created new slotlock file");
            }catch (Exception e){
                SlotLock.LOGGER.error("An error occurred while creating the slotlock file.", e);
            }
        }
        String json;
        try {
            json = Files.readString(slotLockPath);
        }catch (Exception e) {
            SlotLock.LOGGER.error("An error occurred while loading the slotlock file.", e);
            json = "{ }";
        }
        try {
            JsonObject jsonObject = new Gson().fromJson(json, JsonObject.class);
            JsonArray lockedSlotsJson = jsonObject.getAsJsonArray(key);
            if (lockedSlotsJson != null) {
                lockedSlotsJson.forEach(element -> {
                    int slot = -1;
                    try {
                        slot = element.getAsInt();
                    } catch (Exception ignored) {
                    }
                    if (slot != -1)
                        SlotLock.lockedSlots.add(slot);
                });
            }
            SlotLock.LOGGER.info("Successfully loaded slotlock file");
        }catch (Exception e) {
            SlotLock.LOGGER.error("An error occurred while reading the slotlock file.", e);
        }

    }

    public static void handleMouseClick(AbstractContainerMenu handler, Inventory playerInventory, Slot slot, Slot deleteItemSlot, int invSlot, int clickData, ClickType actionType, CallbackInfo info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        if(slot != null && slot.container == playerInventory) {
            Slot finalSlot = slot;
            if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
                finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
            }
            int index = ((SlotAccessor) finalSlot).getIndex();
            if(SlotLock.isLocked(index)) {
                info.cancel();
            }
        }

        if(slot != null && actionType == ClickType.PICKUP_ALL) {
            ItemStack pickedStack = handler.getCarried();
            handler.slots.forEach(handlerSlot -> {
                int slotIndex = ((SlotAccessor) handlerSlot).getIndex();
                if(handlerSlot.container == playerInventory && SlotLock.isLocked(slotIndex) && canMergeItems(pickedStack, handlerSlot.getItem())) {
                    info.cancel();
                }
            });
        }

        if(actionType == ClickType.QUICK_MOVE && invSlot >= 0 && invSlot < handler.slots.size()) {
            if (slot != null && slot == deleteItemSlot) {
                for (int i = 0; i < playerInventory.getContainerSize(); ++i) {
                    if (!SlotLock.isLocked(i)) {
                        playerInventory.removeItemNoUpdate(i);
                    }
                }
                info.cancel();
                return;
            }

            Slot slot2 = handler.slots.get(invSlot);
            if(slot2.container == playerInventory && SlotLock.isLocked(((SlotAccessor) slot2).getIndex())) {
                info.cancel();
            }
        }

        if(actionType == ClickType.SWAP) {
            for(Slot slot3 : handler.slots) {
                Slot finalSlot = slot3;
                if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
                    finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
                }
                int index = ((SlotAccessor) finalSlot).getIndex();
                if(finalSlot.container == playerInventory && index == clickData && SlotLock.isLocked(index)) {
                    info.cancel();
                }
            }
        }
    }

    public static void handleKeyPressed(Slot focusedSlot, Inventory playerInventory, int keyCode, int scanCode, CallbackInfoReturnable<Boolean> info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        if(keyCode != 256 && !Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            Slot finalSlot = focusedSlot;
            if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
                finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
            }
            if(finalSlot != null) {
                int index = ((SlotAccessor) finalSlot).getIndex();
                if(finalSlot.container == playerInventory) {
                    if (SlotLock.lockBinding.matches(keyCode, scanCode)) {
                        boolean locked = SlotLock.isLocked(index);
                        if (locked) {
                            SlotLock.unlockSlot(index);
                        } else if (!finalSlot.getItem().isEmpty()) {
                            SlotLock.lockSlot(index);
                        }
                    } else {
                        if (SlotLock.isLocked(index)) {
                            info.setReturnValue(true);
                        }
                    }
                }
            }
        }
    }

    public static void handleHotbarKeyPressed(Slot focusedSlot, Inventory playerInventory, CallbackInfoReturnable<Boolean> info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        Slot finalSlot = focusedSlot;
        if(finalSlot instanceof CreativeModeInventoryScreen.SlotWrapper) {
            finalSlot = ((CreativeSlotAccessor) finalSlot).getTarget();
        }
        if(finalSlot != null && finalSlot.container == playerInventory && SlotLock.isLocked(((SlotAccessor) finalSlot).getIndex())) {
            info.setReturnValue(false);
        }
    }

    public static void handleDropSelectedItem(Inventory playerInventory, CallbackInfoReturnable<Boolean> info) {
        if(!Minecraft.getInstance().isSameThread()) return;
        int selectedSlot = playerInventory.selected;
        if(SlotLock.isLocked(selectedSlot)) {
            info.setReturnValue(false);
        }
    }

    public static void handleInputEvents(Options options, LocalPlayer player) {
        if(!Minecraft.getInstance().isSameThread()) return;
        boolean toPress = false;
        while(options.keySwapOffhand.isDown()) {
            if (!player.isSpectator()) {
                int selectedSlot = player.getInventory().selected;
                if(!SlotLock.isLocked(selectedSlot) && !SlotLock.isLocked(40)) {
                    toPress = true;
                }
            }
        }
        if(toPress) KeyMapping.click(((KeyBindingAccessor) options.keySwapOffhand).getKey());
    }

    public static void handleItemPick(int selectedSlot, CallbackInfo info) {
        if(SlotLock.isLocked(selectedSlot)) {
            info.cancel();
        }
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (first.getItem() != second.getItem()) {
            return false;
        } else if (first.getDamageValue() != second.getDamageValue()) {
            return false;
        } else if (first.getCount() > first.getMaxStackSize()) {
            return false;
        } else {
            return ItemStack.isSameItem(first, second);
        }
    }

}
