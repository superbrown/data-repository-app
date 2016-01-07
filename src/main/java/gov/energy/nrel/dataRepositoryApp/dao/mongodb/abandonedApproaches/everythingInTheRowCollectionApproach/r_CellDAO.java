package gov.energy.nrel.dataRepositoryApp.dao.mongodb.abandonedApproaches.everythingInTheRowCollectionApproach;


import com.mongodb.client.result.DeleteResult;
import gov.energy.nrel.dataRepositoryApp.dao.ICellDAO;
import gov.energy.nrel.dataRepositoryApp.dao.dto.IDeleteResults;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.AbsDAO;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.dto.DeleteResults;
import gov.energy.nrel.dataRepositoryApp.model.IRow;
import gov.energy.nrel.dataRepositoryApp.model.mongodb.document.CellDocument;
import gov.energy.nrel.dataRepositoryApp.settings.ISettings;
import gov.energy.nrel.dataRepositoryApp.settings.Settings;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.List;

public class r_CellDAO extends AbsDAO implements ICellDAO {

    public static final String COLLECTION_NAME = "cell";

    public r_CellDAO(String collectionName, Settings settings) {
        super(collectionName, settings);
    }

    public r_CellDAO(ISettings settings) {
        super(COLLECTION_NAME, settings);
    }


    public List<ObjectId> add(ObjectId rowId, IRow row) {

        List<ObjectId> cellIDs = new ArrayList();

        for (String columnName : row.getColumnNames()) {

            Object value = row.getValue(columnName);

            CellDocument cellDocument = new CellDocument(rowId, columnName, value);

            ObjectId cellID = add(cellDocument);
            cellIDs.add(cellID);
        }

        return cellIDs;

    }

    public IDeleteResults deleteCellsAssociatedWithRow(ObjectId rowId) {

        DeleteResults allDeleteResults = new DeleteResults();

        Document rowIdFilter = new Document().
                append(CellDocument.MONGO_KEY__ROW_ID, rowId);

        DeleteResult deleteResult = getCollection().deleteMany(rowIdFilter);

        allDeleteResults.add(deleteResult);

        return allDeleteResults;
    }

    @Override
    protected Document createDocumentOfTypeDAOHandles(Document document) {
        return new CellDocument(document);
    }
}