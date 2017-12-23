package objects;

import lombok.Data;
import utilities.Constants;

@Data
public class VersionMeta {

    private String status = Constants.ENTITIY_ACTIVE;
    private String parentId; //null for pioneer object
    private String creatorId; //creator is the deleter of the parent if parentId is present
    private String creationDate; //creationDate is the deletion time of the parent if parentId is present

    //V1.5 validation of updates
    private String validatorId; //if validatorId is present, then the object has been validated
    private int version = 0;
}
