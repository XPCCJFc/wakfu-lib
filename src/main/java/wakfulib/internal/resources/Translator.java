package wakfulib.internal.resources;

import static wakfulib.utils.FileHelper.concatPath;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.IllegalFormatException;
import java.util.ListResourceBundle;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.intellij.lang.annotations.MagicConstant;
import wakfulib.annotation.NonNull;
import wakfulib.annotation.Nullable;
import wakfulib.utils.FileHelper;
import wakfulib.utils.StringFormatter;

/**
 * Handles internationalization and translation of strings.
 */
@Slf4j
public class Translator {

    /**
     * The default path to the translation file.
     */
    public static String DEFAULT_PATH = "/i18n/i18n_fr.jar!/texts_fr.properties";

    /**
     * The default charset used for reading translation files.
     */
    public static Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

    private static Translator INSTANCE;
    private final boolean mocked;

    /**
     * Gets the singleton instance of the Translator.
     *
     * @return the Translator instance
     */
    public static Translator getInstance() {
        return INSTANCE;
    }

    /**
     * Sets the Translator instance using a specific path.
     *
     * @param path the path to the translation file
     */
    public static void setInstance(String path) {
        INSTANCE = new Translator(false, path);
    }

    /**
     * Sets the Translator instance using the default path.
     */
    public static void setInstance() {
        INSTANCE = new Translator(false, concatPath(ResourceManager.getRESOURCES_FOLDER_PATH(), DEFAULT_PATH));
    }

    private static void mocked() {
        INSTANCE = new Translator(true, null);
    }

    private final ResourceBundle bundle;

    /**
     * Initializes a new Translator instance.
     * If the translation file at the specified path cannot be loaded, 
     * a mocked bundle will be used to prevent application crashes.
     *
     * @param mocked Whether to force a mocked bundle.
     * @param path The path to the translation property file.
     */
    private Translator(boolean mocked, @Nullable String path) {
        ResourceBundle temp = null;
        if (path == null) mocked = true;
        if (! mocked) {
            try (InputStream stream = FileHelper.openFile(path, true); InputStreamReader reader = new InputStreamReader(stream, DEFAULT_CHARSET)) {
                temp = new PropertyResourceBundle(reader);
            } catch (Exception e) {
                log.warn("- <init>(): An error occurred while reading the translation file '{}', a mock resourceBundle will be used: {} {}", path, e.getClass().getSimpleName(), e.getMessage());
                mocked = true;
            }
        }
        if (mocked) {
            temp = new ListResourceBundle() {
                @Override
                protected Object[][] getContents() {
                    return new Object[0][];
                }
            };
        }
        this.mocked = mocked;
        assert temp != null;
        this.bundle = temp;
    }

    /**
     * Retrieves the localized name of an item.
     *
     * @param itemId The unique identifier of the item.
     * @return The translated item name, or {@code null} if not found.
     */
    @Nullable
    public String getItemName(long itemId) {
        return getString(Keys.ITEM_NAME_TRANSLATION_TYPE, itemId);
    }

    /**
     * Retrieves the localized description of an item.
     *
     * @param itemId The unique identifier of the item.
     * @return The translated item description, or {@code null} if not found.
     */
    @Nullable
    public String getItemDescription(long itemId) {
        return getString(Keys.ITEM_DESCRIPTION_TRANSLATION_TYPE, itemId);
    }

    /**
     * Retrieves the localized name of a spell.
     *
     * @param spellId The unique identifier of the spell.
     * @return The translated spell name, or {@code null} if not found.
     */
    @Nullable
    public String getSpellName(long spellId) {
        return getString(Keys.SPELL_NAME_TRANSLATION_TYPE, spellId);
    }

    @Getter
    private Map<Integer, String> monstersKeys;

    /**
     * Pre-populates a map of all monster names available in the translation bundle.
     * This is useful for bulk processing or searching monsters by name.
     */
    public void initMonsterNames() {
        if (monstersKeys != null) {
            return;
        }
        monstersKeys = new HashMap<>();
        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith("content.7.")) {
                monstersKeys.put(Integer.parseInt(key.substring(10)), bundle.getString(key));
            }
        }
    }

    /**
     * Retrieves all translated values belonging to a specific key category.
     *
     * @param keyId The ID of the category (e.g., items, spells, monsters).
     * @return A map where the key is the element ID and the value is the localized string.
     */
    @NonNull
    public Map<Integer, String> getValuesForKey(@MagicConstant(valuesFromClass = Keys.class) int keyId) {
        Enumeration<String> keys = bundle.getKeys();
        String prefix = "content." + keyId + ".";
        HashMap<Integer, String> res = new HashMap<>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if (key.startsWith(prefix)) {
                res.put(Integer.parseInt(key.substring(prefix.length())), bundle.getString(key));
            }
        }
        return res;
    }

    /**
     * Retrieves a localized string for a specific category and element ID.
     *
     * @param key The category ID.
     * @param id The unique identifier of the element within the category.
     * @return The localized string.
     */
    @NonNull
    public String getString(@MagicConstant(valuesFromClass = Keys.class) int key, long id) {
        return getString("content." + key + '.' + id);
    }

    /**
     * Checks if a translation exists for the given category and element ID.
     *
     * @param contentType The category ID.
     * @param contentId The element ID.
     * @return {@code true} if a translation is available, {@code false} otherwise.
     */
    public boolean containsContentKey(int contentType, int contentId) {
        return this.containsKey("content." + contentType + '.' + contentId);
    }

    /**
     * Checks if the translation bundle contains a specific key.
     *
     * @param key The property key to check.
     * @return {@code true} if the key exists, {@code false} otherwise.
     */
    public boolean containsKey(@NonNull String key) {
        if (this.bundle != null) {
            try {
                this.bundle.getString(key);
                return true;
            } catch (IllegalFormatException | MissingResourceException ignored) {
            }
        }
        return false;
    }

    /**
     * Retrieves a translated string and optionally formats it with provided arguments.
     * If the key is missing, a placeholder string (e.g., "!key!") is returned.
     *
     * @param key The unique key for the translation property.
     * @param args Arguments used for formatting placeholders in the translated string.
     * @return The formatted localized string.
     */
    @NonNull
    public String getString(@NonNull String key, Object... args) {
        try {
            var format = bundle.getString(key);
            if (args.length != 0) {
                return StringFormatter.format(format, args);
            }
            return format;
        } catch (MissingResourceException e) {
            if (! mocked) log.warn(e.getMessage());
            return "!" + key + "!";
        }
    }

    /**
     * Translation key categories mapping to Wakfu game content types.
     */
    @SuppressWarnings("unused")
    public static final class Keys {
        public static final int SPELL_NAME_TRANSLATION_TYPE = 3;
        public static final int SPELL_BACKGROUND_DESCRIPTION_TRANSLATION_TYPE = 4;
        public static final int SPELL_FREE_DESCRIPTION_TRANSLATION_TYPE = 5;
        public static final int AREA_NAME_TRANSLATION_TYPE = 6;
        public static final int MONSTER_NAME_TRANSLATION_TYPE = 7;
        public static final int STATE_NAME_TRANSLATION_TYPE = 8;
        public static final int STATE_DESCRIPTION_TRANSLATION_TYPE = 9;
        public static final int EFFECT_DESCRIPTION_TRANSLATION_TYPE = 10;
        public static final int RESOURCE_NAME_TRANSLATION_TYPE = 12;
        public static final int EFFECT_COMPLEMENTARY_TEXT_DESCRIPTION_TYPE = 13;
        public static final int ITEM_TYPE_NAME_TRANSLATION_TYPE = 14;
        public static final int ITEM_NAME_TRANSLATION_TYPE = 15;
        public static final int ITEM_DESCRIPTION_TRANSLATION_TYPE = 16;
        public static final int ITEM_SET_NAME_TRANSLATION_TYPE = 20;
        public static final int ITEM_SET_DESCRIPTION_TRANSLATION_TYPE = 21;
        public static final int SKILL_NAME_TRANSLATION_TYPE = 22;
        public static final int SKILL_DESCRIPTION_TRANSLATION_TYPE = 23;
        public static final int CHALLENGE_LONG_DESCRIPTION = 24;
        public static final int CHALLENGE_GOAL_DESCRIPTION = 25;
        public static final int CHALLENGE_TITLE = 26;
        public static final int CHALLENGE_REWARDS_DESCRIPTION = 27;
        public static final int CHALLENGE_DESCRIPTION = 28;
        public static final int CHALLENGE_REQUIREMENTS = 29;
        public static final int EFFECT_IN_CHAT_DESCRIPTION_TYPE = 30;
        public static final int SPELL_BOOSTED_BACKGROUND_DESCRIPTION_TRANSLATION_TYPE = 32;
        public static final int EFFECT_CUSTOM_DESCRIPTION_TRANSLATION_TYPE = 33;
        public static final int PLAYER_TITLE_TRANSLATION_TYPE = 34;
        public static final int LAND_MARK_TRANSLATION_TYPE = 35;
        public static final int ZAAP_NAME_TRANSLATION_TYPE = 36;
        public static final int RESOURCE_TYPE_NAME_TRANSLATION_TYPE = 37;
        public static final int MONSTER_TYPE_NAME_TRANSLATION_TYPE = 38;
        public static final int NATION_NAME_TRANSLATION_TYPE = 39;
        public static final int CRAFT_NAME_TRANSLATION_TYPE = 43;
        public static final int APTITUDE_NAME_TRANSLATION_TYPE = 44;
        public static final int APTITUDE_DESCRIPTION_TRANSLATION_TYPE = 45;
        public static final int BLAHBLAH_MONSTER_SENTENCE_TRANSLATION_TYPE = 47;
        public static final int PROTECTOR_NAME_TRANSLATION_TYPE = 48;
        public static final int PROTECTOR_TALK_TRANSLATION_TYPE = 49;
        public static final int PROTECTOR_BONUS_NAME_TRANSLATION_TYPE = 50;
        public static final int PROTECTOR_BONUS_DESCRIPTION_TRANSLATION_TYPE = 51;
        public static final int CLIMATE_BONUS_NAME_TRANSLATION_TYPE = 52;
        public static final int CLIMATE_BONUS_DESCRIPTION_TRANSLATION_TYPE = 53;
        public static final int AMBIANCE_ZONE_NAME_TRANSLATION_TYPE = 54;
        public static final int TAX_NAME_TRANSLATION_TYPE = 55;
        public static final int NATION_DESCRIPTION_TRANSLATION_TYPE = 56;
        public static final int NATION_RANK_TRANSLATION_TYPE = 57;
        public static final int NATION_RANK_DESCRIPTION_TRANSLATION_TYPE = 40;
        public static final int CRAFT_INTERACTIVE_ELEMENT_NAME = 59;
        public static final int EMOTE_CHAT_DESCRIPTION_TYPE = 60;
        public static final int ACHIEVEMENT_TYPE_NAME_TRANSLATION_TYPE = 61;
        public static final int ACHIEVEMENT_NAME_TRANSLATION_TYPE = 62;
        public static final int ACHIEVEMENT_DESCRIPTION_TRANSLATION_TYPE = 63;
        public static final int ACHIEVEMENT_GOAL_TITLE_TRANSLATION_TYPE = 64;
        public static final int PET_DIALOG_TRANSLATION_TYPE = 65;
        public static final int TERRITORY_NAME_TYPE = 66;
        public static final int BACKGROUND_DISPLAY_TEXT_TRANSLATION_TYPE = 67;
        public static final int MONSTER_BACKGROUND_TRANSLATION_TYPE = 68;
        public static final int MONSTER_BEHAVIOUR_TRANSLATION_TYPE = 69;
        public static final int MONSTER_SPECIAL_TRANSLATION_TYPE = 70;
        public static final int PARAGRAPH_TITLE_TRANSLATION_TYPE = 71;
        public static final int TERRITORY_BACKGROUND_DESCRIPTION_TRANSLATION_TYPE = 72;
        public static final int PROTECTOR_BACKGROUND_DESCRIPTION_TRANSLATION_TYPE = 73;
        public static final int CHALLENGE_PROTECTOR_MESSAGE = 74;
        public static final int INTERACTIVE_DIALOG_QUESTION_TRANSLATION_TYPE = 75;
        public static final int INTERACTIVE_DIALOG_ANSWER_TRANSLATION_TYPE = 76;
        public static final int INSTANCE_NAME_TRANSLATION_TYPE = 77;
        public static final int INTERACTIVE_ELEMENT_BOARD_TRANSLATION_TYPE = 78;
        public static final int INTERACTIVE_ELEMENT_BACKGROUND_DISPLAY_TRANSLATION_TYPE = 79;
        public static final int EMOTE_NAME_TRANSLATION_TYPE = 80;
        public static final int DESTRUCTIBLE_INTERACTIVE_ELEMENT_NAME = 81;
        public static final int IE_GENERIC_NAME = 105;
        public static final int IE_GENERIC_ACTIVABLE_NAME = 106;
        public static final int DRAGO_NAME_TRANSLATION_TYPE = 82;
        public static final int BOAT_NAME_TRANSLATION_TYPE = 83;
        public static final int CANNON_NAME_TRANSLATION_TYPE = 84;
        public static final int LOOT_CHEST_TYPE = 85;
        public static final int COLLECT_MACHINE_TYPE = 86;
        public static final int MARKET_ZONE_NAME = 87;
        public static final int HOUSE_NAME = 88;
        public static final int TELEPORTER_NAME_TRANSLATION_TYPE = 89;
        public static final int PROTECTOR_BOOK_JOB_TYPE = 90;
        public static final int PROTECTOR_BOOK_SEX_TYPE = 91;
        public static final int PROTECTOR_BOOK_HEIGHT_TYPE = 92;
        public static final int PROTECTOR_BOOK_WEIGHT_TYPE = 93;
        public static final int PROTECTOR_BOOK_CUSTOM_DESCRIPTION_TYPE = 94;
        public static final int PROTECTOR_BOOK_SECRET_NAME_TYPE = 95;
        public static final int LAW_NAME = 97;
        public static final int LAW_DESCRIPTION = 98;
        public static final int INTERACTIVE_ELEMENT_VIEW_NAME = 99;
        public static final int RESOURCE_PLANTED_ACTION_NAME = 100;
        public static final int MARKET_INTERACTIVE_ELEMENT_NAME = 101;
        public static final int MARKET_NAME = 102;
        public static final int INTERACTIVE_ELEMENT_TYPE_NAME = 103;
        public static final int STORAGE_BOX_TYPE_NAME = 104;
        public static final int EXCHANGE_MACHINE_NAME = 107;
        public static final int RECYCLE_MACHINE_NAME = 110;
        public static final int RESOURCE_CUTTINGS_TAKEN_ACTION_NAME = 108;
        public static final int RESOURCE_HARVESTED_ACTION_NAME = 109;
        public static final short ARCADE_BONUS_NAME = 112;
        public static final short ARCADE_EVENT_NAME = 113;
        public static final short ARCADE_CHALLENGE_NAME = 114;
        public static final short ARCADE_CHALLENGE_DESCR = 115;
        public static final short ARCADE_DUNGEON_DESCR = 116;
        public static final short ARCADE_DUNGEON_NAME = 117;
        public static final short ARCADE_EVENT_DESCR = 118;
        public static final short IE_REWARD_DISPLAYER_NAME = 119;
        public static final short IE_REWARD_DISPLAYER_DESCR = 120;
        public static final short GAZETTE_TITLE_TYPE = 121;
        public static final short HAVEN_WORLD_HOUSE_CATALOG_NAME = 122;
        public static final short HAVEN_WORLD_HOUSE_CATALOG_DESCR = 123;
        public static final short HAVEN_WORLD_HOUSE_NAME = 126;
        public static final short HAVEN_WORLD_HOUSE_DESCR = 127;
        public static final short HAVEN_WORLD_PATCH_NAME = 124;
        public static final short HAVEN_WORLD_PATCH_DESCR = 125;
        public static final short HAVEN_WORLD_BUILDING_CATALOG_CATEGORY_NAME = 128;
        public static final short HAVEN_WORLD_PATCH_CATALOG_CATEGORY_NAME = 129;
        public static final short HAVEN_WORLD_BUILDING_TYPE_NAME = 134;
        public static final short KROSMASTER_ARENA_FIGURE_NAME = 130;
        public static final short KROSMASTER_ARENA_FIGURE_BG_TITLE = 131;
        public static final short KROSMASTER_ARENA_FIGURE_BG_SHORT = 132;
        public static final short KROSMASTER_ARENA_FIGURE_BG_LONG = 133;
        public static final short DUNGEON_NAME_TYPE_NAME = 137;
        public static final short GUILD_BONUS_NAME = 138;
        public static final short CLIENT_EVENT_CATEGORY = 139;
        public static final short FIGHT_CHALLENGE_NAME = 140;
        public static final short FIGHT_CHALLENGE_DESCRIPTION = 141;
        public static final short FIGHT_CHALLENGE_ADDITIONAL_INFO = 142;
        public static final short SECRET_DESCRIPTION_TRANSLATION_TYPE = 143;
        public static final short PAPERMAP_DECORATION_TRANSLATION_TYPE = 144;
        public static final short PAPERMAP_FULL_TRANSLATION_TYPE = 145;
        public static final short APTITUDE_NEW_NAME_TYPE = 146;
        public static final short APTITUDE_NEW_CATEGORY_TYPE = 147;
        public static final short COMPANY_RANK_NAME_TRANSLATION_TYPE = 148;
        public static final short ACHIEVEMENT_GOAL_DESCRIPTION = 149;
        public static final short BOOKCASE_TRANSLATION_TYPE = 150;
        public static final short ZAAP_CATEGORY_TRANSLATION_TYPE = 151;
    }
}
