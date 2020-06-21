package common.objects;

public enum EntityStatus {
    ACTIVE("ACTIVE"),
    DELETED("DELETED");

    private final String status;

    public String getStatus() {
        return status;
    }

    EntityStatus(final String status)  {
        this.status = status;
    }
}
