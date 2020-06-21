package request.objects;

public enum  TargetType {
    WORD("WORD"),
    MEANING("MEANING");

    private final String type;

    TargetType(final String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }
}
