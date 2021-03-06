package gov.energy.nrel.dataRepositoryApp.model.document.mongodb;

import gov.energy.nrel.dataRepositoryApp.model.common.IMetadata;
import gov.energy.nrel.dataRepositoryApp.model.common.IRow;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.AbstractDocument;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Metadata;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Row;
import gov.energy.nrel.dataRepositoryApp.model.document.IRowDocument;
import org.bson.Document;
import org.bson.types.ObjectId;

public class RowDocument extends AbstractDocument implements IRowDocument {

    public static final String MONGO_KEY__DATASET_ID = "datasetId";
    public static final String MONGO_KEY__METADATA = "metadata";
    public static final String MONGO_KEY__DATA = "data";

    public RowDocument(ObjectId datasetId, IMetadata metadata, IRow data) {

        init(datasetId, metadata, data);
    }

    public RowDocument(Document object) {
        super(object);
    }

    public RowDocument(String json) {
        super(json);
    }

    private void init(ObjectId datasetId, IMetadata metadata, IRow data) {

        put(MONGO_KEY__DATASET_ID, datasetId);
        put(MONGO_KEY__METADATA, metadata);
        put(MONGO_KEY__DATA, data);
    }

    protected void init(Document document) {

        if (document == null) {
            return;
        }

        initObjectId(document);

        ObjectId rowCollectionId = (ObjectId) document.get(MONGO_KEY__DATASET_ID);
        IMetadata metada = new Metadata((Document) document.get(MONGO_KEY__METADATA));
        IRow data = new Row();

        init(rowCollectionId, metada, data);
    }
}
