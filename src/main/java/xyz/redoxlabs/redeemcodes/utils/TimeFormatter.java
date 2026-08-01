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
}



