package common.stores;

import com.mongodb.BasicDBObject;
import common.objects.EntityStatus;

/* MongoQuery knows how for form valid queries*/
public class MongoQuery {

    /*
     *  Helper fields:
     *  Id: used as an identifier of objects stored as a document in a collection
     *  Status: used as an mark the current status of an object in the database, check common.objects.EntityStatus
     */
    public static final String ID_PARAM = "id";
    private static final String STATUS_PARAM = "status";

    public static BasicDBObject getActiveObjectQuery() {
        final BasicDBObject query = new BasicDBObject();
        query.put(STATUS_PARAM, EntityStatus.ACTIVE.toString());
        return query;
    }

    public static BasicDBObject getActiveObjectQuery(String id) {
        final BasicDBObject query = new BasicDBObject();
        query.put(STATUS_PARAM, EntityStatus.ACTIVE.toString());
        query.put(MongoQuery.ID_PARAM, id);
        return query;
    }
}
