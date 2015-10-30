package gov.energy.nbc.car.businessObject;

import gov.energy.nbc.car.Settings;
import gov.energy.nbc.car.businessObject.dto.FileAsRawBytes;
import gov.energy.nbc.car.businessObject.dto.StoredFile;
import gov.energy.nbc.car.dao.DataFileDAO;
import gov.energy.nbc.car.dao.UnableToDeleteFile;
import gov.energy.nbc.car.fileReader.FileReader;
import org.springframework.web.multipart.MultipartFile;

public class DataFileBO {

    protected DataFileDAO dataFileDAO;
    protected DataFileDAO dataFileDAO_FOR_UNIT_TESTING_PURPOSES;

    protected FileReader fileReader;

    public DataFileBO(Settings settings,
                      Settings settings_forUnitTestingPurposes) {

        dataFileDAO = new DataFileDAO(settings);
        dataFileDAO_FOR_UNIT_TESTING_PURPOSES = new DataFileDAO(settings_forUnitTestingPurposes);

        fileReader = new FileReader();
    }

    public StoredFile saveFile(TestMode testMode, MultipartFile dataFile) {

        StoredFile theDataFileThatWasStored = null;
        try {
            theDataFileThatWasStored = getDataFileDAO(testMode).saveFile(dataFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return theDataFileThatWasStored;
    }

    public StoredFile saveFile(TestMode testMode, FileAsRawBytes file) {

        StoredFile theDataFileThatWasStored = null;
        try {
            theDataFileThatWasStored = getDataFileDAO(testMode).saveFile(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return theDataFileThatWasStored;
    }


    public DataFileDAO getDataFileDAO(TestMode testMode) {

        if (testMode == TestMode.NOT_TEST_MODE) {
            return dataFileDAO;
        }
        else {
            return dataFileDAO_FOR_UNIT_TESTING_PURPOSES;
        }
    }

    public void deletFile(String storageLocation)
            throws UnableToDeleteFile {

        dataFileDAO.deletFile(storageLocation);
    }
}