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
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GlobalSackListGUI {
    private final Main plugin;
    private int page = 0;
    public final Set<UUID> awaitingSackName = new HashSet<>();

    public GlobalSackListGUI(Main plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page) {
        this.page = page;
        GUIHolder holder = new GUIHolder("GLOBAL_SACK_LIST");
        Inventory inv = Bukkit.createInventory(holder, 54, xyz.redoxlabs.redeemcodes.utils.MessageUtil.format("&8🎒 ꜱᴀᴄᴋ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ"));
        holder.setInventory(inv);
        GUIUtils.fillBorder(inv, XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE);

        List<String> sacks = Arrays.asList(plugin.getSackManager().getSackNames());

        int start = page * 28;
        int end = Math.min(start + 28, sacks.size());
        int index = 0;

        for (int row = 1; row <= 4; ++row) {
            for (int col = 1; col <= 7 && start + index < end; ++col) {
                int slot = row * 9 + col;
                String name = sacks.get(start + index);
                ItemStack item = HeadManager.getHead("REWARD_SACK", name);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = new ArrayList<>();
                    lore.add("§7ʟᴇꜰᴛ-ᴄʟɪᴄᴋ ᴛᴏ ᴇᴅɪᴛ ꜱᴀᴄᴋ");
                    lore.add("§7ꜱʜɪꜰᴛ+ᴄʟɪᴄᴋ ᴛᴏ ᴅᴇʟᴇᴛᴇ");
                    meta.setLore(lore);
                    meta.setDisplayName("§x§4§5§D§1§5§8" + name);
                    item.setItemMeta(meta);
                }
                inv.setItem(slot, item);
                ++index;
            }
        }

        if (page > 0) {
            inv.setItem(48, HeadManager.getHead("PREV_PAGE", "§7Previous Page"));
        }
        if (end < sacks.size()) {
            inv.setItem(53, HeadManager.getHead("NEXT_PAGE", "§7Next Page"));
        }

        inv.setItem(49, HeadManager.getHead("GENERIC_ADD", "§aAdd New Sack", "§7Click to create a new sack"));
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
                plugin.openGlobalSackGUIs.remove(player);
                AdminPanelGUI.open(player, plugin);
            } else if (name.equals("Next Page")) {
                SoundUtil.playPageTurn(plugin, player);
                open(player, page + 1);
            } else if (name.equals("Previous Page")) {
                SoundUtil.playPageTurn(plugin, player);
                open(player, Math.max(0, page - 1));
            } else if (name.equals("Add New Sack")) {
                SoundUtil.playClick(plugin, player);
                awaitingSackName.add(player.getUniqueId());
                player.closeInventory();
                xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.create-sack", "&#00BFFFPlease type the name for the new sack in chat.\n&#E0E0E0Type 'cancel' to abort."));
            } else if (XMaterial.matchXMaterial(clicked) != XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE) {
                String sackName = ChatColor.stripColor(name);
                if (event.isShiftClick()) {
                    SoundUtil.playClick(plugin, player);
                    if (plugin.getSackManager().deleteSack(sackName)) {
                        xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.sack.deleted", "&#32CD32Sack deleted."));
                        open(player, page);
                    }
                } else {
                    SoundUtil.playClick(plugin, player);
                    plugin.getSackManager().openEditGUI(player, sackName);
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
            awaitingSackName.remove(uuid);
            plugin.getFoliaLib().getImpl().runAtEntity(player, task -> open(player, 0));
            return;
        }

        plugin.getFoliaLib().getImpl().runAtEntity(player, task -> {
            if (awaitingSackName.contains(uuid)) {
                String sackName = msg.replace(" ", "");
                if (!sackName.matches("^[a-zA-Z0-9_]+$")) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("guis.prompts.invalid-name", "&#FF6347Invalid sack name! Use only letters, numbers, and underscores."));
                    return;
                }
                
                if (plugin.getSackManager().createSack(sackName)) {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.sack.created", "&#32CD32Created new sack: &#00BFFF") + sackName);
                } else {
                    xyz.redoxlabs.redeemcodes.utils.MessageUtil.sendMenuMessage(plugin, player, plugin.getMessagesConfig().getString("handlers.sack.already-exists", "&#FF6347Sack already exists."));
                }
                
                awaitingSackName.remove(uuid);
                open(player, 0);
            }
        });
    }
}
