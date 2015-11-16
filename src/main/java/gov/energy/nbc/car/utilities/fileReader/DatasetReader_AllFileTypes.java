package gov.energy.nbc.car.utilities.fileReader;

import gov.energy.nbc.car.utilities.fileReader.dto.RowCollection;
import gov.energy.nbc.car.utilities.fileReader.exception.InvalidValueFoundInHeader;
import gov.energy.nbc.car.utilities.fileReader.exception.UnsupportedFileExtension;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class DatasetReader_AllFileTypes extends AbsDatasetReader implements IDatasetReader_AllFileTypes {

    Logger log = Logger.getLogger(this.getClass());

    public IDatasetReader_ExcelWorkbook excelWorkbookReader;
    public IDatasetReader_CSVFile csvFileReader;

    public DatasetReader_AllFileTypes() {

        this.excelWorkbookReader = new DatasetReader_ExcelWorkbook();
        this.csvFileReader = new DatasetReader_CSVFile();
    }


    @Override
    public RowCollection extractDataFromFile(File file, String nameOfWorksheetContainingTheData, int maxNumberOfValuesPerRow)
            throws UnsupportedFileExtension, InvalidValueFoundInHeader {

        RowCollection rowCollection = null;

        try {
            if (excelWorkbookReader.canReadFile(file)) {

                rowCollection = extractDataFromDataset(file, nameOfWorksheetContainingTheData);
            }
            else if (csvFileReader.canReadFile(file)) {

                rowCollection = extractDataFromCSVFile(file, maxNumberOfValuesPerRow);
            }
            else {
                new UnsupportedFileExtension(file.getName());
            }
        }
        catch (IOException e) {
            // FIXME: Log
            log.error(e);
            throw new RuntimeException(e);
        }

        return rowCollection;
    }

    @Override
    public RowCollection extractDataFromDataset(File file, String nameOfWorksheetContainingTheData)
            throws UnsupportedFileExtension, IOException, InvalidValueFoundInHeader {

        RowCollection dataUpload =
                excelWorkbookReader.extractDataFromFile(file, nameOfWorksheetContainingTheData);

        return dataUpload;
    }

    public RowCollection extractDataFromCSVFile(File file, int maxNumberOfValuesPerRow)
            throws UnsupportedFileExtension, IOException, InvalidValueFoundInHeader {

        RowCollection dataUpload = csvFileReader.extractDataFromFile(file, maxNumberOfValuesPerRow);

        return dataUpload;
    }

    @Override
    public boolean isAnExcelFile(String filename) {

        return excelWorkbookReader.canReadFileWithExtension(filename);
    }

    @Override
    public boolean canReadFile(File file) {

        return excelWorkbookReader.canReadFile(file) ||
                csvFileReader.canReadFile(file);
    }

    @Override
    public boolean canReadFileWithExtension(String filename) {

        return excelWorkbookReader.canReadFileWithExtension(filename) ||
                csvFileReader.canReadFileWithExtension(filename);
    }
}
