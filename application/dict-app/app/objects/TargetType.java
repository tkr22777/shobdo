package objects;

public enum  TargetType {
    WORD("Word"),
    MEANING("MEANING");

    private final String type;

    TargetType(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
