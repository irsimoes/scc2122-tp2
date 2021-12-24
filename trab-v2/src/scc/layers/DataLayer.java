package scc.layers;

import java.util.List;

import scc.cache.RedisCache;
import scc.data.*;

public class DataLayer {

    private boolean cacheActive;
    private RedisCache cache;
    private MongoDBLayer db;

    private static DataLayer instance;

    public static synchronized DataLayer getInstance() {
        if (instance != null)
            return instance;

        instance = new DataLayer(false);
        return instance;
    }

    public DataLayer(boolean cacheActive) {
        this.cache = RedisCache.getInstance();
        this.db = MongoDBLayer.getInstance();
        this.cacheActive = cacheActive;
    }

    public <T, U> T get(String id, Class<T> type, Class<U> typeDB, boolean isFromDeleted) {
        T item = null;

        if (cacheActive)
            item = cache.getValue(id, type);

        if (item == null) {
            U dbItem = db.getById(id, typeDB, isFromDeleted);
            if (dbItem == null) {
                return null;
            }
            item = constructItem(dbItem, type);
            cache.setValue(id, item);
        }
        return item;
    }

    public <T> List<T> getAll(Class<T> typeDB, boolean isFromDeleted) {
        return db.getAll(typeDB, isFromDeleted);
    }

    public <T, U> void put(String id, T item, U itemDB, Class<T> type, Class<U> typeDB, boolean isFromDeleted) {
        db.put(itemDB, isFromDeleted);

        if (cacheActive && !isFromDeleted) {
            itemDB = db.getById(id, typeDB, isFromDeleted);
            if (itemDB != null)
                cache.setValue(id, constructItem(itemDB, type));
        }
    }

    public <T, U> void delete(String id, String partKey, Class<T> type, Class<U> typeDB, boolean isFromDeleted) {
        db.delById(id, partKey, typeDB, isFromDeleted);

        if (cacheActive)
            cache.delete(id, type);
    }

    public <T, U> void patchAdd(String id, Class<T> type, Class<U> typeDB, String field, String change) {
        db.patchAdd(id, typeDB, field, change);

        if (cacheActive) {
            U itemDB = db.getById(id, typeDB, false);
            if (itemDB != null)
                cache.setValue(id, constructItem(itemDB, type));
        }

    }

    public <T, U> void patchRemove(String id, Class<T> type, Class<U> typeDB, String field, String change) {
        db.patchRemove(id, typeDB, field, change);

        if (cacheActive) {
            U itemDB = db.getById(id, typeDB, false);
            if (itemDB != null)
                cache.setValue(id, constructItem(itemDB, type));
        }
    }

    public List<MessageDAO> getMessagesFromChannel(String channelId, int st, int len) {
        return db.getMessagesFromChannel(channelId, st, len);
    }

    @SuppressWarnings("unchecked")
    private <T> T constructItem(Object item, Class<T> type) {
        if (type.equals(User.class))
            return (T) new User((UserDAO) item);
        else if (type.equals(Message.class))
            return (T) new Message((MessageDAO) item);
        else if (type.equals(Channel.class))
            return (T) new Channel((ChannelDAO) item);
        return null;
    }
}
