package gov.energy.nbc.car.dao;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.util.JSON;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

public class DAOUtilities {

    public static List<Document> get(MongoCollection<Document> collection, Bson query, Bson projection) {

        FindIterable<Document> resultsCursor = collection.find(query).projection(projection);
        MongoCursor<Document> resultsIterator = resultsCursor.iterator();
        List<Document> results = DAOUtilities.toList(resultsIterator);
        return results;
    }

    public static List<Document> toList(MongoCursor<Document> resultsIterator) {

        List<Document> list = new ArrayList<>();

        if (resultsIterator.hasNext() == false) {
            return list;
        }

        while (resultsIterator.hasNext()) {
            list.add(resultsIterator.next());
        }

        return list;
    }

    public static String serialize(Object spreadsheetRow) {

        return JSON.serialize(spreadsheetRow);
    }
}