package objects;

public enum  EntityStatus {
    ACTIVE("ACTIVE"),
    DELETED("DELETED");

    private final String status;

    EntityStatus(final String status)  {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
