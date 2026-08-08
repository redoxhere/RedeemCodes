# RedeemCodes Data Migration Guide

This document explains how the data migration pipeline works and how developers should add new migrations to handle config layout changes, new features, or complete database overhauls.

## Core Concepts

The plugin uses a **Version-Based Migration Pipeline**.
- The current data version is stored in `config.yml` as `data-version`.
- If an admin updates the plugin, the `MigrationManager` compares `data-version` with the plugin's `.jar` version.
- If the plugin version is newer, the `MigrationManager` looks through a registered **Path** (a list of migrations) and executes any migration that targets a version strictly newer than the current `data-version` but older than or equal to the `.jar` version.
- Before any migration is run, a backup of all data files is automatically created in `plugins/RedeemCodes/backups/`. If a migration fails, the original data is seamlessly reverted.

## How to Create a New Migration

Whenever you make a structural change to `.yml` files or move to a database (e.g. SQLite), you must write a new Migration.

1. **Create the Migration Class**
   Inside `src/main/java/xyz/redoxlabs/redeemcodes/migrations/`, create a new package for the major version if it doesn't exist (e.g., `v27`).
   Create a class implementing the `Migration` interface.
   
   ```java
   package xyz.redoxlabs.redeemcodes.migrations.v27;
   
   import xyz.redoxlabs.redeemcodes.Main;
   import xyz.redoxlabs.redeemcodes.migrations.Migration;
   
   public class Migration_27_0 implements Migration {
       @Override
       public String getTargetVersion() {
           return "27.0.0"; // The plugin version this migration upgrades the data TO
       }
   
       @Override
       public boolean execute(Main plugin) throws Exception {
           // Do your config/database manipulation here.
           // Example: plugin.getCodesConfig().set("OldKey", null);
           // plugin.saveCodesConfig();
           
           return true; // Return false or throw an Exception if it fails
       }
   }
   ```

2. **Register the Migration to the Path**
   Open `MigrationManager.java`.
   Add your new class to the `MIGRATION_PATH` list in the static block. 
   **IMPORTANT:** Ensure it is added in chronological order!

   ```java
   static {
       MIGRATION_PATH.add(new Migration_26_1_Beta_3());
       // Add your new migration BELOW the older ones
       MIGRATION_PATH.add(new Migration_27_0());
   }
   ```

## Rules for Migrations
- **Never rely on `config.yml` comments**: Migrations using standard YamlConfiguration will strip comments when saving. If you are manipulating `config.yml`, use `FileTracker` or manipulate the nodes directly and accept comment loss for that specific node block.
- **Fail Gracefully**: If your migration encounters missing critical data, throw an `Exception`. The `MigrationManager` will automatically catch it, revert the files from the backup, and disable the plugin safely.
- **Do not update `data-version` yourself**: The `MigrationManager` automatically updates `config.yml` with the new version after your `execute()` method successfully returns `true`.
