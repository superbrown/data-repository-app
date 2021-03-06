package gov.energy.nrel.dataRepositoryApp.dao.mongodb;

import gov.energy.nrel.dataRepositoryApp.bo.ResultsMode;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.TestData;
import gov.energy.nrel.dataRepositoryApp.dao.IRowDAO;
import gov.energy.nrel.dataRepositoryApp.dao.dto.SearchCriterion;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Metadata;
import gov.energy.nrel.dataRepositoryApp.model.document.mongodb.DatasetDocument;
import gov.energy.nrel.dataRepositoryApp.model.document.mongodb.RowDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.*;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static gov.energy.nrel.dataRepositoryApp.dao.dto.ComparisonOperator.EQUALS;
import static org.junit.Assert.assertTrue;


public abstract class AbsRowDAOTest extends TestUsingTestData
{
    private static IRowDAO rowDAO;

    @BeforeClass
    public static void beforeClass() {

        TestUsingTestData.beforeClass();
    }

    @AfterClass
    public static void afterClass() {
        TestUsingTestData.afterClass();
    }

    @Before
    public void before() {

        super.before();

        rowDAO = getBusinessObjects().getRowBO().getRowDAO();
    }

    @After
    public void after() {
        super.after();
    }

    @Test
    public void testThatQueriesWorkAsExpectedOnColumnsWithHeterogenousDataTypes() {

        Bson filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name"),
                TestData.date_1);
        List<Document> results = rowDAO.get(filter);
        assertTrue(results.size() == 1);

        filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name"),
                "1000");
        results = rowDAO.get(filter);
        assertTrue(results.size() == 1);

        filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name"),
                1000);
        results = rowDAO.get(filter);
        assertTrue(results.size() == 1);

        filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name"),
                1000.00);
        results = rowDAO.get(filter);
        assertTrue(results.size() == 1);

        filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name"),
                1300.54);
        results = rowDAO.get(filter);
        assertTrue(results.size() == 1);

        filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "Varying Value Types Column Name")
                , "string value");
        results = rowDAO.get(filter);
        assertTrue(results.size() == 1);
    }

    @Test
    public void testQueryForOneFilter_1() {

        Document idFilter = new Document().append(RowDocument.MONGO_KEY__DATASET_ID, TestData.dataset_1_objectId);
        List<Document> documents = rowDAO.get(idFilter);
        assertTrue(documents != null);
        assertTrue(documents.size() == 5);

        for (Document document : documents) {
            assertTrue(document.get(RowDocument.MONGO_KEY__DATASET_ID).equals(TestData.dataset_1_objectId));
        }
    }

    @Test
    public void testQueryForOneFilter_2() {

        Bson filter = eq(RowDocument.MONGO_KEY__DATA + "." +
                MongoFieldNameEncoder.toMongoSafeFieldName(
                        "String Values Column Name"),
                "String 1");
        List<Document> results = rowDAO.get(filter);
        assertTrue(results.size() == 1);
    }

    @Test
    public void testQueryForOneFilter_3() {

        Bson filter = and(
                eq(DatasetDocument.MONGO_KEY__METADATA + "." + Metadata.MONGO_KEY__DATA_CATEGORY, TestData.dataCategory),
                eq(RowDocument.MONGO_KEY__DATA + "." +
                        MongoFieldNameEncoder.toMongoSafeFieldName(
                                "Date Values Column Name"),
                        TestData.date_2)
        );

        Document fieldsToInclude = new Document().append(DatasetDocument.MONGO_KEY__METADATA, 1);
        List<Document> results = rowDAO.get(filter);
        assertTrue(results.size() == 2);
    }

    @Test
    public void testThatTheRightNumberOfSpreasheetRowDocumentsExist() {

        int numberOfRowsThatShouldExist =
                (TestData.rowCollection_1.getRows().size()) +
                        (TestData.rowCollection_2.getRows().size());

        long numberOfRowsThatActuallyExist =
                ((IMongodbDAO)rowDAO).getCollection().count();

        assertTrue(numberOfRowsThatActuallyExist == numberOfRowsThatShouldExist);
    }

    @Test
    public void testQuery_rowSearchCriteria() {

        List<SearchCriterion> rowSearchCriteria = new ArrayList<>();
        rowSearchCriteria.add(new SearchCriterion("Some Column Name", 1, EQUALS));
        rowSearchCriteria.add(new SearchCriterion("Float Values Column Name", 1.22, EQUALS));
        rowSearchCriteria.add(new SearchCriterion("Additional Column Name 1", "a1", EQUALS));

        List<Document> documents = rowDAO.query(rowSearchCriteria, ResultsMode.INCLUDE_ONLY_DATA_COLUMNS_BEING_FILTERED_UPON);
        assertTrue(documents.size() == 1);

        Document document = documents.get(0);
        Object data = document.get(RowDocument.MONGO_KEY__DATA);

        if (data instanceof Document) {
            Document dataDocument = (Document) data;
            assertTrue(dataDocument.keySet().size() == 4);
            assertTrue(((Integer)dataDocument.get(" Row")) == 0);
            assertTrue(((Integer)dataDocument.get("Some Column Name")) == 1);
            assertTrue(((Double)dataDocument.get("Float Values Column Name")) == 1.22);
            assertTrue(dataDocument.get("Additional Column Name 1").equals("a1"));
        }
    }
}
