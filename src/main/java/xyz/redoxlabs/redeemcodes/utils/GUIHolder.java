package xyz.redoxlabs.redeemcodes.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class GUIHolder implements InventoryHolder {
    private final String guiType;
    private Inventory inventory;

    public GUIHolder(String guiType) {
        this.guiType = guiType;
    }

    public String getGuiType() {
        return guiType;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
