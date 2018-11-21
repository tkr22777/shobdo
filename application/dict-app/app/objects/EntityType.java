package objects;

//Entity types on version meta
public enum EntityType {
    WORD("WORD"),
    MEANING("MEANING"),
    REQUEST("REQUEST");

    private final String type;

    EntityType(final String type)  {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
