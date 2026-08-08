package xyz.redoxlabs.redeemcodes.utils;

public class VersionComparator {

    public static int compare(String versionA, String versionB) {
        String[] partsA = versionA.split("\\.");
        String[] partsB = versionB.split("\\.");
        int length = Math.max(partsA.length, partsB.length);

        for (int i = 0; i < length; ++i) {
            String partA = i < partsA.length ? partsA[i].replaceAll("[^0-9]", "") : "0";
            String partB = i < partsB.length ? partsB[i].replaceAll("[^0-9]", "") : "0";
            int numA = partA.isEmpty() ? 0 : Integer.parseInt(partA);
            int numB = partB.isEmpty() ? 0 : Integer.parseInt(partB);
            
            if (numA > numB) {
                return 1;
            }
            if (numA < numB) {
                return -1;
            }
        }
        return 0;
    }

    public static boolean isNewerVersion(String versionA, String versionB) {
        return compare(versionA, versionB) > 0;
    }
}
