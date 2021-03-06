package gov.energy.nrel.dataRepositoryApp.model.document.mongodb;

import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.AbstractDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

public class CellDocument extends AbstractDocument {

    public static final String MONGO_KEY__ROW_ID = "rowId";
    public static final String MONGO_KEY__COLUMN_NAME = "columnName";
    public static final String MONGO_KEY__VALUE = "value";

    public CellDocument(ObjectId rowId, String columnName, Object value) {

        init(rowId, columnName, value);
    }

    public CellDocument(Document document) {
        super(document);
    }

    protected void init(ObjectId rowId, String columnName, Object value) {

        this.put(MONGO_KEY__ROW_ID, rowId);
        this.put(MONGO_KEY__COLUMN_NAME, columnName);
        this.put(MONGO_KEY__VALUE, value);
    }

    @Override
    protected void init(Document document) {

        if (document == null) {
            return;
        }

        initObjectId(document);

        ObjectId rowId = (ObjectId) document.get(MONGO_KEY__ROW_ID);
        String columnName = (String)document.get(MONGO_KEY__COLUMN_NAME);
        Object value = document.get(MONGO_KEY__VALUE);

        init(rowId, columnName, value);
    }
}
