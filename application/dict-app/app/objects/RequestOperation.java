package objects;

public enum RequestOperation {

    //REQUEST OPERATION TYPES
    CREATE("CREATE"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String operation;

    RequestOperation(final String operation)  {
        this.operation = operation;
    }
}
