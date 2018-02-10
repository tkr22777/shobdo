package objects;

//Shobdo Object/Entity Status
public enum  EntityStatus {

    ACTIVE("ACTIVE"),
    LOCKED("LOCKED"),
    UPDATED("UPDATED"),
    MERGED("MERGED");

    private final String status;

    EntityStatus(final String status)  {
        this.status = status;
    }

}
