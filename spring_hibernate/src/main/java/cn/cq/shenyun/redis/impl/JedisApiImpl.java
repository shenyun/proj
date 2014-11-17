package cn.cq.shenyun.redis.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.cq.shenyun.redis.JedisApi;
import cn.cq.shenyun.redis.RedisClient;
import cn.cq.shenyun.redis.impl.callback.AbstractJedisCallBack;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

//@Service(value = "jedisCacheApi")
public class JedisApiImpl implements JedisApi {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(JedisApiImpl.class);
	public static final String SEPARATOR = ".";

//	@Value("#{redisProps.redisIndex}")
	private int index = 15;

	public void setIndex(int index) { 
		this.index = index;
	}

//	@Autowired
	private RedisClient client;

	public void setClient(RedisClient client) {
		this.client = client;
	}

	@Override
	public Set<String> keys(final String pattern) {
		return execute(new AbstractJedisCallBack<Set<String>>() {
			@Override
			public Set<String> execute(Jedis jedis) {
				Pipeline pipelined = jedis.pipelined();
				Response<Set<String>> keys = pipelined.keys(pattern);
				pipelined.sync();
				return keys.get();
			}
		});
	}

	
	@Override
	public void rename(final String oldKey, final String newKey) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				jedis.rename(oldKey, newKey);
				return null;
			}
			
		});
		
	}

	@Override
	public boolean exists(final String key) {
		return execute(new AbstractJedisCallBack<Boolean>() {
			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.exists(key);
			}
		});
	}

	@Override
	public Long incrBy(final String key, final int count) {
		return execute(new AbstractJedisCallBack<Long>() {
			@Override
			public Long execute(Jedis jedis) {
				return jedis.incrBy(key, count);
			}
		});
	}

	@Override
	public void hmset(final String key, final Map<String, String> keyValues,
			final String... idNames) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				StringBuffer sb = new StringBuffer(key);
				for (String idName : idNames) {
					sb.append(SEPARATOR).append(keyValues.get(idName));
				}
				for (Map.Entry<String, String> entity : keyValues.entrySet()) {
					if (entity.getValue() == null) {
						entity.setValue("");
					}
				}
				String string = sb.toString();
				if(jedis.exists(string)){
					jedis.del(string);
				}
				jedis.hmset(string, keyValues);
				return null;
			}
		});
	}

	@Override
	public void hmsets(final String key,
			final List<Map<String, String>> keyValues, final String... idNames) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				for (Map<String, String> map : keyValues) {
					StringBuffer sb = new StringBuffer(key);
					for (String idName : idNames) {
						sb.append(SEPARATOR).append(map.get(idName));
					}
					for (Map.Entry<String, String> entity : map.entrySet()) {
						if (entity.getValue() == null) {
							entity.setValue("");
						}
					}
					String string = sb.toString();
					if(jedis.exists(string)){
						jedis.del(string);
					}
					jedis.hmset(string, map);
				}
				return null;
			}
		});
	}

	@Override
	public void hmget(final String key, final Map<String, String> keyValues,
			final String... idNames) {
		execute(new AbstractJedisCallBack<Map<String, String>>() {
			@Override
			public Map<String, String> execute(Jedis jedis) {
				StringBuffer sb = new StringBuffer(key);
				for (String idName : idNames) {
					sb.append(SEPARATOR).append(keyValues.get(idName));
				}
				String[] array = keyValues.keySet().toArray(new String[0]);
				List<String> hmget = jedis.hmget(sb.toString(), array);
				for (int i = 0; i < array.length; i++) {
					keyValues.put(array[i], hmget.get(i));
				}
				return keyValues;
			}
		});
	}
	
	@Override
	public String hget(final String key,final String field){
		return execute(new AbstractJedisCallBack<String>() {
			@Override
			public String execute(Jedis jedis) {
				if(jedis.hexists(key, field)){
					return jedis.hget(key, field);
				}else{
					return "";
				}
			}
		});
	}
	
	public void hset(final String key,final String field,final String value){
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				if(value == null){
					jedis.hset(key, field, "");
				}else{
					jedis.hset(key, field, value);
				}
				
				return null;
			}
		});
	}

	@Override
	public void hmgets(final String key,
			final List<Map<String, String>> keyValues, final String... idNames) {
		execute(new AbstractJedisCallBack<List<Map<String, String>>>() {
			@Override
			public List<Map<String, String>> execute(Jedis jedis) {
				Map<String,Response<List<String>>> keyResponseMap = new HashMap<String, Response<List<String>>>();
				Map<String,Map<String,String>> keyRowMap = new HashMap<String,Map<String,String>>();
				Pipeline pipelined = jedis.pipelined();
				for (Map<String, String> map : keyValues) {
					StringBuffer sb = new StringBuffer(key);
					for (String idName : idNames) {
						sb.append(SEPARATOR).append(map.get(idName));
					}
					String string = sb.toString();
					String[] array = map.keySet().toArray(new String[0]);
					keyResponseMap.put(string,pipelined.hmget(string, array));
					keyRowMap.put(string, map);
				}
				pipelined.sync();
				for(Map.Entry<String,Map<String,String>> en:keyRowMap.entrySet()){
					Map<String,String> map = en.getValue();
					String[] array = map.keySet().toArray(new String[0]);
					List<String> list = keyResponseMap.get(en.getKey()).get();
					for (int i = 0; list!=null&&i < array.length; i++) {
						map.put(array[i], list.get(i));
					}
				}
				return keyValues;
			}
		});
	}

	@Override
	public void deletes(final Collection<String> keys) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				return jedis.del(keys.toArray(new String[0]));
			}
		});
	}

	@Override
	public void delete(final String key) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				return jedis.del(key);
			}
		});
	}

	@Override
	public void hdel(final String key, final String... fields) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				int i = 0;
				for (String field : fields) {
					i += jedis.hdel(key, field);
				}
				return i;
			}
		});
	}

	@Override
	public String get(final String key) {
		return execute(new AbstractJedisCallBack<String>() {
			@Override
			public String execute(Jedis jedis) {
				return jedis.get(key);
			}
		});
	}

	@Override
	public Map<String, String> gets(final String... keys) {
		return execute(new AbstractJedisCallBack<Map<String, String>>() {
			@Override
			public Map<String, String> execute(Jedis jedis) {
				Pipeline pipelined = jedis.pipelined();
				Map<String,Response<String>> keyResponseMap = new HashMap<String, Response<String>>();
				Map<String, String> row = new HashMap<String, String>();
				for (String key : keys) {
					keyResponseMap.put(key, pipelined.get(key));
				}
				pipelined.sync();
				for(Map.Entry<String,Response<String>> en:keyResponseMap.entrySet()){
					String val = en.getValue().get();
					if(val!=null){
						row.put(en.getKey(), val);
					}
				}
				return row;
			}
		});
	}
	
	@Override
	public Map<String, Map<String, String>> hgets(final String... keys) {
		return execute(new AbstractJedisCallBack<Map<String, Map<String, String>>>() {
			@Override
			public Map<String, Map<String, String>> execute(Jedis jedis) {
				Pipeline pipelined = jedis.pipelined();
				Map<String,Response<Map<String, String>>> keyResponseMap = new HashMap<String, Response<Map<String, String>>>();
				Map<String, Map<String, String>> row = new HashMap<String, Map<String, String>>();
				for (String key : keys) {
					keyResponseMap.put(key, pipelined.hgetAll(key));
				}
				pipelined.sync();
				for(Map.Entry<String,Response<Map<String, String>>> en:keyResponseMap.entrySet()){
					Map<String, String> map = en.getValue().get();
					if(map!=null){
						row.put(en.getKey(), map);
					}
				}
				return row;
			}
		});
	}

	@Override
	public Map<String, String> getRow(final String table,
			final String... idValues) {
		return execute(new AbstractJedisCallBack<Map<String, String>>() {
			@Override
			public Map<String, String> execute(Jedis jedis) {
				StringBuffer sb = new StringBuffer(table);
				for (String value : idValues) {
					sb.append(SEPARATOR).append(value);
				}
				String string = sb.toString();
				return jedis.exists(string)?jedis.hgetAll(string):null;
			}
		});
	}

	@Override
	public List<Map<String, String>> getRows(final String table,
			final List<String[]> listIdValues) {
		return execute(new AbstractJedisCallBack<List<Map<String, String>>>() {
			@Override
			public List<Map<String, String>> execute(Jedis jedis) {
				Pipeline pipelined = jedis.pipelined();
				List<Response<Map<String, String>>> rowResponse = new LinkedList<Response<Map<String, String>>>();
				List<Map<String, String>> rows = new LinkedList<Map<String, String>>();
				Response<Map<String, String>> row = null;
				for (String[] idValues : listIdValues) {
					StringBuffer sb = new StringBuffer(table);
					for (String value : idValues) {
						sb.append(SEPARATOR).append(value);
					}
					String key = sb.toString();
					row = pipelined.hgetAll(key);
					rowResponse.add(row);
				}
				pipelined.sync();
				for(Response<Map<String, String>> rowR:rowResponse){
					Map<String, String> map = rowR.get();
					if(map!=null){
						rows.add(map);
					}
				}
				return rows;
			}
		});
	}

	@Override
	public void zadd(final String key, final double score, final String value) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				return jedis.zadd(key, score, value);
			}
		});
		
		
	}
	
	@Override
	public void zrem(final String key,final String value) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				return jedis.zrem(key, value);
			}
		});

	}
	
	

	@Override
	public void zadds(final String key, final String scorePropertyName,
			final String valuePropertyName, final List<Map<String, String>> rows) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				int i = 0;
				for (Map<String, String> row : rows) {
					try {
						i += jedis.zadd(key,
								Double.valueOf(row.get(scorePropertyName)),
								row.get(valuePropertyName));
					} catch (NumberFormatException e) {
					}
				}
				return i;
			}
		});

	}

	@Override
	public List<Map<String, String>> zRevRange(final String key,
			final String tableName, final int begin, final int end) {
		return execute(new AbstractJedisCallBack<List<Map<String, String>>>() {
			@Override
			public List<Map<String, String>> execute(Jedis jedis) {
				Set<String> ids = jedis.zrevrange(key, begin, end);
				List<Map<String, String>> rows = new LinkedList<Map<String, String>>();
				for (String idValues : ids) {
					Map<String, String> row = getRow(tableName, new String[]{idValues});
					rows.add(row);
				}
				return rows;
			}
		});
	}

	@Override
	public boolean expire(final String key, final long seconds) {
		return execute(new AbstractJedisCallBack<Boolean>() {
			@Override
			public Boolean execute(Jedis jedis) {
				return jedis.expire(key, (int) seconds) > 0;
			}
		});
	}

	@Override
	public void hmsetsMap(final String key,
			final List<Map<String, String>> keyValues, final String... idNames) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				for (Map<String, String> map : keyValues) {
					StringBuffer sb = new StringBuffer(key);
					for (String idName : idNames) {
						sb.append(SEPARATOR).append(map.get(idName));
					}
					for (Map.Entry<String, String> entity : map.entrySet()) {
						if (entity.getValue() == null) {
							entity.setValue("");
						}
					}
					jedis.hmset(sb.toString(), map);
				}
				return null;
			}
		});
	}

	@Override
	public void set(final String key, final String value) {
		execute(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				return jedis.set(key, value);
			}
		});
	}

	@Override
	public <T> T done(AbstractJedisCallBack<T> cmd){
		return execute(cmd);
	}
	
	private <T> T execute(AbstractJedisCallBack<T> cmd) {
		Jedis jedis = null;
		T t = null;
		boolean isSuccess = false;
		// 如果执行出现异常，重新执行，最多不超过maxTimes次
		int i = 0;
		int maxTimes = 5;
		for (; i < maxTimes && !isSuccess;) {
			try {
				t = null;
				jedis = client.getJedis(index);
				t = cmd.execute(jedis);
				isSuccess = true;
			} catch (Exception e) {
				i++;
				isSuccess = false;
				String message = e.getMessage();
				LOGGER.warn(i > 0 ? "重试:" + i + ":" + message : message);
				if (jedis != null) {
					client.returnBrokenResource(jedis);
					jedis = null;
				}
			} finally {
				if (jedis != null) {
					client.returnResource(jedis);
				}
			}
		}
		if (i == maxTimes) {
			throw new RuntimeException("数据处理失败，请检查redis");
		}
		return t;
	}

}
