package utilities;

public final class Constants {

    private Constants() {}

    public static final int SEARCH_SPELLING_LIMIT = 40;

    public static final String PREFIX_WORD_ID = "WD";
    public static final String PREFIX_MEANING_ID = "MNG";
    public static final String PREFIX_REQUEST_ID = "REQ";

    //REST REQUEST FIELD NAMES
    public static final String KEY_SPELLING = "spelling";
    public static final String KEY_STRENGTH = "strength";
    public static final String KEY_SEARCH_STRING = "searchString";
    public static final String KEY_WORD_COUNT = "wordCount";

    //Messages
    public static final String MESSAGES_DELETED_ENTITY = "Entity has been deleted.";
    public static final String MESSAGES_ID_NULLOREMPTY      = "Id is null/empty/mismatch:";
    public static final String MESSAGES_SPELLING_NULLOREMPTY = "spelling is null/empty.";
    public static final String MESSAGES_MEANING_NULLOREMPTY = "Meaning string is null/empty.";
    public static final String MESSAGES_SPELLING_EXISTS = "spelling exists:%s";
    public static final String MESSAGES_MEANING_PROVIDED = "Mutating meaning not allowed with this route";
    public static final String MESSAGES_REQUEST_MERGED = "Request Merged";

    public static class Messages {
        public static String UserProvidedIdForbidden(final String id) {
            return String.format("User provided id is not permissible for create:%s", id);
        }

        public static String SpellingExists(final String spelling) {
            return String.format("Word with the spelling: %s already exist", spelling);
        }

        public static String EntityNotFound(final String entityId) {
            return String.format("Entity not found for id:%s", entityId);
        }
    }
}
