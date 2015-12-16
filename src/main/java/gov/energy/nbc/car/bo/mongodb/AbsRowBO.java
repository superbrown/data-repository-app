package gov.energy.nbc.car.bo.mongodb;


import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import gov.energy.nbc.car.ResultsMode;
import gov.energy.nbc.car.bo.IRowBO;
import gov.energy.nbc.car.dao.IRowDAO;
import gov.energy.nbc.car.dao.dto.ComparisonOperator;
import gov.energy.nbc.car.dao.dto.SearchCriterion;
import gov.energy.nbc.car.dao.mongodb.DAOUtilities;
import gov.energy.nbc.car.model.IRowDocument;
import gov.energy.nbc.car.model.mongodb.common.Metadata;
import gov.energy.nbc.car.model.mongodb.common.Row;
import gov.energy.nbc.car.model.mongodb.common.StoredFile;
import gov.energy.nbc.car.model.mongodb.document.RowDocument;
import gov.energy.nbc.car.restEndpoint.DataType;
import gov.energy.nbc.car.settings.ISettings;
import gov.energy.nbc.car.utilities.PerformanceLogger;
import gov.energy.nbc.car.utilities.Utilities;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.*;

public abstract class AbsRowBO implements IRowBO {

    protected Logger log = Logger.getLogger(getClass());

    protected IRowDAO rowDAO;

    public AbsRowBO(ISettings settings) {

        init(settings);
    }

    protected abstract void init(ISettings settings);

    @Override
    public String getRow(String rowId) {

        IRowDocument rowDocument = getRowDAO().get(rowId);

        if (rowDocument == null) { return null; }

        String json = DAOUtilities.serialize(rowDocument);
        return json;
    }

    @Override
    public String getRows(String query, ResultsMode resultsMode) {

        List<Document> rowDocuments = getRowsAsDocuments(query, resultsMode);

        String json = DAOUtilities.serialize(rowDocuments);
        return json;
    }

    protected List<Document> getRowsAsDocuments(String query, ResultsMode resultsMode) {

        BasicDBList criteria = (BasicDBList) JSON.parse(query);

        List<SearchCriterion> rowSearchCriteria = new ArrayList<>();

        for (Object criterion : criteria) {

            BasicDBObject criterionDocument = (BasicDBObject)criterion;

            String name = (String) criterionDocument.get("name");
            String dataTypeString = (String) criterionDocument.get("dataType");
            Object dataType = DataType.valueOf(dataTypeString);
            Object rawValue = criterionDocument.get("value");
            ComparisonOperator comparisonOperator = ComparisonOperator.valueOf((String) criterionDocument.get("comparisonOperator"));

            Object value = null;
            if (dataType == DataType.STRING) {
                String aString = rawValue.toString();
                value = aString;
            }
            else if (dataType == DataType.NUMBER) {
                Double aDouble = new Double(rawValue.toString());
                value = aDouble;
            }
            else if (dataType == DataType.DATE) {

                value = toDateWithTheTimeOfDayAdjustedSoComparisonOperatesCorrectly(
                        rawValue, comparisonOperator);
            }
            else if (dataType == DataType.BOOLEAN) {
                Boolean aBoolean = Boolean.valueOf(rawValue.toString());
                value = aBoolean;
            }

            if (value instanceof Date && comparisonOperator == ComparisonOperator.EQUALS) {

                List<SearchCriterion> searchCriteria =
                        crateSearchCriteriaToMakeSureWholeDayIsCovered(name, (Date) value);
                rowSearchCriteria.addAll(searchCriteria);
            }
            else {
                SearchCriterion searchCriterion = new SearchCriterion(name, value, comparisonOperator);
                rowSearchCriteria.add(searchCriterion);
            }
        }

        return getRowDAO().query(rowSearchCriteria, resultsMode);
    }

    protected List<SearchCriterion> crateSearchCriteriaToMakeSureWholeDayIsCovered(String name, Date date) {

        Calendar beginningOfTheDay = Utilities.toCalendar(date);
        Utilities.setTimeToTheBeginningOfTheDay(beginningOfTheDay);

        Calendar endOfTheDay = Utilities.clone(beginningOfTheDay);
        Utilities.setTimeToTheEndOfTheDay(endOfTheDay);

        List<SearchCriterion> searchCriteria = new ArrayList<>();

        SearchCriterion laterThanBeginningOfTheDay =
                new SearchCriterion(name, beginningOfTheDay.getTime(), ComparisonOperator.GREATER_THAN_OR_EQUAL);
        searchCriteria.add(laterThanBeginningOfTheDay);

        SearchCriterion earlierThanTheEndOfDay = new SearchCriterion(
                name, endOfTheDay.getTime(), ComparisonOperator.LESS_THAN_OR_EQUAL);
        searchCriteria.add(earlierThanTheEndOfDay);

        return searchCriteria;
    }

    protected Date toDateWithTheTimeOfDayAdjustedSoComparisonOperatesCorrectly(Object rawValue, ComparisonOperator comparisonOperator) {

        Calendar calendar = Utilities.toCalendar(rawValue.toString());
        adjustTimeOfDaySoComparisonsOperatesCorrectly(calendar, comparisonOperator);
        return calendar.getTime();
    }

    protected void adjustTimeOfDaySoComparisonsOperatesCorrectly(Calendar calendar, ComparisonOperator comparisonOperator) {

        if (comparisonOperator == ComparisonOperator.GREATER_THAN_OR_EQUAL) {
            Utilities.setTimeToTheBeginningOfTheDay(calendar);
        }
        else if (comparisonOperator == ComparisonOperator.LESS_THAN_OR_EQUAL) {
            Utilities.setTimeToTheEndOfTheDay(calendar);
        }
        else if (comparisonOperator == ComparisonOperator.GREATER_THAN) {
            Utilities.setTimeToTheEndOfTheDay(calendar);
        }
        else if (comparisonOperator == ComparisonOperator.LESS_THAN) {
            Utilities.setTimeToTheBeginningOfTheDay(calendar);
        }
    }

    @Override
    public String getRowsFlat(String query) {

        List<Document> rowDocuments = getRowsAsDocuments(
                query,
                ResultsMode.INCLUDE_ONLY_DATA_COLUMNS_BEING_FILTERED_UPON);

        BasicDBList rowsFlat = flatten(rowDocuments, Purpose.FOR_SCREEN_DIAPLAYED_SEARCH_RESULTS);

        String json = DAOUtilities.serialize(rowsFlat);
        return json;
    }

    private enum Purpose {
        FOR_FILE_DOWNLOAD,
        FOR_SCREEN_DIAPLAYED_SEARCH_RESULTS,
    }

    private BasicDBList flatten(List<Document> rowDocuments, Purpose purpose) {

        BasicDBList rowsFlat = new BasicDBList();

        for (Document document : rowDocuments) {

            Document row = new Document();

            Document metadata = (Document) document.get(RowDocument.ATTR_KEY__METADATA);
            Document data = (Document) document.get(RowDocument.ATTR_KEY__DATA);

            // link for downloading the file
            // DESIGN NOTE: I know, this is the wrong architectural layer. I'm in a time crunch right now.
            Document uploadedFile = (Document) metadata.get(Metadata.ATTR_KEY__UPLOADED_FILE);

            String originalFileName = (String) uploadedFile.get(StoredFile.ATTR_KEY__ORIGINAL_FILE_NAME);

            if (purpose == Purpose.FOR_FILE_DOWNLOAD) {
                row.put(StoredFile.ATTR_KEY__ORIGINAL_FILE_NAME, originalFileName);
            }

            if (purpose == Purpose.FOR_SCREEN_DIAPLAYED_SEARCH_RESULTS) {
                String datasetId = ((ObjectId) document.get(RowDocument.ATTR_KEY__DATASET_ID)).toHexString();
                Integer rowNumber = (Integer) data.get(Row.ATTR_KEY__ROW_NUMBER);
                row.put("Source",
                        "<a href='/api/dataset/" + datasetId + "/" + "uploadedFile' " +
                                "target='_blank'>" +
                                originalFileName + "</a> (row " + rowNumber + ")");
            }

//            row.put(Metadata.ATTR_KEY__DATA_CATEGORY, metadata.get(Metadata.ATTR_KEY__DATA_CATEGORY));
            row.put(Metadata.ATTR_KEY__SUBMISSION_DATE, toString((Date) metadata.get(Metadata.ATTR_KEY__SUBMISSION_DATE)));
            row.put(Metadata.ATTR_KEY__SUBMITTER, metadata.get(Metadata.ATTR_KEY__SUBMITTER));
            row.put(Metadata.ATTR_KEY__PROJECT_NAME, metadata.get(Metadata.ATTR_KEY__PROJECT_NAME));
            row.put(Metadata.ATTR_KEY__CHARGE_NUMBER, metadata.get(Metadata.ATTR_KEY__CHARGE_NUMBER));
            row.put(Metadata.ATTR_KEY__COMMENTS, metadata.get(Metadata.ATTR_KEY__COMMENTS));

            for (String name : data.keySet()) {

                Object value = data.get(name);

                if (value == null) {

                }
                else if (Row.ATTR_KEY__ROW_NUMBER.equals(name)) {
                    // we already grabbed this above
                }
                else if (value instanceof ObjectId) {
                    row.put(name, ((ObjectId) value).toHexString());
                }
                else if (value instanceof Number) {
                    String stringValue = value.toString();
                    row.put(name, stringValue);
                }
                else if (value instanceof Date) {
                    row.put(name, toString((Date) value));
                }
                else {
                    row.put(name, value.toString());
                }
            }

            rowsFlat.add(row);
        }

        return rowsFlat;
    }

    private static final Comparator ALPHANUMERIC_COMPARATOR = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {

            if ((o1 instanceof String && o2 instanceof String) == false){
                throw new RuntimeException("Both values must be strings: " +
                        "o1: " + o1 + " (" + o1.getClass() + "), " +
                        "o2: " + o2 + " (" + o2.getClass() + ")");
            }

            String string_1 = (String) o1;
            String string_2 = (String) o2;

            Double number_1 = toNumberIfPossible(string_1);
            Double number_2 = toNumberIfPossible(string_2);

            if (number_1 != null) {

                if (number_2 != null) {
                    // both are numbers
                    return number_1.compareTo(number_2);
                }
                else {
                    // first is a number, second is not
                    return 1;
                }
            }
            else {

                if (number_2 != null) {
                    // first is not a number, second is
                    return -1;
                }
                else {
                    // neither are numbers
                    return string_1.compareTo(string_2);
                }
            }
        }

        private Double toNumberIfPossible(String string_1) {

            try {
                return Double.parseDouble(string_1);
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };

    @Override
    public XSSFWorkbook getRowsAsExcelWorkbook(String query) {

        List<Document> documents = getRowsAsDocuments(query, ResultsMode.INCLUDE_ALL_DATA);

        BasicDBList basicDBList = flatten(documents, Purpose.FOR_FILE_DOWNLOAD);

        XSSFWorkbook workbook = toExcelWorkbook(basicDBList);
        return workbook;
    }

    private XSSFWorkbook toExcelWorkbook(BasicDBList documents) {

        List<String> allKeys = getAllKeysAsAList(documents);
        allKeys.remove("Source"); // we don't want to include
        Collections.sort(allKeys, ALPHANUMERIC_COMPARATOR);

        // Sample code:
        // http://www.avajava.com/tutorials/lessons/how-do-i-write-to-an-excel-file-using-poi.html

        XSSFWorkbook workbook = new XSSFWorkbook();

        XSSFSheet worksheet = workbook.createSheet("sheet");

        // create heading row
        short rowIndex = 0;

        XSSFRow row = worksheet.createRow(rowIndex);

        int columnIndex = 0;

        for (String columnName : allKeys) {
            XSSFCell cell = row.createCell(columnIndex);
            cell.setCellValue(columnName);
            columnIndex++;
        }

        for (Object object : documents) {

            rowIndex++;
            Document document = (Document) object;

            row = worksheet.createRow(rowIndex);

            columnIndex = 0;
            for (String columnName : allKeys) {

                XSSFCell cell = row.createCell(columnIndex);

                if (document.containsKey(columnName)) {
                    setCellValue(cell, document.get(columnName));
                }
                else {
                    // don't set cell's value
                }

                columnIndex++;
            }
        }
        return workbook;
    }

    private void setCellValue(XSSFCell cell, Object value) {
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        }
        else if (value instanceof String) {
            cell.setCellValue((String) value);
        }
        else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        }
        else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        }
        else {
            throw new RuntimeException("Encountered unrecognized value: " + value);
        }
    }

    private List<String> getAllKeysAsAList(BasicDBList documents) {

        TreeSet<String> allUniqueKeys = new TreeSet<>();
        for (Object document : documents) {
            allUniqueKeys.addAll(((Document)document).keySet());
        }

        return new ArrayList(allUniqueKeys);
    }

//    @Override
//    public String getRows(RowSearchCriteria rowSearchCriteria, String projection) {
//
//        List<Document> rowDocuments = getRowDAO(testMode).query(rowSearchCriteria);
//
//        if (rowDocuments.size() == 0) { return null; }
//
//        String json = DAOUtilities.serialize(rowDocuments);
//        return json;
//    }

//    public String getRows(Bson query, Bson projection) {
//
//        List<Document> rowDocuments = getRowDAO(testMode).query(query);
//
//        if (rowDocuments.size() == 0) { return null; }
//
//        String json = DAOUtilities.serialize(rowDocuments);
//        return json;
//    }

    @Override
    public String getRows(List<SearchCriterion> rowSearchCriteria, ResultsMode resultsMode) {

        List<Document> rowDocuments = getRowDAO().query(rowSearchCriteria, resultsMode);

        if (rowDocuments.size() == 0) { return null; }

        String json = DAOUtilities.serialize(rowDocuments);
        return json;
    }

    @Override
    public String getAllRows() {

        Iterable<Document> rowDocuments = getRowDAO().getAll();

        String jsonOut = DAOUtilities.serialize(rowDocuments);
        return jsonOut;
    }


    @Override
    public String getRowsAssociatedWithDataset(String datasetId) {

        Document idFilter = new Document().append(
                RowDocument.ATTR_KEY__DATASET_ID, new ObjectId(datasetId));

        PerformanceLogger performanceLogger = new PerformanceLogger(log, "getRows(testMode).query(" + idFilter.toJson() + ")");
        List<Document> rowDocuments = getRowDAO().get(idFilter, null);
        performanceLogger.done();

        String jsonOut = DAOUtilities.serialize(rowDocuments);
        return jsonOut;
    }

    @Override
    public IRowDAO getRowDAO() {
        return rowDAO;
    }

    protected String toString(Date date) {
        String string = new SimpleDateFormat("yyyy-MM-dd").format(date);
        return string;
    }

}