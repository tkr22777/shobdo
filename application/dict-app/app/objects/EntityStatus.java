package objects;

//Object/Entity Status
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
