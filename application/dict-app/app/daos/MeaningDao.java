package daos;

import objects.Meaning;
import java.util.ArrayList;

public interface MeaningDao {

    Meaning create(Meaning meaning);

    Meaning get(String id);

    Meaning update(Meaning meaning);

    Meaning delete(String id);

    ArrayList<Meaning> list(String startId, int limit);

    long totalCount();
}