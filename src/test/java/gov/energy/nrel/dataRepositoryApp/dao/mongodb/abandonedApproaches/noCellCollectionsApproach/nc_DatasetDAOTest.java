package gov.energy.nrel.dataRepositoryApp.dao.mongodb.abandonedApproaches.noCellCollectionsApproach;

import gov.energy.nrel.dataRepositoryApp.DataRepositoryApplication;
import gov.energy.nrel.dataRepositoryApp.bo.IBusinessObjectsInventory;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.abandonedApproaches.noCellCollectionApproach.nc_BusinessObjectsInventory;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.AbsDatasetDAOTest;

public class nc_DatasetDAOTest extends AbsDatasetDAOTest{

    @Override
    protected IBusinessObjectsInventory createBusinessObjects(DataRepositoryApplication dataRepositoryApplication) {

        return new nc_BusinessObjectsInventory(dataRepositoryApplication);
    }
}
