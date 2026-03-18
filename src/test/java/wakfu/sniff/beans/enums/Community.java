package wakfu.sniff.beans.enums;

import lombok.Getter;

public enum Community {
    FR(0, "fr"),
    UK(1, "en"),
    INT(2, "int"),
    DE(3, "de"),
    ES(4, "es"),
    RU(5, "ru"),
    PT(6, "pt"),
    NL(7, "nl"),
    JP(8, "jp"),
    IT(9, "it"),
    NA(11, "na"),
    CN(12, "cn"),
    ASIA(13, "asia"),
    TW(14, "tw");

    private static final Community DEFAULT_COMMUNITY = UK;

    @Getter
    private final int id;
    @Getter
    private final String name;


    Community(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Community getFromId(int id) {
        for (Community community : values()) {
            if (id == community.id) {
                return community;
            }
        }

        return DEFAULT_COMMUNITY;
    }

    public static Community getFromName(String s) {
        if (s == null) return DEFAULT_COMMUNITY;
        for (Community community : values()) {
            if (s.equals(community.name)) {
                return community;
            }
        }

        return DEFAULT_COMMUNITY;
    }
}
