package gov.energy.nrel.dataRepositoryApp.dao.mongodb.singleCellCollectionApproach;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import gov.energy.nrel.dataRepositoryApp.bo.ResultsMode;
import gov.energy.nrel.dataRepositoryApp.dao.ICellDAO;
import gov.energy.nrel.dataRepositoryApp.dao.IRowDAO;
import gov.energy.nrel.dataRepositoryApp.dao.dto.ComparisonOperator;
import gov.energy.nrel.dataRepositoryApp.dao.dto.IDeleteResults;
import gov.energy.nrel.dataRepositoryApp.dao.dto.SearchCriterion;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.AbsDAO;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.DAOUtilities;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.MongoFieldNameEncoder;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.dto.DeleteResults;
import gov.energy.nrel.dataRepositoryApp.model.common.IMetadata;
import gov.energy.nrel.dataRepositoryApp.model.common.IRow;
import gov.energy.nrel.dataRepositoryApp.model.common.IRowCollection;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Metadata;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Row;
import gov.energy.nrel.dataRepositoryApp.model.document.IDatasetDocument;
import gov.energy.nrel.dataRepositoryApp.model.document.IRowDocument;
import gov.energy.nrel.dataRepositoryApp.model.document.mongodb.CellDocument;
import gov.energy.nrel.dataRepositoryApp.model.document.mongodb.RowDocument;
import gov.energy.nrel.dataRepositoryApp.settings.ISettings;
import gov.energy.nrel.dataRepositoryApp.utilities.PerformanceLogger;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

public class sc_RowDAO extends AbsDAO implements IRowDAO {

    public static final String COLLECTION_NAME = "row";

    protected sc_CellDAO cellDAO;

    protected static Logger log = Logger.getLogger(sc_RowDAO.class);;

    public sc_RowDAO(ISettings settings) {

        super(COLLECTION_NAME, settings);
    }

    @Override
    public void init(String collectionName, ISettings settings) {

        super.init(collectionName, settings);
        cellDAO = new sc_CellDAO(settings);
    }

    public IRowDocument get(String id) {

        return (IRowDocument) getOneWithId(id);
    }

    public List<ObjectId> add(ObjectId datasetId, IDatasetDocument datasetDocument, IRowCollection data) {

        List<ObjectId> rowIds = new ArrayList<>();

        IMetadata metadata = datasetDocument.getMetadata();

        for (IRow row : data.getRows()) {

            IRowDocument rowDocument = new RowDocument(
                    datasetId,
                    metadata,
                    row);

//            PerformanceLogger performanceLogger = new PerformanceLogger(log, "insert(rowDocument)");
            ObjectId rowId = add(rowDocument);
//            performanceLogger.done();

            rowIds.add(rowId);

            cellDAO.add(datasetId, datasetDocument, rowId, row);
        }

        return rowIds;
    }

    public IDeleteResults deleteRowsAssociatedWithDataset(ObjectId datasetId) {

        DeleteResults allDeleteResults = new DeleteResults();

        Document datasetIdFilter = new Document().
                append(RowDocument.MONGO_KEY__DATASET_ID, datasetId);

        Bson projection = fields(include(RowDocument.MONGO_KEY__ID));

        List<Document> rowsAssociatedWithDataset = this.query(datasetIdFilter, projection);

        for (Document document : rowsAssociatedWithDataset) {

            ObjectId rowId = (ObjectId) document.get(RowDocument.MONGO_KEY__ID);

            // delete all cells associated with the row
            allDeleteResults.addAll(cellDAO.deleteCellsAssociatedWithRow(rowId));

            // delete the row
            allDeleteResults.addAll(deleteRow(rowId));
        }

        return allDeleteResults;
    }

    private IDeleteResults deleteRow(ObjectId rowId) {

        Document idFilter = createIdFilter(rowId);

        DeleteResult deleteResult = getCollection().deleteOne(idFilter);

        DeleteResults deleteResults = new DeleteResults(deleteResult);

        return deleteResults;

    }

    @Override
    public Document createDocumentOfTypeDAOHandles(Document document) {

        return new RowDocument(document);
    }

    @Override
    public IDeleteResults delete(ObjectId objectId) {

        throw new RuntimeException(
                "This method should not be called because rows should not be deleted independently of " +
                        "their spreasheet.");
    }

    protected List<Document> query(Bson bson, Bson projection) {

        List<Document> documents = get(bson, projection);
        return documents;
    }

    @Override
    public List<Document> query(List<SearchCriterion> searchCriteria, ResultsMode resultsMode) {

        if (searchCriteria.size() == 0) {
            throw new RuntimeException();
        }

        encodeColumnNamesForMongoSafety(searchCriteria);

        List<CriterionAndItsNumberOfMatches> numberOfMatchesForEachCriterion =
                getNumberOfMatchesForEachCriterion(searchCriteria);

        // Sort these in descending order to promote search speed. So we'll first search with the most restrictive
        // criterion and work our way up.
        Collections.sort(numberOfMatchesForEachCriterion);

        CriterionAndItsNumberOfMatches firstCriterion = numberOfMatchesForEachCriterion.get(0);
        if (firstCriterion.getNumberOfMatches() == 0) {
            // since the search is performed as an AND operation, this means that, by definition, there are no matches
            return new ArrayList<>();
        }

        // get the row numbers of all cells that match the first criterion
        List<CriterionAndItsNumberOfMatches> criteria_allButTheFirst = new ArrayList(numberOfMatchesForEachCriterion);
        criteria_allButTheFirst.remove(firstCriterion);

        Set<ObjectId> matchingRowIds = getIdsOfRowsThatMatch(firstCriterion.getCriterion());

        for (CriterionAndItsNumberOfMatches criterion : criteria_allButTheFirst) {

            // DESIGN NOTE: Each time this is called, it'll likely make the set of IDs smaller.
            matchingRowIds = getIdsOfRowsInSubsetThatMatch(matchingRowIds, criterion.getCriterion());
        }

        List<Document> rows;

        if (resultsMode == ResultsMode.INCLUDE_ONLY_DATA_COLUMNS_BEING_FILTERED_UPON) {

            Set<String> dataColumnNamesToIncludedInQueryResults = new LinkedHashSet<>();

            for (SearchCriterion searchCriterion : searchCriteria) {

                String name = searchCriterion.getName();

                if (Metadata.isAMetadataFieldName(MongoFieldNameEncoder.toClientSideFieldName(name)) == false) {

                    dataColumnNamesToIncludedInQueryResults.add(name);
                }
            }
            rows = getRowsWithIds(matchingRowIds, dataColumnNamesToIncludedInQueryResults);
        }
        else {
            rows = getRowsWithIds(matchingRowIds, null);
        }

        return rows;
    }

    protected List<CriterionAndItsNumberOfMatches> getNumberOfMatchesForEachCriterion(
            List<SearchCriterion> searchCriteria) {

        List<CriterionAndItsNumberOfMatches> results = new ArrayList<>();

        for (SearchCriterion searchCriterion : searchCriteria) {

            long count = getCountOfCellsThatMatch(searchCriterion);
            CriterionAndItsNumberOfMatches result = new CriterionAndItsNumberOfMatches(searchCriterion, count);

            results.add(result);

            if (count == 0) {
                return results;
            }
        }

        return results;
    }

    /**
     * If dataColumnNamesToIncludedInQueryResults is null, all data will be returned.
     */
    protected List<Document> getRowsWithIds(Set<ObjectId> rowIds,
                                            Set<String> dataColumnNamesToIncludedInQueryResults) {


        Bson projection;

        if (dataColumnNamesToIncludedInQueryResults != null) {

            List<String> attributesToInclude = new ArrayList<>();

            // mandatory
            attributesToInclude.add(RowDocument.MONGO_KEY__ID);
            attributesToInclude.add(RowDocument.MONGO_KEY__DATASET_ID);
            attributesToInclude.add(RowDocument.MONGO_KEY__METADATA);
            attributesToInclude.add(
                    RowDocument.MONGO_KEY__DATA + "." +
                            MongoFieldNameEncoder.toMongoSafeFieldName(Row.MONGO_KEY__ROW_NUMBER));

            for (String columnIncludedInQuery : dataColumnNamesToIncludedInQueryResults) {
                attributesToInclude.add(
                        RowDocument.MONGO_KEY__DATA + "." +
                                MongoFieldNameEncoder.toMongoSafeFieldName(columnIncludedInQuery));
            }

            projection = fields(include(attributesToInclude));
        }
        else {

            projection = null;
        }

        PerformanceLogger performanceLogger = new PerformanceLogger(log, "getting rows for the matching IDs");

        Bson query = in(RowDocument.MONGO_KEY__ID, rowIds);

        List<Document> results = get(query, projection);

        for (Document result : results) {

            Document data = (Document) result.get(RowDocument.MONGO_KEY__DATA);
            if (data == null) {
                result.put(RowDocument.MONGO_KEY__DATA, new Document());
            }
            else {
                result.put(RowDocument.MONGO_KEY__DATA, DAOUtilities.toDocumentWithClientSideFieldNames(data));
            }
        }

        performanceLogger.done("[RESULTS] results.size(): " + results.size() +
                ", row.count():" + getCount() +
                ", cell.count():" + cellDAO.getCollection().count());

        return results;
    }

    protected Set<ObjectId> getIdsOfRowsInSubsetThatMatch(Set<ObjectId> rowIds, SearchCriterion searchCriterion) {

        // CAUTION: Do NOT change the order of these, as these reflect an index set within the
        //          database.  If you do, you'll have to change the index as well.
        Bson query = and(
                in(CellDocument.MONGO_KEY__ROW_ID, rowIds),

                DAOUtilities.toCriterion(
                        CellDocument.MONGO_KEY__COLUMN_NAME,
                        searchCriterion.getName(),
                        ComparisonOperator.EQUALS),

                DAOUtilities.toCriterion(
                        CellDocument.MONGO_KEY__VALUE,
                        searchCriterion.getValue(),
                        searchCriterion.getComparisonOperator()));

        Bson projection = fields(include(CellDocument.MONGO_KEY__ROW_ID));

        PerformanceLogger performanceLogger = new PerformanceLogger(
                log,
                "[getIdsOfRowsInSubsetThatMatch()] cellDAO.get(" + DAOUtilities.toJSON(query) + ")"
        );

        List<Document> documents = cellDAO.get(query, projection);

        performanceLogger.done("[RESULTS] results.size(): " + documents.size() +
                ", row.count():" + getCount() +
                ", cell.count():" + cellDAO.getCollection().count());

        return toSetOfRowIds(documents);
    }

    public long getCountOfCellsThatMatch(SearchCriterion searchCriterion) {

        // CAUTION: Do NOT change the order of these, as these reflect an index set within the
        //          database.  If you do, you'll have to change the index as well.

        Bson query = and(

                eq(
                        CellDocument.MONGO_KEY__COLUMN_NAME,
                        toMongoSafeFieldName(searchCriterion.getName())),

                DAOUtilities.toCriterion(
                        CellDocument.MONGO_KEY__VALUE,
                        searchCriterion.getValue(),
                        searchCriterion.getComparisonOperator()));

        PerformanceLogger performanceLogger = new PerformanceLogger(
                log,
                "[getCountOfRowsThatMatch()] cellDAO.getCollection().count(" + DAOUtilities.toJSON(query) + ")"
        );
        long count = cellDAO.getCollection().count(query);
        performanceLogger.done("[RESULTS] count: " + count +
                ", row.count():" + getCount() +
                ", cell.count():" + cellDAO.getCollection().count());

        return count;
    }

    public Set<ObjectId> getIdsOfRowsThatMatch(SearchCriterion searchCriterion) {

        Bson rowIdCriterion = eq(CellDocument.MONGO_KEY__COLUMN_NAME, toMongoSafeFieldName(searchCriterion.getName()));

        Bson valueCriterion = DAOUtilities.toCriterion(
                CellDocument.MONGO_KEY__VALUE,
                searchCriterion.getValue(),
                searchCriterion.getComparisonOperator());

        // CAUTION: Do NOT change the order of these, as these reflect an index set within the
        //          database.  If you do, you'll have to change the index as well.
        Bson query = and(rowIdCriterion, valueCriterion);

        Bson projection = fields(include(CellDocument.MONGO_KEY__ROW_ID));

        PerformanceLogger performanceLogger = new PerformanceLogger(log, "[getIdsOfRowsThatMatch()] cellDAO.get(" + DAOUtilities.toJSON(query) + ")");

        List<Document> documents = cellDAO.get(query, projection);

        performanceLogger.done("[RESULTS] results.size(): " + documents.size() +
                ", row.count():" + getCount() +
                ", cell.count():" + cellDAO.getCollection().count());

        return toSetOfRowIds(documents);
    }

    protected boolean rowMatchesTheCriterion(ObjectId rowId, SearchCriterion searchCriterion) {

        // CAUTION: Do NOT change the order of these, as these reflect an index set within the
        //          database.  If you do, you'll have to change the index as well.
        Bson query = and(

                eq(
                        CellDocument.MONGO_KEY__ROW_ID,
                        rowId),

                eq(
                        CellDocument.MONGO_KEY__COLUMN_NAME,
                        toMongoSafeFieldName(searchCriterion.getName())),

                DAOUtilities.toCriterion(
                        CellDocument.MONGO_KEY__VALUE,
                        searchCriterion.getValue(),
                        searchCriterion.getComparisonOperator()));

        Bson projection = fields(include(
                CellDocument.MONGO_KEY__ROW_ID));

        PerformanceLogger performanceLogger = new PerformanceLogger(log, "[rowMatchesTheCriterion()] cellDAO.getOne(" + DAOUtilities.toJSON(query) + ")");
        Document document = cellDAO.getOne(query, projection);
        performanceLogger.done();

        return document != null;
    }

    private Set<ObjectId> toSetOfRowIds(List<Document> documents) {

        Set<ObjectId> objectIds = new HashSet();

        for (Document document : documents) {

            objectIds.add((ObjectId) document.get(CellDocument.MONGO_KEY__ROW_ID));
        }

        return objectIds;
    }

    @Override
    public ICellDAO getCellDAO(String columnName) {
        return getCellDAO();
    }

    public ICellDAO getCellDAO() {
        return cellDAO;
    }

    @Override
    public void makeSureTableColumnsIRelyUponAreIndexed() {

        // DESIGN NOTE: Even though these indexes are on the cell collection, they are being set here because this
        //              is where the code exists that relies upon them. I figured if they were here, they would less
        //              likely get removed by someone who didn't realize they were used somewhere.

        MongoCollection<Document> cellCollection = cellDAO.getCollection();

        BasicDBObject compoundIndex = new BasicDBObject();
        compoundIndex.put(CellDocument.MONGO_KEY__COLUMN_NAME, 1);
        compoundIndex.put(CellDocument.MONGO_KEY__VALUE, 1);
        cellCollection.createIndex(compoundIndex);

        compoundIndex = new BasicDBObject();
        compoundIndex.put(CellDocument.MONGO_KEY__ROW_ID, 1);
        compoundIndex.put(CellDocument.MONGO_KEY__COLUMN_NAME, 1);
        compoundIndex.put(CellDocument.MONGO_KEY__VALUE, 1);
        cellCollection.createIndex(compoundIndex);

        cellDAO.logCollectionIndexes();
    }

    protected void encodeColumnNamesForMongoSafety(List<SearchCriterion> searchCriteria_data) {

        for (SearchCriterion searchCriterion : searchCriteria_data) {

            searchCriterion.setName(toMongoSafeFieldName(searchCriterion.getName()));
        }
    }

    protected String toMongoSafeFieldName(String name) {

        return MongoFieldNameEncoder.toMongoSafeFieldName(name);
    }

    private class CriterionAndItsNumberOfMatches implements Comparable {

        private final SearchCriterion criterion;
        private final Long numberOfMatches;

        public CriterionAndItsNumberOfMatches(SearchCriterion criterion, long numberOfMatches) {

            this.criterion = criterion;
            this.numberOfMatches = numberOfMatches;
        }

        public SearchCriterion getCriterion() {
            return criterion;
        }

        public long getNumberOfMatches() {
            return numberOfMatches;
        }


        @Override
        public int compareTo(Object o) {

            long delta = this.getNumberOfMatches() - ((CriterionAndItsNumberOfMatches) o).getNumberOfMatches();

            if (delta < 0) return -1;
            if (delta > 0) return 1;
            return 0;
        }
    }
}
