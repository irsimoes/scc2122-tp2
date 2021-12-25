package scc.layers;

import java.util.ArrayList;
import java.util.List;

import scc.data.*;

import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.client.model.Filters.eq;

import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;

public class MongoDBLayer {

	private static final String USERS_CONTAINER = "Users";
	private static final String MESSAGES_CONTAINER = "Messages";
	private static final String CHANNELS_CONTAINER = "Channels";

	private static final String DELETED_USERS_CONTAINER = "DeletedUsers";
	private static final String DELETED_MESSAGES_CONTAINER = "DeletedMessages";
	private static final String DELETED_CHANNELS_CONTAINER = "DeletedChannels";

	private static MongoDBLayer instance;
	private MongoCollection currentCollection;
	private CodecRegistry pojoCodecRegistry;

	public static synchronized MongoDBLayer getInstance() {
		if (instance != null)
			return instance;

		CodecProvider pojoP = PojoCodecProvider.builder().automatic(true).build();
		CodecRegistry pojoR = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoP));

		instance = new MongoDBLayer(pojoR);

		return instance;
	}

	public MongoDBLayer(CodecRegistry pojoCodecRegistry) {
		this.pojoCodecRegistry = pojoCodecRegistry;
	}

	private synchronized <T> void init(Class<T> type, boolean isDeleted) {

		String mongoHost = System.getenv("MONGO_HOST");
		String mongoPort = System.getenv("MONGO_PORT");
		String mongoUser = System.getenv("MONGO_USER");
		String mongoPassword = System.getenv("MONGO_PASSWORD");
		String uri = String.format("mongodb://%s:%s@%s:%s", mongoUser, mongoPassword, mongoHost, mongoPort);

		MongoClient mongoClient = MongoClients.create(uri);
		MongoDatabase database = mongoClient.getDatabase("data").withCodecRegistry(pojoCodecRegistry);

		if (!isDeleted) {
			if (type.equals(UserDAO.class))
				currentCollection = database.getCollection(USERS_CONTAINER, UserDAO.class);
			else if (type.equals(MessageDAO.class))
				currentCollection = database.getCollection(MESSAGES_CONTAINER, MessageDAO.class);
			else if (type.equals(ChannelDAO.class))
				currentCollection = database.getCollection(CHANNELS_CONTAINER, ChannelDAO.class);
		} else {
			if (type.equals(UserDAO.class))
				currentCollection = database.getCollection(DELETED_USERS_CONTAINER, UserDAO.class);
			else if (type.equals(MessageDAO.class))
				currentCollection = database.getCollection(DELETED_MESSAGES_CONTAINER, MessageDAO.class);
			else if (type.equals(ChannelDAO.class))
				currentCollection = database.getCollection(DELETED_CHANNELS_CONTAINER, ChannelDAO.class);
		}
	}

	public <T> boolean delById(String id, String partKey, Class<T> type, boolean isFromDeleted) {
		init(type, isFromDeleted);

		DeleteResult result = currentCollection.deleteOne(eq("_id", id));

		return result.getDeletedCount() == 1;
	}

	public <T> String put(T item, boolean isFromDeleted) {
		init(item.getClass(), isFromDeleted);

		InsertOneResult result = ((MongoCollection<T>) currentCollection).insertOne(item);

		return result.getInsertedId().toString();
	}

	public <T> T getById(String id, Class<T> type, boolean isFromDeleted) {
		init(type, isFromDeleted);

		T item = (T) currentCollection.find(eq("_id", id)).first();

		return item;
	}

	public <T> List<T> getAll(Class<T> type, boolean isFromDeleted) {
		init(type, isFromDeleted);

		List<T> objs = new ArrayList<T>();
		((MongoCollection<T>) currentCollection).find().into(objs);

		return objs;
	}

	public <T> boolean patchAdd(String id, Class<T> type, String field, String change) {
		init(type, false);

		T item = (T) currentCollection.findOneAndUpdate(eq("_id", id), Updates.addToSet(field, change));

		return item != null;
	}

	public <T> boolean patchRemove(String id, Class<T> type, String field, String change) {
		init(type, false);

		T item = (T) currentCollection.findOneAndUpdate(eq("_id", id), Updates.pull(field, change));

		return item != null;
	}

	public List<MessageDAO> getMessagesFromChannel(String channelId, int st, int len) {
		init(MessageDAO.class, false);

		List<MessageDAO> objs = new ArrayList<MessageDAO>();
		currentCollection.find(eq("channel", channelId)).sort(Sorts.descending("ts")).skip(st).limit(len).into(objs);

		return objs;
	}

}
