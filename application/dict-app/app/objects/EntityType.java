package objects;

//Entity Types
public enum EntityType {

    //ENTITY TYPES, For type on version meta
    WORD("WORD"),
    MEANING("MEANING"),
    REQUEST("REQUEST"),
    UNKNOWN("UNKNOWN");

    private final String type;

    EntityType(final String type)  {
        this.type = type;
    }
}
