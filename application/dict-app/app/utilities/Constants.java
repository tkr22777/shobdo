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

    //REST REQUEST FIELD NAMES
    public static final String WORD_SPELLING_KEY = "wordSpelling";
    public static final String SEARCH_STRING_KEY = "searchString";
    public static final String WORD_COUNT_KEY = "wordCount";

    //Messages
    public static final String ENTITY_NOT_FOUND = "Entity not found for:";
    public static final String ENTITY_IS_DEACTIVE = "Entity deactive:";
    public static final String ENTITY_LOCKED = "Entity is currently locked:";
    public static final String ID_NULLOREMPTY = "Id is null/empty/mismatch:";
    public static final String WORDSPELLING_NULLOREMPTY = "WordSpelling is null/empty.";
    public static final String MEANING_NULLOREMPTY = "Meaning string is null/empty.";
    public static final String CREATE_ID_NOT_PERMITTED = "User provided id is not permissible for create:";
    public static final String CREATE_SPELLING_EXISTS = "WordSpelling exists:";
    public static final String MEANING_PROVIDED = "Mutating meaning not allowed with this route";

}
