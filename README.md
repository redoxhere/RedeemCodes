<div align="center">
  <img src="https://cdn.modrinth.com/data/cached_images/2018cfdff840876bfa1c45b362fea47751057238.png" alt="RedeemCodes Minecraft Plugin Banner" />
  
  <h1>RedeemCodes - The Ultimate Minecraft Voucher & Rewards Plugin</h1>
  <p><b>Create custom redeem codes, vouchers, and rewards for your Minecraft server with infinite possibilities!</b></p>
</div>

**RedeemCodes** is the most advanced, free **Minecraft Redeem Codes and Vouchers plugin** available for Spigot, Paper, and Folia servers. Whether you are running a survival SMP, Skyblock, or Factions server, RedeemCodes allows server administrators to easily create, manage, and distribute custom codes to reward their players. 

From simple item drops to complex gacha-style reward pools, RedeemCodes provides everything you need to boost player engagement through giveaways, vote rewards, and VIP perks.

### 🌟 Key Features

* **Custom Redeemable Codes** – Define unlimited codes with flexible reward options.
* **Blacklist/Whitelist System** – Block or allow specific players for each code.
* **Redemption Limits & Cooldowns** – Set per-player, per-IP, or global redemption limits with cooldown timers.
* **Permission System** - Develop permission-based codes to limit usage to a specific rank or group
* **Events** - Trigger customizable actions such as fireworks, command sets, or animations when a reward is claimed
* **Interactive GUI Editor** – Manage codes and rewards directly in an intuitive in-game GUI.
* **Interaction Sounds** – Configurable sounds that play on events, successful or failed redemption.
* **Permission-Based Access** – Every admin command is fully protected by permissions.
* **Fully Configurable** – Customize all messages, sounds, and prefix in `config.yml`.
* **File Tracker** - Monitors all plugin files and notifies admins about invalid formats, syntax issues, and errors  
* **Async Data Management** - Reads and writes data asynchronously to ensure smooth, lag-free file management
* **Infinite Rewards** - Create command-based rewards that execute console commands, or design custom rewards with our various reward types, unlocking infinite possibilities.

### Reward System

Build anything from simple rewards to complex gacha-style reward pools by combining multiple reward types.

* **Multiple Distribution Modes** – Choose how rewards are given:
  * **ALL** – Grant every configured reward.
  * **RANDOM** – Select one reward pack randomly.
  * **DRAW** – Select one reward using configurable weighted chances.
* **Command Packs** – Group multiple console commands into reusable reward packs.
* **Sacks** – Give predefined item collections directly to the player's inventory.
* **Premade Rewards** – Reuse commonly used reward sets defined in `premades.yml`.
* **Reward Events** – Trigger custom celebrations, animations, sounds, fireworks, or other effects from the `events` folder.
* **Weighted Rewards** – Assign weights to reward packs for loot box or gacha-style reward systems.
* **Mix & Match Categories** – Combine commands, sacks, premades, and events in a single reward for highly customizable redemption experiences.
* **Placeholder Support** – Supports PlaceholderAPI and built-in placeholders.

### Built-in Placeholders

* `%player%` – Player's name
* `%uuid%` – Player's UUID
* `%displayname%` – Player's display name
* `%world%` – World name
* `%random-min-max%` – Random number within a range

Example reward configuration:

```yaml
rewards:
  type: DRAW

  events:
    - celebration

  commands:
    lucky_cash:
      - "eco give %player% %random-500-1000%"
      - "msg %player% &aYou won a cash reward!"
      - "weight: 10"

  sacks:
    - starter: 5
    - miner: 2

  premades:
    - vip_rank: 1
```

### Commands & Permissions

**Player Command**

```plaintext
/redeem <code>
```

Redeem a code and claim its reward.
⤷ *Permission: None (available to all players)*

**Admin Commands**

| Command | Description |
| :--- | :--- |
| `/rc create <new|copy> <name> [new-name]` | Create a new code or copy an existing one. |
| `/rc remove <code>` | Delete a code completely. |
| `/rc list` | List all existing codes with their active status. |
| `/rc show <code>` | Show details and configured rewards of a code. |
| `/rc edit <code>` | Edit a code and its properties |
| `/rc redeemed <code> [page]`| View a paginated list of players who redeemed a code. |
| `/rc reward` | Add, remove, or view code rewards. |
| `/rc sack` | Add, remove, edit, or give Sack integrations. |
| `/rc premade` | Add, remove, or view Premade reward sets. |
| `/rc event` | Add, remove, or test Event hooks. |
| `/rc reload` | Reload the configuration safely. |
| `/rc version` | View the active plugin version. |
| `/rc test <code>` | Dry run a code to safely test its rewards. |
| `/rc info` | View detailed plugin information and credits. |
| `/rc help [page]` | Show the help menu. |
| `/rc review <message>` | Send a review or bug report to the developers. |

*Note: All admin commands support seamless **Tab Completion** and can be run from the Server Console.*

⤷ *Permission: `redeemcodes.admin`*


### GUI System

The plugin includes a modern and easy-to-use GUI for code management:



<details>
<summary>Main Menu</summary>
Create Code, Codes List, Admin Panel<br><br>


![RedeemCodes Menu](https://cdn.modrinth.com/data/cached_images/e18682b3eed35a24d04218d5c268c7574e3f4a95.png)

</details>

<details>
<summary>Code List Menu</summary>
View all available codes at one glance<br><br>


![Codeslist](https://cdn.modrinth.com/data/cached_images/04b1c2ff452fd8c3932b188e3ca825b7647dcfa3.png)!

</details>

<details>
<summary>Code Editor Menu</summary>
Edit rewards, settings, and options directly in-game<br><br>


![Code editor](https://cdn.modrinth.com/data/cached_images/3f2f5a5ccc9a512f472b72afde68c135c3939fdb.png)

</details>
   

### Example Code Setup

<details>
<summary>codes.yml</summary>

```yaml
# ==============================================================================
#                           REDEEM CODES CONFIGURATION
# ==============================================================================
# This file contains all the redemption codes and their settings.
# You can create codes using the in-game GUI command: /rc gui
# ==============================================================================

Codes:
  # Example Code: "welcome_gift"
  # Users redeem this by typing: /redeem welcome_gift
  welcome_gift:

    # Enable or disable the code instantly.
    enabled: true

    # Permission Settings [Requires a permission plugin like LuckPerms]
    # If required is true, the player must have one of the listed permissions.
    permisson:
      required: false
      list:
        - code.redeem.welcome

    # Redemption Limits
    # player: Maximum times a single player can redeem this code.
    # ip: Maximum times a single IP address can redeem this code.
    # global: Maximum times this code can be redeemed across the entire server.
    redeem-limit:
      player: 1 # Number of times allowed per player (Set to -1 for infinite).
      ip: 1 # Number of times allowed per ip address (Set to -1 for infinite).
      global: -1 # Number of codes in stock globally (Set to -1 for infinite).
      cooldown: 0 # Cooldown in minutes between uses (0 to disable).
      cooldown-message: "&cYou must wait %Cooldown% before redeeming again!"

    # Expiration time in seconds. -1 means it never expires.
    expire-time: -1

    # Player Data & Blacklisting
    Playerlist:
      # Blacklist Control
      # Type:
      #   ENABLED  -> Players in the list CANNOT use the code.
      #   REVERSE  -> Only players in the list CAN use the code (Whitelist mode).
      #   DISABLED -> The list is ignored.
      Blacklist:
        Type: ENABLED
        List:
          - notch
          - herobrine

    # ==========================================================================
    #                                 REWARDS
    # ==========================================================================
    # The reward system allows you to mix and match different types of rewards.
    #
    # Distribution Types (rewards.type):
    #   ALL    -> The player receives EVERY reward defined below.
    #   RANDOM -> The player receives ONE reward selected randomly from all defined packs.
    #   DRAW   -> The player receives ONE reward based on a weighted chance system.
    #
    # Reward Categories:
    #   1. Commands: A list of console commands to execute.
    #   2. Sacks: A collection of items given directly to the inventory (defined in /sacks/).
    #   3. Premades: Reusable command lists defined in premades.yml.
    #   4. Events: Special visual/audio effects defined in /events/.
    #
    # Placeholders for commands:
    #   %player%      -> Player Name
    #   %uuid%        -> Player UUID
    #   %world%       -> World Name
    #   %displayname% -> Display Name
    #   %random-x-y%  -> Random number between x and y
    # ==========================================================================

    rewards:
      type: ALL

      # --- Events ---
      # Trigger visual/sound effects from files in the 'events' folder.
      events:
        - celebration # Plays 'events/celebration.yml'

      # --- Command Packs ---
      # Custom groups of commands specific to this code.
      commands:
        basic_stuff:
          - "say Welcome %player%!"
          - "give %player% apple 5"
          - "weight: 10" # Used if type is DRAW (higher weight = higher chance)

        lucky_roll:
          - "eco give %player% %random-50-100%"
          - "msg %player% &aYou got some lucky cash!"
          - "weight: 5"

      # --- Sacks ---
      # Items defined in the 'sacks' folder. Format: "sackname:weight"
      # If type is ALL, weight is ignored.
      sacks:
        - starter: 1
        - miner: 1

      # --- Premades ---
      # Reusable rewards defined in 'premades.yml'. Format: "premadename:weight"
      premades:
        - vip_rank: 1
        - money_small: 2
```

</details>  


### Configurable Sounds

```yaml
sounds:
  success: ENTITY_PLAYER_LEVELUP
  failure: BLOCK_ANVIL_LAND
  click: UI_BUTTON_CLICK
  page-turn: ITEM_BOOK_PAGE_TURN
  error: ENTITY_VILLAGER_NO
  close: BLOCK_CHEST_CLOSE
```


### Fully Customizable Messages

```yaml
general:
  usage: "&cUsage: /redeem <code>"
  not-exist: "&cThis code doesn't exist!"
  no-permission: "&cYou don't have permission to use this."
  code-disabled: "&cThis code is currently disabled."
  out-of-stock: "&cThis code is out of stock!"
  code-expired: "&cThis code has expired."

handlers:
  blacklisted: "&cYou are not allowed to redeem this code."
  already-used: "&eYou have already redeemed this code."
  redeem-success: "&aSuccessfully redeemed the code!"

commands:
  create:
    usage: "&cUsage: /rc create <new|copy> <name> [new-name]"
    exists: "&cCode already exists!"
    success: "&aCreated new code: %code%"
  remove:
    success: "&aRemoved code: %code%"
  reload:
    success: "&aAll configuration files reloaded!"

guis:
  code-editor:
    invalid-number: "&cInvalid number."
```


### Requirements

* Minecraft 1.8.8+
* Server Software: Paper, Purpur, Spigot, Bukkit, Folia


### Why Use RedeemCodes?

Whether you’re organizing events, giveaways, loyalty programs, or simple player rewards, RedeemCodes offers a seamless, lightweight, and customizable way to engage your server community.

