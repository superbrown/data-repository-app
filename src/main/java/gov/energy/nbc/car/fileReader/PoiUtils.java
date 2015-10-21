package gov.energy.nbc.car.fileReader;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class PoiUtils {

    protected static Logger log = Logger.getLogger(PoiUtils.class);

    public static Object toAppropriateDataType(Cell cell) {

        switch (cell.getCellType()) {

            case Cell.CELL_TYPE_STRING:
                return cell.getStringCellValue();

            case Cell.CELL_TYPE_BLANK:
                return null;

            case Cell.CELL_TYPE_BOOLEAN:
                return cell.getBooleanCellValue();

            case Cell.CELL_TYPE_ERROR:
                return cell.getErrorCellValue();

            case Cell.CELL_TYPE_FORMULA:
                return null;

            case Cell.CELL_TYPE_NUMERIC:

                // Date
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    return date;
                }
                else {
                    return cell.getNumericCellValue();
                }
        }

        log.error("This shouldn't happen because we should have covered all possible cases.");
        return null;
    }

    public static List<String> determineColumnNames(Sheet sheet)
            throws NonStringValueFoundInHeader {

        Row firstRow = sheet.iterator().next();
        Iterator<Cell> cellIterator = firstRow.cellIterator();

        List<String> headings = new ArrayList();

        boolean lastColumnEncountered = false;
        int columnNumber = 1;
        while (cellIterator.hasNext()) {

            Cell cell = cellIterator.next();
            int cellType = cell.getCellType();

            switch (cellType) {

                case Cell.CELL_TYPE_STRING:
                    headings.add(cell.getStringCellValue());
                    break;

                case Cell.CELL_TYPE_BLANK:
                    lastColumnEncountered = true;
                    break;

                case Cell.CELL_TYPE_BOOLEAN:
                case Cell.CELL_TYPE_ERROR:
                case Cell.CELL_TYPE_FORMULA:
                case Cell.CELL_TYPE_NUMERIC:
                    throw new NonStringValueFoundInHeader(columnNumber, cell.toString());
            }

            if (lastColumnEncountered) {
                break;
            }

            columnNumber++;
        }

        return headings;
    }
}