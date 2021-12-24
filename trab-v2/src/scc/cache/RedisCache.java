package scc.cache;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;
import redis.clients.jedis.Tuple;

public class RedisCache {
	
	private static JedisPool instance;
	private static RedisCache cache;
	private static final long CACHE_EXPIRATION_TIME = 30; //30 seconds for testing purposes
	private static final long SESSION_EXPIRATION_TIME = 3600; //1 hour
	
	public synchronized static JedisPool getCachePool() {
		if( instance != null)
			return instance;
		final JedisPoolConfig poolConfig = new JedisPoolConfig();

        String redisHostname = "http://192.168.39.159:31380/";

		poolConfig.setMaxTotal(128);
		poolConfig.setMaxIdle(128);
		poolConfig.setMinIdle(16);
		poolConfig.setTestOnBorrow(true);
		poolConfig.setTestOnReturn(true);
		poolConfig.setTestWhileIdle(true);
		poolConfig.setNumTestsPerEvictionRun(3);
		poolConfig.setBlockWhenExhausted(true);
		instance = new JedisPool(poolConfig, redisHostname, 6380, 1000, true);
		return instance;
		
	}

	public synchronized static RedisCache getInstance() {
		if(cache!=null)
			return cache;
		cache = new RedisCache();
		return cache;
	}

	public RedisCache() {
	}

	public <T> void setValue(String id, T item) {
		ObjectMapper mapper = new ObjectMapper();
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String cacheId = item.getClass().getSimpleName()+":"+id;
			jedis.set(cacheId, mapper.writeValueAsString(item));
			jedis.expire(cacheId, CACHE_EXPIRATION_TIME);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public <T> T getValue(String id, Class<T> type) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String str = jedis.get(type.getSimpleName()+":" + id);
			ObjectMapper mapper = new ObjectMapper();
			T item = null;
			try {
				item = mapper.readValue(str, type);
			}
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			return item;
		}
	}

	public void putSession(String cookieId, String userId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String cacheId = "session:"+cookieId;
			jedis.set(cacheId, userId);
			jedis.expire(cacheId, SESSION_EXPIRATION_TIME);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public String getSession(String cookieId) {
		try (Jedis jedis = RedisCache.getCachePool().getResource()) {
			String str = jedis.get("session:"+cookieId);
			return str;
		}
	}

	public <T> void add(T item) {
		ObjectMapper mapper = new ObjectMapper();
		String listName = "MostRecent"+item.getClass().getSimpleName();
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			Long cnt = jedis.lpush(listName, mapper.writeValueAsString(item));
			if (cnt > 5)
				jedis.ltrim(listName, 0, 4);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public <T> List<String> list(Class<T> type) {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			List<String> lst = jedis.lrange("MostRecent"+type.getSimpleName(), 0, -1);
			return lst;
		}
	}

	public <T> long incr(Class<T> type) {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			return jedis.incr("Num"+type.getSimpleName());
		}
	}

	public <T> void delete(String id, Class<T> type) {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			jedis.del(type.getSimpleName()+":" + id);
		}
	}

	public void deleteCookie(String cookieId) {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			jedis.del("session:"+cookieId);
		}
	}

	public void incrementLeaderboard(String id) {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			jedis.zincrby("leaderboard", 1, id);
		}
	}

	public String[] getTrendingChannels() {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			return (jedis.zrevrange("leaderboard", 0, 4).toArray(new String[5]));
		}
	}

	public void updateTrending() {
		try (Jedis jedis = RedisCache. getCachePool().getResource()) {
			Set<Tuple> leaderboard = jedis.zrevrangeWithScores("leaderboard", 0, -1);
			for(Tuple entry : leaderboard) {
				jedis.zadd("leaderboard", entry.getScore() / 2, entry.getElement());
			}
		}
	}
}