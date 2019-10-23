package objects;

/**
 * Created by Tahsin Kabir on 11/24/16.
 */
public final class Constants {

    private Constants() {}

    public static final int SEARCH_SPELLING_LIMIT = 40;

    public static final String WORD_ID_PREFIX = "WD";
    public static final String MEANING_ID_PREFIX = "MNG";
    public static final String REQUEST_ID_PREFIX = "REQ";

    //Meta constant keys
    public static final String MEANING_STRING = "MEANING STRING";
    public static final String ORIGINAL_STRING = "ORIGINAL STRING";
    public static final String ENG_PRONUN_STRING = "ENG PRONUNCIATION";

    //REST REQUEST FIELD NAMES
    public static final String SPELLING_KEY = "spelling";
    public static final String SEARCH_STRING_KEY = "searchString";
    public static final String WORD_COUNT_KEY = "wordCount";

    public static final String REQUEST_MERGED = "Requests Merged";

    //Messages
    public static final String ENTITY_IS_DELETED= "Entity has been deleted.";
    public static final String ID_NULLOREMPTY = "Id is null/empty/mismatch:";
    public static final String SPELLING_NULLOREMPTY = "spelling is null/empty.";
    public static final String MEANING_NULLOREMPTY = "Meaning string is null/empty.";
    public static final String CREATE_SPELLING_EXISTS = "spelling exists:%s";
    public static final String MEANING_PROVIDED = "Mutating meaning not allowed with this route";
    public static final String ENTITY_LOCKED = "Entity is currently locked:";

    public static class Messages {
        public static String UserProvidedIdForbidden(final String id) {
            return String.format("User provided id is not permissible for create:%s", id);
        }

        public static String spellingExists(final String spelling) {
            return String.format("Word with the spelling: %s already exist", spelling);
        }

        public static String EntityNotFound(final String entityId) {
            return String.format("Entity not found for id:%s", entityId);
        }
    }
}
