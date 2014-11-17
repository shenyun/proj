package cn.cq.shenyun.redis;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.cq.shenyun.redis.impl.callback.AbstractJedisCallBack;




/**
 * 缓存接口，简单抽象
 * @author yq
 *
 */
public interface JedisApi {

	/**
	 * 取得指定模式的所有键
	 * @param pattern
	 * @return
	 */
	public Set<String> keys(String pattern);
	
	public void rename(final String oldKey, final String newKey);

	/**
	 * 保存给定的值
	 * hmset key+"."+".".join(idNames),{key1:value1,key2:value2...}
	 * @param key
	 * @param keyValus
	 * @param idNames
	 */
	public void hmset(final String key, final Map<String, String> keyValus, final String... idNames);

	public void hmsets(final String key, final List<Map<String, String>> keyValus, final String... idNames);

	public void hmget(final String key, final Map<String, String> keyValues, final String... idNames);

	public void hmgets(final String key, final List<Map<String, String>> keyValues, final String... idNames);

	/**
	 * 删除指定的键值对
	 * @param keys
	 */
	public void deletes(Collection<String> keys);

	public void delete(String key);

	public void hdel(String key, String... fields);

	/**
	 * 取得指定的键值
	 * @param key
	 * @return
	 */
	public String get(String key);

	public Map<String,String> gets(String... keys);

	public Map<String,String> getRow(String table, String... idValues);

	public List<Map<String,String>> getRows(String table, List<String[]> listIdValues);

	/**
	 * 保存给定的值,并安sroce排序
	 * @param key
	 * @param score
	 * @param value
	 */
	public void zadd(String key, double score, String value);

	/**
	 * 取得排序后的给定范围的值
	 * @param key
	 * @param tableName
	 * @param begin
	 * @param end
	 * @return
	 */
	public List<Map<String,String>> zRevRange(String key, String tableName, int begin, int end);

	/**
	 * 指定给定键的过期时间
	 * @param key
	 * @param seconds
	 * @return
	 */
	public boolean expire(String key, long seconds);

	public void hmsetsMap(final String key, final List<Map<String, String>> keyValus, final String... idNames);

	public abstract void zadds(final String key, final String scorePropertyName, final String valuePropertyName,
							   final List<Map<String, String>> rows);

	public Long incrBy(final String pattern, final int count);

	public boolean exists(final String key);

	public abstract void set(final String key, final String value);

	public abstract String hget(final String key, final String field);

	/**
	 * 自定义操作
	 * @param <T>
	 * @param cmd
	 * @return
	 */
	public <T> T done(AbstractJedisCallBack<T> cmd);

	public void zrem(final String key, final String value);

	public Map<String, Map<String, String>> hgets(final String... keys);

}

