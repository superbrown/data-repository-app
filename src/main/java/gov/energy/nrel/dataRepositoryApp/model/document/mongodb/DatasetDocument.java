package gov.energy.nrel.dataRepositoryApp.model.document.mongodb;

import gov.energy.nrel.dataRepositoryApp.dao.mongodb.DAOUtilities;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.AbstractDocument;
import gov.energy.nrel.dataRepositoryApp.model.common.IMetadata;
import gov.energy.nrel.dataRepositoryApp.model.common.IStoredFile;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.Metadata;
import gov.energy.nrel.dataRepositoryApp.model.document.IDatasetDocument;
import org.bson.Document;

import java.util.Date;
import java.util.List;

public class DatasetDocument extends AbstractDocument implements IDatasetDocument {

    public static final String MONGO_KEY__METADATA = "metadata";

    public DatasetDocument(String json) {
        super(json);
    }

    public DatasetDocument(Document object) {
        super(object);
    }

    public DatasetDocument(IMetadata metadata) {

        init(metadata);
    }

    public DatasetDocument(String dataCategory,
                           Date submissionDate,
                           String submitter,
                           String chargeNumber,
                           String projectName,
                           String comments,
                           IStoredFile sourceDocument,
                           String nameOfSubdocumentContainingDataIfApplicable,
                           List<IStoredFile> attachments) {

        IMetadata metadata = new Metadata(
                dataCategory,
                submissionDate,
                submitter,
                chargeNumber,
                projectName,
                comments,
                sourceDocument,
                nameOfSubdocumentContainingDataIfApplicable,
                attachments);

        init(metadata);
    }

    protected void init(Document document) {

        if (document == null) {
            return;
        }

        initObjectId(document);

        IMetadata metadata = new Metadata(DAOUtilities.serialize(document.get(MONGO_KEY__METADATA)));

        init(metadata);
    }

    private void init(IMetadata metadata) {

        put(MONGO_KEY__METADATA, metadata);
    }

    @Override
    public Metadata getMetadata() {
        return (Metadata) get(MONGO_KEY__METADATA);
    }
}
