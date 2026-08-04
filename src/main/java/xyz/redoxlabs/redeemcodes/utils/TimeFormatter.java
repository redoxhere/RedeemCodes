package xyz.redoxlabs.redeemcodes.utils;

import java.util.concurrent.TimeUnit;

public class TimeFormatter {
   public static String formatDuration(long millis) {
      if (millis < 0L) {
         return "N/A";
      } else {
         long days = TimeUnit.MILLISECONDS.toDays(millis);
         millis -= TimeUnit.DAYS.toMillis(days);
         long hours = TimeUnit.MILLISECONDS.toHours(millis);
         millis -= TimeUnit.HOURS.toMillis(hours);
         long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
         millis -= TimeUnit.MINUTES.toMillis(minutes);
         long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
         StringBuilder sb = new StringBuilder();
         if (days > 0L) {
            sb.append(days).append("d ");
         }

         if (hours > 0L) {
            sb.append(hours).append("h ");
         }

         if (minutes > 0L) {
            sb.append(minutes).append("m ");
         }

         if (seconds > 0L || sb.length() == 0) {
            sb.append(seconds).append("s");
         }

         return sb.toString().trim();
      }
   }

   public static long parseTimeToMinutes(String input) {
      java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*(s|m|h|d|w|mn|y)", 2);
      java.util.regex.Matcher m = p.matcher(input.toLowerCase().trim());
      if (!m.matches()) {
         return -1L;
      } else {
         long value = Long.parseLong(m.group(1));
         switch (m.group(2)) {
            case "s":
               return value / 60L;
            case "m":
               return value;
            case "h":
               return value * 60L;
            case "d":
               return value * 60L * 24L;
            case "w":
               return value * 60L * 24L * 7L;
            case "mn":
               return value * 60L * 24L * 30L;
            case "y":
               return value * 60L * 24L * 365L;
            default:
               return -1L;
         }
      }
   }

   public static long parseTimeToSeconds(String input) {
      java.util.regex.Pattern p = java.util.regex.Pattern.compile("(\\d+)\\s*(s|m|h|d|w|mn|y)", 2);
      java.util.regex.Matcher m = p.matcher(input.toLowerCase().trim());
      if (!m.matches()) {
         return -1L;
      } else {
         long value = Long.parseLong(m.group(1));
         switch (m.group(2)) {
            case "s":
               return value;
            case "m":
               return value * 60L;
            case "h":
               return value * 60L * 60L;
            case "d":
               return value * 60L * 60L * 24L;
            case "w":
               return value * 60L * 60L * 24L * 7L;
            case "mn":
               return value * 60L * 60L * 24L * 30L;
            case "y":
               return value * 60L * 60L * 24L * 365L;
            default:
               return -1L;
         }
      }
   }
}



