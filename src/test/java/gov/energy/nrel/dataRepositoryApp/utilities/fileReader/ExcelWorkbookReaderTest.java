package gov.energy.nrel.dataRepositoryApp.utilities.fileReader;

import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Row;
import gov.energy.nrel.dataRepositoryApp.utilities.valueSanitizer.IValueSanitizer;
import gov.energy.nrel.dataRepositoryApp.utilities.Utilities;
import gov.energy.nrel.dataRepositoryApp.utilities.valueSanitizer.ValueSanitizer_usingOwaspJavaHtmlSanitizer;
import gov.energy.nrel.dataRepositoryApp.utilities.fileReader.dto.RowCollection;
import gov.energy.nrel.dataRepositoryApp.utilities.fileReader.exception.FileContainsInvalidColumnName;
import gov.energy.nrel.dataRepositoryApp.utilities.fileReader.exception.NotAnExcelWorkbook;
import gov.energy.nrel.dataRepositoryApp.utilities.fileReader.exception.UnsupportedFileExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;


public class ExcelWorkbookReaderTest {

    protected static IDatasetReader_ExcelWorkbook excelWorkbookReader;

    @BeforeClass
    public static void beforeClass() {

        IValueSanitizer valueSanitizer = new ValueSanitizer_usingOwaspJavaHtmlSanitizer();
        excelWorkbookReader = new DatasetReader_ExcelWorkbook(valueSanitizer);
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: extractDataFromDataset(File file, String nameOfSubdocumentContainingDataIfApplicable)
     */
    @Test
    public void testExtractDataFromDataset_xls() throws Exception {

        try {
            File file = Utilities.getFile("/testData/active/SpreadsheetForReadingTest.xls");
            testExtractDataFromDataset(file);
        }
        catch (Throwable fileContainsInvalidColumnName) {
            fail();
        }
    }

    @Test
    public void testExtractDataFromDataset_xlm() throws Exception {

        try {
            File file = Utilities.getFile("/testData/active/SpreadsheetForReadingTest.xlsm");
            testExtractDataFromDataset(file);
        }
        catch (Throwable e) {
            fail();
        }
    }

    @Test
    public void testExtractDataFromDataset_xlsx() throws Exception {

        try {
            File file = Utilities.getFile("/testData/active/SpreadsheetForReadingTest.xlsx");
            testExtractDataFromDataset(file);
        }
        catch (Throwable e) {
            fail();
        }
    }

    private void testExtractDataFromDataset(File file)
            throws IOException, FileContainsInvalidColumnName, UnsupportedFileExtension, ParseException, NotAnExcelWorkbook, UnsanitaryData {

        RowCollection rowCollection = excelWorkbookReader.extractDataFromFile(file, "Sheet Containing Data");

        assertTrue(rowCollection.columnNames.size() == 8);
        assertTrue(rowCollection.columnNames.get(0).equals(Row.MONGO_KEY__ROW_NUMBER));
        assertTrue(rowCollection.columnNames.get(1).equals("Column 1"));
        assertTrue(rowCollection.columnNames.get(2).equals("1.5"));
        assertTrue(rowCollection.columnNames.get(3).equals("Column 2"));
        assertTrue(rowCollection.columnNames.get(4).equals("Column 3"));
        assertTrue(rowCollection.columnNames.get(5).equals("Column 4"));
        assertTrue(rowCollection.columnNames.get(6).equals("Column 5"));
        assertTrue(rowCollection.columnNames.get(7).equals("Column 6"));

        assertTrue(rowCollection.rowData.size() == 5);

        List row_1 = rowCollection.rowData.get(0);
        assertTrue(row_1.size() == 8);
        assertTrue(row_1.get(1).equals(1.0));
        assertTrue(row_1.get(2).equals(true));
        assertTrue(row_1.get(3).equals("one"));
        assertTrue(row_1.get(4).equals(toDate("01/01/2015")));
        assertTrue(row_1.get(5) == null);
        assertTrue(row_1.get(6).equals("one"));
        assertTrue(row_1.get(7) == null);

        List row_2 = rowCollection.rowData.get(1);
        assertTrue(row_2.size() == 8);
        assertTrue(row_2.get(1) == null);
        assertTrue(row_2.get(2).equals(false));
        assertTrue(row_2.get(3).equals("two"));
        assertTrue(row_2.get(4).equals(toDate("01/02/2015")));
        assertTrue(row_2.get(5) == null);
        assertTrue(row_2.get(6).equals(2.0));
        assertTrue(row_2.get(7) == null);

        List row_3 = rowCollection.rowData.get(2);
        assertTrue(row_3.size() == 8);
        assertTrue(row_3.get(1).equals(3.0));
        assertTrue(row_3.get(2).equals(false));
        assertTrue(row_3.get(3) == null);
        assertTrue(row_3.get(4).equals(toDate("01/03/2015")));
        assertTrue(row_3.get(5) == null);
        assertTrue(row_3.get(6).equals(3.345));
        assertTrue(row_3.get(7) == null);

        List row_4 = rowCollection.rowData.get(3);
        assertTrue(row_4.size() == 8);
        assertTrue(row_4.get(1).equals(4.0));
        assertTrue(row_4.get(2).equals(true));
        assertTrue(row_4.get(3).equals("four"));
        assertTrue(row_4.get(4) == null);
        assertTrue(row_4.get(5) == null);
        assertTrue(row_4.get(6).equals("four"));
        assertTrue(row_4.get(7).equals("hello 1"));

        List row_5 = rowCollection.rowData.get(4);
        assertTrue(row_5.size() == 8);
        assertTrue(row_5.get(1).equals(5.0));
        assertTrue(row_5.get(2).equals(true));
        assertTrue(row_5.get(3).equals("five"));
        assertTrue(row_5.get(4).equals(toDate("01/05/2015")));
        assertTrue(row_5.get(5) == null);
        assertTrue(row_5.get(6).equals("five"));
        assertTrue(row_5.get(7) == null);
    }

    private Date toDate(String dateAsString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.parse(dateAsString);
    }

}
