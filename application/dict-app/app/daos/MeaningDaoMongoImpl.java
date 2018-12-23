package daos;

import objects.Meaning;

public class MeaningDaoMongoImpl implements MeaningDao {

    private static final String DB_NAME = "Dictionary";
    private static final String COLLECTION_NAME = "Meanings";

    @Override
    public Meaning create(Meaning meaning) {
        return null;
    }

    @Override
    public Meaning get(String id) {
        return null;
    }

    @Override
    public Meaning update(Meaning meaning) {
        return null;
    }

    @Override
    public Meaning delete(String id) {
        return null;
    }

    @Override
    public long totalCount() {
        return 0;
    }

    @Override
    public void deleteAll() {

    }
}
