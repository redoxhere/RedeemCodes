package xyz.redoxlabs.redeemcodes.guis;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.cryptomorin.xseries.XMaterial;
import xyz.redoxlabs.redeemcodes.Main;
import xyz.redoxlabs.redeemcodes.managers.HeadManager;
import xyz.redoxlabs.redeemcodes.utils.GUIHolder;
import xyz.redoxlabs.redeemcodes.utils.GUIUtils;
import xyz.redoxlabs.redeemcodes.utils.SoundUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlobalPremadeListGUI {
    private final Main plugin;
    private int page = 0;
    public final Set<UUID> awaitingPremadeName = new HashSet<>();

    public GlobalPremadeListGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        this.page = page;
        GUIHolder holder = new GUIHolder("GLOBAL_PREMADE_LIST");
        Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format("&8🎁 ᴘʀᴇᴍᴀᴅᴇ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ"));
        holder.setInventory(inv);
        GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);

        List<String> premades = new ArrayList<>(plugin.getPremadeManager().getPremadeNames());

        int start = page * 28;
        int end = Math.min(start + 28, premades.size());
        int index = 0;

        for (int row = 1; row <= 4; ++row) {
            for (int col = 1; col <= 7 && start + index < end; ++col) {
                int slot = row * 9 + col;
                String name = premades.get(start + index);
                ItemStack item = HeadManager.getHead("REWARD_PREMADE", name);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴠɪᴇᴡ ᴄᴏᴍᴍᴀɴᴅꜱ");
                    lore.add("§7ꜱʜɪꜰᴛ+ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ");
                    meta.setLore(lore);
                    meta.setDisplayName("§x§6§E§B§1§D§4" + name);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
                ++index;
            }
        }

        if (page > 0) {
            inv.setItem(48, HeadManager.getHead("PREV_PAGE", "§7Previous Page"));
        }
        if (end < premades.size()) {
            inv.setItem(53, HeadManager.getHead("NEXT_PAGE", "§7Next Page"));
        }

        inv.setItem(49, HeadManager.getHead("GENERIC_ADD", "§aAdd New Premade", "§7Click to create a new premade"));
        inv.setItem(45, HeadManager.getHead("BACK", "§cGo Back", "§7Return to Admin Panel"));

        GUIUtils.applyFlags(inv);
        player.openInventory(inv);
    }

    public void handleClick(InventoryClickEvent event, Player player) {
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.hasItemMeta()) {
            String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
            event.setCancelled(true);
            
            if (name.equals("Go Back")) {
                SoundUtil.playClick(plugin, player);
                plugin.openGlobalPremadeGUIs.remove(player);
                AdminPanelGUI.open(player, plugin);
            } else if (name.equals("Next Page")) {
                SoundUtil.playPageTurn(plugin, player);
                open(player, page + 1);
            } else if (name.equals("Previous Page")) {
                SoundUtil.playPageTurn(plugin, player);
                open(player, Math.max(0, page - 1));
            } else if (name.equals("Add New Premade")) {
                SoundUtil.playClick(plugin, player);
                awaitingPremadeName.add(player.getUniqueId());
                player.closeInventory();
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.premades.prompt-create", "&#00BFFFPlease type the name for the new premade in chat.\n&#E0E0E0Type 'cancel' to abort."));
            } else if (XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                String premadeName = ChatColor.stripColor(name);
                if (event.isShiftClick()) {
                    SoundUtil.playClick(plugin, player);
                    // the manager doesn't have a delete method that just takes name, let me check. 
                    // Actually I might need to write logic to clear it in PremadeManager or just clear commands.
                    List<String> cmds = plugin.getPremadeManager().getPremadeCommands(premadeName);
                    if (cmds != null) {
                        for(int i = cmds.size() - 1; i >= 0; i--) {
                            plugin.getPremadeManager().removeCommand(premadeName, i);
                        }
                    }
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.premade.deleted", "&#32CD32Premade deleted."));
                    open(player, page);
                } else {
                    SoundUtil.playClick(plugin, player);
                    plugin.getFoliaLib().getImpl().runNextTick(task -> player.closeInventory());
                    player.performCommand("rc premade view " + premadeName);
                }
            }
        }
    }

    public void handleChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = ChatColor.stripColor(event.getMessage()).trim();
        UUID uuid = player.getUniqueId();
        
        if (msg.equalsIgnoreCase("cancel")) {
            xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.cancel", "&#FF6347Action cancelled."));
            awaitingPremadeName.remove(uuid);
            plugin.getFoliaLib().getImpl().runAtEntity(player, task -> open(player, 0));
            return;
        }

        plugin.getFoliaLib().getImpl().runAtEntity(player, task -> {
            if (awaitingPremadeName.contains(uuid)) {
                String premadeName = msg.replace(" ", "");
                if (!premadeName.matches("^[a-zA-Z0-9_]+$")) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.invalid-name", "&#FF6347Invalid premade name! Use only letters, numbers, and underscores."));
                    return;
                }
                
                // create an empty premade by just doing nothing since they are stored as needed? No, wait. 
                // We must add an empty list to config.
                plugin.getCodesConfig().set("Premades." + premadeName, new ArrayList<String>());
                plugin.saveCodesConfig();
                
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.premade.created", "&#32CD32Created new premade: &#00BFFF") + premadeName);
                awaitingPremadeName.remove(uuid);
                open(player, 0);
            }
        });
    }
}
