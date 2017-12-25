package objects;

import lombok.Data;

@Data
public class VersionMeta {

    //private String id;
    private String status = SOStatus.ACTIVE;
    private String type = SOTypes.UNKNOWN;
    private String parentId; //null for pioneer object
    private String creatorId; //creator is the deleter of the parent if parentId is present
    private String creationDate;
    private String deactivationDate;

    //V1.5 validation of updates
    private String validatorId; //if validatorId is present, then the object has been validated
    private int version = 0;

    public VersionMeta() { }
}
