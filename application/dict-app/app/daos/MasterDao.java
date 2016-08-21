package daos;

/**
 * Created by tahsinkabir on 8/21/16.
 */
public interface MasterDao {

    public boolean createDatabases();

    public boolean clearDatabase();

    public boolean saveCurrentDatabase();

    public boolean loadDatabase();

}
