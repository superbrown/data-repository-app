package gov.energy.nrel.dataRepositoryApp.bo.mongodb.abandonedApproaches.singleRowSchemaApproach;

import gov.energy.nrel.dataRepositoryApp.DataRepositoryApplication;
import gov.energy.nrel.dataRepositoryApp.bo.IBusinessObjects;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.AbsDatasetBOTest;
import gov.energy.nrel.dataRepositoryApp.bo.mongodb.singleCellSchemaApproach.r_BusinessObjects;


public class r_DatasetBOTest extends AbsDatasetBOTest
{
    @Override
    protected IBusinessObjects createBusinessObjects(DataRepositoryApplication dataRepositoryApplication) {

        return new r_BusinessObjects(dataRepositoryApplication);
    }

}
