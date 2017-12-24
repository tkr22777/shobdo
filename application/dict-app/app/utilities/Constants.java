package utilities;

/**
 * Created by tahsinkabir on 11/24/16.
 */
public class Constants {

    public static final String MONGODB_HOSTNAME_CONFIG_STRING = "shobdo.mongodbhostname";
    public static final String MONGODB_PORT_CONFIG_STRING = "shobdo.mongodbport";
    public static final String DICTIONARY_DATABASE_NAME = "Dictionary";
    public static final String WORD_COLLECTION_NAME = "Words";

    public static final String REDIS_HOSTNAME_CONFIG_STRING = "shobdo.redishostname";

    public static final int SEARCH_SPELLING_LIMIT = 40;

    public static final String WORD_ID_PREFIX = "WD";
    public static final String MEANING_ID_PREFIX = "MNG";
    public static final String REQ_ID_PREFIX = "REQ";

    //Meta constant keys
    public static final String MEANING_STRING = "MEANING STRING";
    public static final String ORIGINAL_STRING = "ORIGINAL STRING";
    public static final String ENG_PRONUN_STRING = "ENG PRONUNCIATION";

    //Versioning
    public static final String ENTITIY_ACTIVE = "ACTIVE";
    public static final String ENTITIY_UPDATED = "UPDATED";
    public static final String ENTITIY_DELETED = "DELETED";
    //REQUESTED
    public static final String ENTITIY_CREATE_REQUESTED = "CREATE_REQUESTED";
    public static final String ENTITIY_UPDATE_REQUESTED = "UPDATE_REQUESTED";
    public static final String ENTITIY_DELETE_REQUESTED = "DELETED_REQUESTED";

    //REST REQUEST CONSTANTS
    public static final String WORD_SPELLING_KEY = "wordSpelling";
    public static final String SEARCH_STRING_KEY = "searchString";
    public static final String WORD_COUNT_KEY = "wordCount";

    //Messages
    public static final String GET_WORD_NOT_FOUND = "No word found for wordId/spelling:";
    //Error Messages
    public static final String CREATE_WORDID_EXISTS = "WordID exists:";
    public static final String CREATE_SPELLING_NOT_PROVIDED = "WordSpelling was not provided.";
    public static final String CREATE_SPELLING_EXISTS = "WordSpelling exists:";
    public static final String CREATE_MEANING_PROVIDED = "Word creation along with meaning is not supported";
    public static final String GET_WORDID_NULLEMPTY = "Provided wordId is null/empty:";

}
