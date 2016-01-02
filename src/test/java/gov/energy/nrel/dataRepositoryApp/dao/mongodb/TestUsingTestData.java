package gov.energy.nrel.dataRepositoryApp.dao.mongodb;

import gov.energy.nrel.dataRepositoryApp.app.DataRepositoryApplication;
import gov.energy.nrel.dataRepositoryApp.bo.IBusinessObjects;
import gov.energy.nrel.dataRepositoryApp.bo.ITestDataBO;
import gov.energy.nrel.dataRepositoryApp.settings.Settings;

public abstract class TestUsingTestData {

    static public boolean SUSPEND_DATA_SEEDING = false;
    static public boolean SUSPEND_DATA_CLEANUP = false;

    public static final String[] DEFAULT_SET_OF_DATA_CATEGORIES = new String[] {"Algae", "ATP3", "Biomass"};

    private DataRepositoryApplication dataRepositoryApplication;

    protected abstract DataRepositoryApplication createAppSingleton(Settings settings);
    {
        Settings settings = new Settings();

        settings.setMongoDbHost("localhost");
        settings.setMongoDbPort("27017");
        settings.setMongoDatabaseName("car_s");
        settings.setRootDirectoryForUploadedDataFiles("target/test-classes");
        settings.setDefaultSetOfDataCategories(DEFAULT_SET_OF_DATA_CATEGORIES);
        settings.setPerformanceLoggingEnabled(false);

        dataRepositoryApplication = createAppSingleton(settings);
    }

    public TestUsingTestData() {
    }

    public DataRepositoryApplication getDataRepositoryApplication() {
        return dataRepositoryApplication;
    }

    public static void beforeClass() {
    }

    public void before() {

        ITestDataBO testDataBO = getBusinessObjects().getTestDataBO();

        // (just in case it's necessary)
        if (SUSPEND_DATA_CLEANUP == false) {
            testDataBO.removeTestData();
        }

        if (SUSPEND_DATA_SEEDING == false) {
            testDataBO.seedTestDataInTheDatabase_dataset_1_and_2();
        }
    }

    protected IBusinessObjects getBusinessObjects() {

        return getDataRepositoryApplication().getBusinessObjects();
    }

    public void after() {

        if (SUSPEND_DATA_CLEANUP == false) {
            ITestDataBO testDataBO = getBusinessObjects().getTestDataBO();
            testDataBO.dropTheTestDatabase();
        }
    }

    public static void afterClass() {

    }
}