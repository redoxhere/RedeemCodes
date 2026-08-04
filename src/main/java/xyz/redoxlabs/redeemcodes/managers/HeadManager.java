package xyz.redoxlabs.redeemcodes.managers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import com.cryptomorin.xseries.XMaterial;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.cryptomorin.xseries.profiles.builder.XSkull;
import com.cryptomorin.xseries.profiles.objects.Profileable;
public class HeadManager {
   private static final Map<String, ItemStack> cachedHeads = new HashMap<>();
   private static final Map<String, UUID> headUUIDs = new HashMap<>();

   public static void preloadHeads() {
      register("CREATE", "ab478a4b2622ee44e81ad019f5302985659de8c382d29f0ef21e993d43d87d3");
      register("LIST", "6f9c7e59a2cebc0584f3bf71bbdaaf199c4921c2310723efdcb4eb6bcb9a13fc");
      register("ADMIN", "cfd3f7d43d360923b1c04445b01022466b50ca58df9893267121081e0c1e2f4e");
      register("CLOSE", "beb588b21a6f98ad1ff4e085c552dcb050efc9cab427f46048f18fc803475f7");
      register("CODE_ITEM", "ca18def035f87f7616404978208926be7476b579dfa360f0c2269ef2d4029c");
      register("BACK", "223fb67429716b21bc6e8e7d669ceddf65b13e0790a5ce55b2e077b82d19e124");
      register("NEXT_PAGE", "8271a47104495e357c3e8e80f511a9f102b0700ca9b88e88b795d33ff20105eb");
      register("PREV_PAGE", "69ea1d86247f4af351ed1866bca6a3040a06c68177c78e42316a1098e60fb7d3");
      register("EXPIRE_TIME", "85fa0a3a2d62d7d1171d48b3ae8fcb551f6faccc70cee40f44c767c3da7b785f");
      register("REACTIVATE", "ca50b3b3bf82165645a9335817b46de933e8b92a58304dc591b98d6033404447");
      register("EXPIRED_LIST", "85fa0a3a2d62d7d1171d48b3ae8fcb551f6faccc70cee40f44c767c3da7b785f");
      register("EXPIRED_CODE_ITEM", "266d513dd1a03655af361ce5fd3e66b890b2b8866f0ebd14a82e9e6568d7db86");
      register("ADD_SOUND", "7beb2d7368bfb8656f3948e126df8b858c70ddf54283f494fe1c1675056178b4");
      register("SOUND_ITEM", "9c250b47e10a50d69166cb0ecaf234100cae7899a4898108c1307017a5a7ebb3");
      register("REWARD_ADD", "ab478a4b2622ee44e81ad019f5302985659de8c382d29f0ef21e993d43d87d3");
      register("REWARD_EVENT", "4c0d3dac525e843f2fcfab4157a57360d69ecc73ba2f8282741ed86a638949ab");
      register("REWARD_TYPE", "4eacc840b17ad8cc332066a41b93277b616b3143334065852dc7bc89b5af0915");
      register("REWARD_COMMAND", "eb6cee8fda7ef0b3ae0eb0579d5676ce36af7efc574d88728f3894f6b166538");
      register("REWARD_SACK", "6cc06ace3df0ec3483a882873c9532113e1542fe7a3e2ac2c24905d24b0fa6af");
      register("REWARD_PREMADE", "756bb243fcbda7803ede16f02b615636dd2d7252e17dde9131424c68a4d5aca9");
      register("GENERIC_ADD", "bd0f7034c1ac9f65f94aabe30c69ef7e7bc08cc0ca287a39af140436e5093aae");
      register("GENERIC_REMOVE", "60335c5089b28f7e1261749bf107fa675900c1956ca9e6d851ba55a5e1f8624a");
      register("COMMAND_PACK", "3687a2bcaf83ea8a3174d03d3900991b67e65c8ca1cc4d95a0b2cb717967a627");
   }

   public static Map<String, ItemStack> getAllCachedHeads() {
      return new HashMap(cachedHeads);
   }

   private static void register(String key, String textureId) {
      UUID consistentUUID = headUUIDs.computeIfAbsent(key, (k) -> UUID.nameUUIDFromBytes(k.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
      ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
      if (head == null) return;
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      if (meta != null) {
         try {
            String profileIdStr = consistentUUID.toString().replace("-", "");
            String base64 = java.util.Base64.getEncoder().encodeToString(("{\"timestamp\":1,\"profileId\":\"" + profileIdStr + "\",\"profileName\":\"NPC\",\"textures\":{\"SKIN\":{\"url\":\"https://textures.minecraft.net/texture/" + textureId + "\"}}}").getBytes());
            setHeadTexture(meta, base64, consistentUUID);
            head.setItemMeta(meta);
            System.out.println("[RedeemCodes] Successfully loaded head texture for key: " + key);
         } catch (Exception e) {
            System.err.println("[RedeemCodes] ERROR: Failed to load head texture for key '" + key + "' - " + e.getMessage());
         }
      } else {
         System.err.println("[RedeemCodes] ERROR: Failed to get SkullMeta for key '" + key + "'");
      }

      cachedHeads.put(key, head);
   }

   private static void registerBase64(String key, String base64Texture) {
      ItemStack head = XMaterial.PLAYER_HEAD.parseItem();
      if (head == null) return;
      SkullMeta meta = (SkullMeta)head.getItemMeta();
      if (meta != null) {
         try {
            String decodedTexture = new String(java.util.Base64.getDecoder().decode(base64Texture));
            System.out.println("[RedeemCodes] DEBUG: Decoded Base64 for '" + key + "': " + decodedTexture);
            String textureUrl = null;
            if (decodedTexture.contains("\"url\":\"")) {
               int start = decodedTexture.indexOf("\"url\":\"") + 7;
               int end = decodedTexture.indexOf("\"", start);
               if (end > start) {
                  textureUrl = decodedTexture.substring(start, end);
               }
            } else if (decodedTexture.contains("url:")) {
               int start = decodedTexture.indexOf("url:") + 4;
               int end = decodedTexture.indexOf("\"", start);
               if (end == -1) {
                  end = decodedTexture.indexOf("}", start);
               }

               if (end > start) {
                  textureUrl = decodedTexture.substring(start, end).replace("\"", "").trim();
               }
            }

            if (textureUrl != null && !textureUrl.isEmpty()) {
               System.out.println("[RedeemCodes] DEBUG: Extracted texture URL for '" + key + "': " + textureUrl);
               UUID consistentUUID = headUUIDs.computeIfAbsent(key, (k) -> UUID.nameUUIDFromBytes(k.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
               String profileIdStr = consistentUUID.toString().replace("-", "");
               String base64 = java.util.Base64.getEncoder().encodeToString(("{\"timestamp\":1,\"profileId\":\"" + profileIdStr + "\",\"profileName\":\"NPC\",\"textures\":{\"SKIN\":{\"url\":\"" + textureUrl + "\"}}}").getBytes());
               setHeadTexture(meta, base64, consistentUUID);
               head.setItemMeta(meta);
               System.out.println("[RedeemCodes] Successfully loaded Base64 head texture for key: " + key);
            } else {
               System.err.println("[RedeemCodes] ERROR: Failed to parse texture URL from Base64 for key '" + key + "' - URL is null or empty");
               System.err.println("[RedeemCodes] DEBUG: Decoded content: " + decodedTexture);
            }
         } catch (IllegalArgumentException e) {
            System.err.println("[RedeemCodes] ERROR: Invalid Base64 format for key '" + key + "' - " + e.getMessage());
         } catch (Exception e) {
            System.err.println("[RedeemCodes] ERROR: Failed to load Base64 head texture for key '" + key + "' - " + e.getMessage());
            e.printStackTrace();
         }
      } else {
         System.err.println("[RedeemCodes] ERROR: Failed to get SkullMeta for Base64 key '" + key + "'");
      }

      cachedHeads.put(key, head);
   }

   public static ItemStack getHead(String key, String displayName, String... lore) {
      ItemStack base = (ItemStack)cachedHeads.get(key);
      if (base == null) {
         if (key.startsWith("REWARD") || key.startsWith("GENERIC")) {
            if (key.contains("ADD")) {
               base = (ItemStack)cachedHeads.get("ADD_SOUND");
            } else if (key.contains("REMOVE")) {
               base = (ItemStack)cachedHeads.get("CLOSE");
            } else {
               base = (ItemStack)cachedHeads.get("CODE_ITEM");
            }
         }

         if (base == null) {
            System.out.println("[RedeemCodes] WARN: Missing head texture for key: " + key);
            return XMaterial.PLAYER_HEAD.parseItem();
         }
      }

      ItemStack clone = base.clone();
      SkullMeta meta = (SkullMeta)clone.getItemMeta();
      if (meta != null) {
         meta.setDisplayName(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(displayName));
         if (lore != null && lore.length > 0) {
            java.util.List<String> formattedLore = new java.util.ArrayList<>();
            for (String l : lore) formattedLore.add(xyz.redoxlabs.redeemcodes.utils.MessageUtil.format(l));
            meta.setLore(formattedLore);
         }
         
         meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES, org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

         clone.setItemMeta(meta);
      }

      return clone;
   }

   private static void setHeadTexture(SkullMeta meta, String texture, UUID uuid) {
       try {
           XSkull.of(meta).profile(Profileable.detect(texture)).apply();
       } catch (Exception e) {
           e.printStackTrace();
       }
   }
}



