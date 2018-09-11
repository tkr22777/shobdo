package objects;

//Shobdo Object/Entity Status
public enum  EntityStatus {

    ACTIVE("ACTIVE"),
    DEACTIVE("DEACTIVE");

    private final String status;

    EntityStatus(final String status)  {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
