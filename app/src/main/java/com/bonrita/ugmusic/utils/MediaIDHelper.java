package com.bonrita.ugmusic.utils;

/**
 * Utility class to help on queue related tasks.
 */
public class MediaIDHelper {
    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static final String MEDIA_ID_EMPTY_ROOT = "__EMPTY_ROOT__";
    public static final String MEDIA_ID_MUSICS_BY_GENRE = "__BY_GENRE__";
    public static final String MEDIA_ID_MUSICS_BY_ARTIST = "__BY_ARTIST__";
    public static final String CATEGORY_SEPARATOR = "/";
    public static final String MUSIC_SEPARATOR = "|";

    /**
     *
     * @param musicID Unique music ID for playable items, or null for browseable items.
     * @param categories hierarchy of categories representing this item's browsing parents
     * @return a hierarchy-aware media ID
     */
    public static String createMediaID(String musicID, String... categories) {
        StringBuilder sb = new StringBuilder();
        if (categories != null) {
            for (int i = 0; i < categories.length; i++) {
                if (!isValidCategory(categories[i])) {
                    throw new IllegalArgumentException("Invalid category: " + categories[i]);
                }
                sb.append(categories[i]);
                if (i < categories.length - 1) {
                    sb.append(CATEGORY_SEPARATOR);
                }
            }
        }

        if (musicID != null) {
            if (sb.length() > 0) {
                sb.append(MUSIC_SEPARATOR);
            }
            sb.append(musicID);
        }

        return sb.toString();
    }

    private static boolean isValidCategory(String category) {
        if (category != null) {
            return (category.indexOf(CATEGORY_SEPARATOR) < 0 || category.indexOf("|") < 0);
        }

        return false;
    }

}
