package objects;

//REQUEST OPERATION TYPES
public enum RequestOperation {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    public String getOperation() {
        return operation;
    }

    private final String operation;

    RequestOperation(final String operation)  {
        this.operation = operation;
    }
}
