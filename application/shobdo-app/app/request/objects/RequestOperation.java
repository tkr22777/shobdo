package request.objects;

//REQUEST OPERATION TYPES
public enum RequestOperation {
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String operation;

    public String getOperation() {
        return operation;
    }

    RequestOperation(final String operation)  {
        this.operation = operation;
    }
}
