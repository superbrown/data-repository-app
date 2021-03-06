package gov.energy.nrel.dataRepositoryApp.bo.mongodb.singleCellCollectionApproach;

import com.mongodb.client.MongoDatabase;
import gov.energy.nrel.dataRepositoryApp.DataRepositoryApplication;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.AbsBO;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.TestData;
import gov.energy.nrel.dataRepositoryApp.dao.IDatasetDAO;
import gov.energy.nrel.dataRepositoryApp.dao.exception.CompletelyFailedToPersistDataset;
import gov.energy.nrel.dataRepositoryApp.dao.exception.PartiallyFailedToPersistDataset;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.DAOUtilities;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.IMongodbDAO;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.singleCellCollectionApproach.sc_DatasetDAO;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class sc_TestDataBO extends AbsBO implements gov.energy.nrel.dataRepositoryApp.bo.ITestDataBO {

    public IDatasetDAO datasetDAO;

    public sc_TestDataBO(DataRepositoryApplication dataRepositoryApplication) {
        super(dataRepositoryApplication);
    }

    @Override
    protected void init() {
        datasetDAO = new sc_DatasetDAO(getSettings());
    }

    @Override
    public String seedTestDataInTheDatabase_dataset_1_and_2()
            throws PartiallyFailedToPersistDataset, CompletelyFailedToPersistDataset {

        TestData.dataset_1_objectId = datasetDAO.add(TestData.dataset_1, TestData.rowCollection_1);
        TestData.dataset_2_objectId = datasetDAO.add(TestData.dataset_2, TestData.rowCollection_2);

        List<ObjectId> newObjects = new ArrayList<>();
        newObjects.add(TestData.dataset_1_objectId);
        newObjects.add(TestData.dataset_2_objectId);

        Document document = new Document().
                append("datasetIDs", newObjects);

        return DAOUtilities.serialize(document);
    }

    @Override
    public String seedTestDataInTheDatabase_dataset_1()
            throws PartiallyFailedToPersistDataset, CompletelyFailedToPersistDataset {

        TestData.dataset_1_objectId = datasetDAO.add(TestData.dataset_1, TestData.rowCollection_1);

        List<ObjectId> newObjects = new ArrayList<>();
        newObjects.add(TestData.dataset_1_objectId);

        Document document = new Document().
                append("datasetIDs", newObjects);

        return DAOUtilities.serialize(document);
    }

    @Override
    public String seedTestDataInTheDatabase_dataset_2()
            throws PartiallyFailedToPersistDataset, CompletelyFailedToPersistDataset {

        TestData.dataset_2_objectId = datasetDAO.add(TestData.dataset_2, TestData.rowCollection_2);

        List<ObjectId> newObjects = new ArrayList<>();
        newObjects.add(TestData.dataset_2_objectId);

        Document document = new Document().append("datasetIDs", newObjects);

        return DAOUtilities.serialize(document);
    }

    @Override
    public void removeTestData() {

        ((IMongodbDAO)datasetDAO).getMongoClient().dropDatabase(getSettings().getMongoDatabaseName());
    }

    @Override
    public void dropTheTestDatabase() {

        MongoDatabase database = ((IMongodbDAO)datasetDAO).getDatabase();
        database.drop();
    }
}
