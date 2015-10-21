package gov.energy.nbc.car.fileReader;

import gov.energy.nbc.car.utilities.SpreadsheetData;
import gov.energy.nbc.car.utilities.Utilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;


public class CSVFileReaderTest {

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @Test
    public void testExtractDataFromCSVFile_1() throws Exception {

        try {
            File file = Utilities.getFile("/SpreadsheetForReadingTest_1.csv");

            SpreadsheetData spreadsheetData = CSVFileReader.extractDataFromFile(file);

            assertTrue(spreadsheetData.columnNames.size() == 6);
            assertTrue(spreadsheetData.columnNames.get(0).equals("Column 1"));
            assertTrue(spreadsheetData.columnNames.get(1).equals("Column 2"));
            assertTrue(spreadsheetData.columnNames.get(2).equals("Column 3"));
            assertTrue(spreadsheetData.columnNames.get(3).equals("Column 4"));
            assertTrue(spreadsheetData.columnNames.get(4).equals("Column 5"));
            assertTrue(spreadsheetData.columnNames.get(5).equals("Column 6"));

            assertTrue(spreadsheetData.spreadsheetData.size() == 5);

            List row_1 = spreadsheetData.spreadsheetData.get(0);
            assertTrue(row_1.size() == 6);
            assertTrue(row_1.get(0).equals(1.0));
            assertTrue(row_1.get(1).equals("one"));
            assertTrue(row_1.get(2).equals(toDate("01/01/2015")));
            assertTrue(row_1.get(3) == null);
            assertTrue(row_1.get(4).equals("one"));
            assertTrue(row_1.get(5) == null);

            List row_2 = spreadsheetData.spreadsheetData.get(1);
            assertTrue(row_2.size() == 6);
            assertTrue(row_2.get(0) == null);
            assertTrue(row_2.get(1).equals("two"));
            assertTrue(row_2.get(2).equals(toDate("01/02/2015")));
            assertTrue(row_2.get(3) == null);
            assertTrue(row_2.get(4).equals(2.0));
            assertTrue(row_2.get(5) == null);

            List row_3 = spreadsheetData.spreadsheetData.get(2);
            assertTrue(row_3.size() == 6);
            assertTrue(row_3.get(0).equals(3.0));
            assertTrue(row_3.get(1) == null);
            assertTrue(row_3.get(2).equals(toDate("01/03/2015")));
            assertTrue(row_3.get(3) == null);
            assertTrue(row_3.get(4).equals(3.345));
            assertTrue(row_3.get(5) == null);

            List row_4 = spreadsheetData.spreadsheetData.get(3);
            assertTrue(row_4.size() == 6);
            assertTrue(row_4.get(0).equals(4.0));
            assertTrue(row_4.get(1).equals("four"));
            assertTrue(row_4.get(2) == null);
            assertTrue(row_4.get(3) == null);
            assertTrue(row_4.get(4).equals("four"));
            assertTrue(row_4.get(5).equals("hello 1"));

            List row_5 = spreadsheetData.spreadsheetData.get(4);
            assertTrue(row_5.size() == 6);
            assertTrue(row_5.get(0).equals(5.0));
            assertTrue(row_5.get(1).equals("five"));
            assertTrue(row_5.get(2).equals(toDate("01/05/2015")));
            assertTrue(row_5.get(3) == null);
            assertTrue(row_5.get(4).equals("five"));
            assertTrue(row_5.get(5) == null);
        }
        catch (NonStringValueFoundInHeader nonStringValueFoundInHeader) {
            fail();
        }
    }

    @Test
    public void testExtractDataFromCSVFile_2() throws Exception {

        try {
            File file = Utilities.getFile("/SpreadsheetForReadingTest_2.csv");

            SpreadsheetData spreadsheetData = CSVFileReader.extractDataFromFile(file);

            List row_1 = spreadsheetData.spreadsheetData.get(0);
            assertTrue(row_1.size() == 5);
            assertTrue(row_1.get(0).equals(1.0));
            assertTrue(row_1.get(1).equals("one"));
            assertTrue(row_1.get(2).equals(toDate("01/01/2015")));
            assertTrue(row_1.get(3) == null);
            assertTrue(row_1.get(4).equals("one"));

            List row_2 = spreadsheetData.spreadsheetData.get(1);
            assertTrue(row_2.size() == 5);
            assertTrue(row_2.get(0) == null);
            assertTrue(row_2.get(1).equals("two"));
            assertTrue(row_2.get(2).equals(toDate("01/02/2015")));
            assertTrue(row_2.get(3) == null);
            assertTrue(row_2.get(4).equals(2.0));

            List row_3 = spreadsheetData.spreadsheetData.get(2);
            assertTrue(row_3.size() == 5);
            assertTrue(row_3.get(0).equals(3.0));
            assertTrue(row_3.get(1) == null);
            assertTrue(row_3.get(2).equals(toDate("01/03/2015")));
            assertTrue(row_3.get(3) == null);
            assertTrue(row_3.get(4).equals(3.345));

            List row_4 = spreadsheetData.spreadsheetData.get(3);
            assertTrue(row_4.size() == 5);
            assertTrue(row_4.get(0).equals(4.0));
            assertTrue(row_4.get(1).equals("four"));
            assertTrue(row_4.get(2) == null);
            assertTrue(row_4.get(3) == null);
            assertTrue(row_4.get(4).equals("four"));

            List row_5 = spreadsheetData.spreadsheetData.get(4);
            assertTrue(row_5.size() == 5);
            assertTrue(row_5.get(0).equals(5.0));
            assertTrue(row_5.get(1).equals("five"));
            assertTrue(row_5.get(2).equals(toDate("01/05/2015")));
            assertTrue(row_5.get(3) == null);
            assertTrue(row_5.get(4).equals("five"));
        }
        catch (NonStringValueFoundInHeader nonStringValueFoundInHeader) {
            fail();
        }
    }

    @Test
    public void testExtractDataFromCSVFile_3() throws Exception {

        try {
            File file = Utilities.getFile("/SpreadsheetForReadingTest_3.csv");

            SpreadsheetData spreadsheetData = CSVFileReader.extractDataFromFile(file);

            assertTrue(spreadsheetData.columnNames.size() == 6);
            assertTrue(spreadsheetData.columnNames.get(0).equals("Column 1"));
            assertTrue(spreadsheetData.columnNames.get(1).equals("Column 2"));
            assertTrue(spreadsheetData.columnNames.get(2).equals("Column 3"));
            assertTrue(spreadsheetData.columnNames.get(3).equals("Column 4"));
            assertTrue(spreadsheetData.columnNames.get(4).equals("Column 5"));
            assertTrue(spreadsheetData.columnNames.get(5).equals("Column 6"));

            assertTrue(spreadsheetData.spreadsheetData.size() == 5);

            List row_1 = spreadsheetData.spreadsheetData.get(0);
            assertTrue(row_1.size() == 6);
            assertTrue(row_1.get(0).equals(1.0));
            assertTrue(row_1.get(1).equals("one"));
            assertTrue(row_1.get(2).equals(toDate("01/01/2015")));
            assertTrue(row_1.get(3) == null);
            assertTrue(row_1.get(4).equals("one"));
            assertTrue(row_1.get(5) == null);

            List row_2 = spreadsheetData.spreadsheetData.get(1);
            assertTrue(row_2.size() == 6);
            assertTrue(row_2.get(0) == null);
            assertTrue(row_2.get(1).equals("two"));
            assertTrue(row_2.get(2).equals(toDate("01/02/2015")));
            assertTrue(row_2.get(3) == null);
            assertTrue(row_2.get(4).equals(2.0));
            assertTrue(row_2.get(5) == null);

            List row_3 = spreadsheetData.spreadsheetData.get(2);
            assertTrue(row_3.size() == 6);
            assertTrue(row_3.get(0).equals(3.0));
            assertTrue(row_3.get(1) == null);
            assertTrue(row_3.get(2).equals(toDate("01/03/2015")));
            assertTrue(row_3.get(3) == null);
            assertTrue(row_3.get(4).equals(3.345));
            assertTrue(row_3.get(5) == null);

            List row_4 = spreadsheetData.spreadsheetData.get(3);
            assertTrue(row_4.size() == 6);
            assertTrue(row_4.get(0).equals(4.0));
            assertTrue(row_4.get(1).equals("four"));
            assertTrue(row_4.get(2) == null);
            assertTrue(row_4.get(3) == null);
            assertTrue(row_4.get(4).equals("four"));
            assertTrue(row_4.get(5).equals("hello 1"));

            List row_5 = spreadsheetData.spreadsheetData.get(4);
            assertTrue(row_5.size() == 6);
            assertTrue(row_5.get(0).equals(5.0));
            assertTrue(row_5.get(1).equals("five"));
            assertTrue(row_5.get(2).equals(toDate("01/05/2015")));
            assertTrue(row_5.get(3) == null);
            assertTrue(row_5.get(4).equals("five"));
            assertTrue(row_5.get(5) == null);
        }
        catch (NonStringValueFoundInHeader nonStringValueFoundInHeader) {
            fail();
        }
    }

    private Date toDate(String dateAsString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        return sdf.parse(dateAsString);
    }
}