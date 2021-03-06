package gov.energy.nrel.dataRepositoryApp.dao.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import gov.energy.nrel.dataRepositoryApp.dao.IDAO;
import gov.energy.nrel.dataRepositoryApp.dao.dto.IDeleteResults;
import gov.energy.nrel.dataRepositoryApp.dao.exception.UnknownEntity;
import gov.energy.nrel.dataRepositoryApp.dao.mongodb.dto.DeleteResults;
import gov.energy.nrel.dataRepositoryApp.model.common.mongodb.AbstractDocument;
import gov.energy.nrel.dataRepositoryApp.settings.ISettings;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbsDAO implements IDAO, IMongodbDAO {

    protected MongoDatabase database;
    protected MongoClient mongoClient;
    private MongoCollection<Document> collection;

    private String collectionName;

    protected ISettings settings;
    protected static Logger log = Logger.getLogger(AbsDAO.class);

    static {

    }

    public AbsDAO(String collectionName, ISettings settings) {

        init(collectionName, settings);
        makeSureTableColumnsIRelyUponAreIndexed();
    }

    @Override
    public void init(String collectionName, ISettings settings) {

        this.collectionName = collectionName;
        this.settings = settings;

        mongoClient = MongoClients.getClientForHost(
                settings.getMongoDbHost(),
                Integer.valueOf(settings.getMongoDbPort()));

        database = mongoClient.getDatabase(
                settings.getMongoDatabaseName());
    }

    @Override
    public ObjectId add(Object model) {

        if ((model instanceof Document) == false) {

            throw new InvalidParameterException("Must be Document object, but was a :" + model.getClass().getName());
        }

        Document document = (Document)model;

        // DESIGN NOTE: I don't know why we need to create a wrapper here, but if we don't, Mongo throws the following
        // exception:
        //
        // org.bson.codecs.configuration.CodecConfigurationException: Can't find a codec for class
        // gov.energy.nbc.dataset.model.document.Dataset.

        Document wrapper = new Document(document);
        getCollection().insertOne(wrapper);

        ObjectId objectId = wrapper.getObjectId("_id");
        return objectId;
    }

    @Override
    public void addMany(List<Document> models) {

        getCollection().insertMany(models);
    }


    @Override
    public Document getOne(Bson filter) {
        Document document = DAOUtilities.getOne(getCollection(), filter);

        if (document == null) {
            return null;
        }

        return createDocumentOfTypeDAOHandles(document);
    }

    @Override
    public Long getCount() {
        return getCollection().count();
    }

    @Override
    public Document getOne(Bson filter, Bson projection) {
        Document document = DAOUtilities.getOne(getCollection(), filter, projection);
        return document;
    }


    @Override
    public Document getOneWithId(String id) {
        ObjectId objectId = null;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            // this means there is no matching document
            return null;
        }
        return getOne(objectId);
    }

    @Override
    public Document getOne(ObjectId objectId) {
        Document idFilter = createIdFilter(objectId);
        return getOne(idFilter);
    }

    @Override
    public Document getOne(ObjectId objectId, Bson projection) {
        Document idFilter = createIdFilter(objectId);
        Document document = getOne(idFilter, projection);
        return document;
    }


    @Override
    public List<Document> get(Bson filter) {

        List<Document> documents = DAOUtilities.get(getCollection(), filter);
        return documents;
    }

    @Override
    public List<Document> get(Bson filter, Bson projection) {

        List<Document> documents = DAOUtilities.get(getCollection(), filter, projection);
        return documents;
    }

    @Override
    public void dropCollection() {

        getCollection().drop();
    }

    @Override
    public IDeleteResults delete(String id)
            throws UnknownEntity {

        ObjectId objectId = new ObjectId(id);
        return delete(objectId);
    }

    @Override
    public IDeleteResults delete(ObjectId objectId)
            throws UnknownEntity {

        Document idFilter = createIdFilter(objectId);

        Document document = getOne(idFilter);;
        if (document == null) {
            throw new UnknownEntity();
        }

        DeleteResult deleteResult = getCollection().deleteOne(idFilter);

        DeleteResults deleteResults = new DeleteResults(deleteResult);

        return deleteResults;
    }

    public static Document createIdFilter(ObjectId objectId) {

        Document idFilter = new Document().
                append(AbstractDocument.MONGO_KEY__ID, objectId);
        return idFilter;
    }


    @Override
    public MongoCollection<Document> getCollection() {

        if (collection == null) {
            collection = database.getCollection(collectionName).withWriteConcern(WriteConcern.NORMAL);
        }

        return collection;
    }

    @Override
    public MongoDatabase getDatabase() {

        return database;
    }

    @Override
    public Iterable<Document> getAll() {

        return getCollection().find(new BasicDBObject());
    }

    @Override
    public UpdateResult updateOne(String id, Bson update) {

        Bson filter = createIdFilter(new ObjectId(id));
        UpdateResult updateResult = getCollection().updateOne(filter, new Document("$set", update));
        return updateResult;
    }

    @Override
    public List<Document> createDocumentsOfTypeDAOHandles(List<Document> documents) {

        List<Document> documentsOfTypeDAOHandles = new ArrayList<>();

        for (Document document : documents) {

            Document documentOfTypeDAOHandles = createDocumentOfTypeDAOHandles(document);
            documentsOfTypeDAOHandles.add(documentOfTypeDAOHandles);
        }

        return documentsOfTypeDAOHandles;
    }

    @Override
    public MongoClient getMongoClient() {
        return mongoClient;
    }

    @Override
    public String getCollectionName() {
        return collectionName;
    }

    @Override
    public ISettings getSettings() {
        return settings;
    }

    @Override
    public void logCollectionIndexes() {

        if (log.isInfoEnabled()) {

            StringBuilder message = new StringBuilder();
            message.append(getCollectionName() + " indexes");

            ListIndexesIterable<Document> indexes = getCollection().listIndexes();
            for (Document index : indexes) {
                message.append(index.toJson());
            }

            log.info(message);
        }
    }
}
