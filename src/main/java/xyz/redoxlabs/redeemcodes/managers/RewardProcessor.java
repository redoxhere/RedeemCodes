package xyz.redoxlabs.redeemcodes.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import xyz.redoxlabs.redeemcodes.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewardProcessor {

    private final Main plugin;
    private final Random random = new Random();

    public RewardProcessor(Main plugin) {
        this.plugin = plugin;
    }

    public void processRewards(Player player, String code, FileConfiguration codes, boolean dryRun) {
        String rewardPath = "Codes." + code + ".rewards";
        
        if (codes.contains(rewardPath) && codes.isList(rewardPath)) {
            for (String cmd : codes.getStringList(rewardPath)) {
                if (dryRun) {
                    player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Would execute legacy command: &f" + cmd));
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsePlaceholders(cmd, player));
                }
            }
        }

        String type = codes.getString(rewardPath + ".type", "ALL").toUpperCase();
        List<RewardEntry> allRewards = new ArrayList<>();

        if (codes.isConfigurationSection(rewardPath + ".commands")) {
            ConfigurationSection cmdSection = codes.getConfigurationSection(rewardPath + ".commands");
            if (cmdSection != null) {
                for (String packName : cmdSection.getKeys(false)) {
                    List<String> commands = cmdSection.getStringList(packName);
                    int weight = 1;
                    List<String> cleanCommands = new ArrayList<>();

                    for (String line : commands) {
                        if (line.toLowerCase().startsWith("weight:")) {
                            try {
                                weight = Integer.parseInt(line.split(":")[1].trim());
                            } catch (Exception var24) {
                                weight = 1;
                            }
                        } else {
                            cleanCommands.add(line);
                        }
                    }

                    allRewards.add(new RewardEntry(RewardType.COMMAND_PACK, packName, weight, cleanCommands));
                }
            }
        }

        if (codes.contains(rewardPath + ".sacks")) {
            for (String entry : codes.getStringList(rewardPath + ".sacks")) {
                String[] parts = entry.split(":");
                String name = parts[0];
                int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
                allRewards.add(new RewardEntry(RewardType.SACK, name, weight, null));
            }
        }

        if (codes.contains(rewardPath + ".premades")) {
            for (String entry : codes.getStringList(rewardPath + ".premades")) {
                String[] parts = entry.split(":");
                String name = parts[0];
                int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
                allRewards.add(new RewardEntry(RewardType.PREMADE, name, weight, null));
            }
        }

        if (codes.contains(rewardPath + ".events")) {
            for (String entry : codes.getStringList(rewardPath + ".events")) {
                String[] parts = entry.split(":");
                String name = parts[0];
                int weight = parts.length > 1 ? parseInt(parts[1]) : 1;
                allRewards.add(new RewardEntry(RewardType.EVENT, name, weight, null));
            }
        }

        if (!allRewards.isEmpty()) {
            if (type.equals("ALL")) {
                for (RewardEntry reward : allRewards) {
                    executeReward(player, reward, dryRun);
                }
            } else if (type.equals("RANDOM")) {
                RewardEntry selected = allRewards.get(random.nextInt(allRewards.size()));
                executeReward(player, selected, dryRun);
            } else if (type.equals("DRAW")) {
                int totalWeight = allRewards.stream().mapToInt(r -> r.weight).sum();
                if (totalWeight <= 0) {
                    executeReward(player, allRewards.get(random.nextInt(allRewards.size())), dryRun);
                } else {
                    int rand = random.nextInt(totalWeight);
                    int current = 0;
                    for (RewardEntry reward : allRewards) {
                        current += reward.weight;
                        if (current > rand) {
                            executeReward(player, reward, dryRun);
                            break;
                        }
                    }
                }
            } else {
                for (RewardEntry reward : allRewards) {
                    executeReward(player, reward, dryRun);
                }
            }
        }
    }

    private void executeReward(Player player, RewardEntry reward, boolean dryRun) {
        switch (reward.type) {
            case COMMAND_PACK:
                if (dryRun) {
                    player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Selected command pack: &e" + reward.key));
                }
                if (reward.commands != null) {
                    for (String cmd : reward.commands) {
                        if (dryRun) {
                            player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Would execute command: &f" + cmd));
                        } else {
                            String parsed = parsePlaceholders(cmd, player);
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                        }
                    }
                }
                break;
            case SACK:
                if (dryRun) {
                    player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Would give sack: &b" + reward.key));
                } else {
                    plugin.getSackManager().giveSack(player, reward.key);
                }
                break;
            case PREMADE:
                if (dryRun) {
                    player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Would execute premade: &9" + reward.key));
                } else {
                    for (String cmd : plugin.getPremadeManager().getPremadeCommands(reward.key)) {
                        String parsed = parsePlaceholders(cmd, player);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
                    }
                }
                break;
            case EVENT:
                if (dryRun) {
                    player.sendMessage(plugin.color("&8[&aDRY RUN&8] &7Would play event: &6" + reward.key));
                } else {
                    plugin.getEventManager().executeEvent(player, reward.key);
                }
                break;
        }
    }

    private String parsePlaceholders(String input, Player player) {
        input = input.replace("%player%", player.getName());
        input = input.replace("%uuid%", player.getUniqueId().toString());
        input = input.replace("%displayname%", player.getDisplayName());
        input = input.replace("%world%", player.getWorld().getName());
        
        Pattern randomPattern = Pattern.compile("%random-(\\d+)-(\\d+)%");
        Matcher matcher = randomPattern.matcher(input);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(2));
            int result = random.nextInt(max - min + 1) + min;
            matcher.appendReplacement(sb, String.valueOf(result));
        }

        matcher.appendTail(sb);
        String resultText = sb.toString();
        
        if (player != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            resultText = xyz.redoxlabs.redeemcodes.utils.PAPIUtil.setPlaceholders(player, resultText);
        }
        
        return resultText;
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private enum RewardType {
        COMMAND_PACK,
        SACK,
        PREMADE,
        EVENT;
    }

    private static class RewardEntry {
        RewardType type;
        String key;
        int weight;
        List<String> commands;

        public RewardEntry(RewardType type, String key, int weight, List<String> commands) {
            this.type = type;
            this.key = key;
            this.weight = weight;
            this.commands = commands;
        }
    }
}
