package cn.cq.shenyun.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis客户端类
 * @author Administrator
 *
 */
public class RedisClient {

	/**
	 * 配置信息
	 * @author Administrator
	 *
	 */
	public static class JedisClientConfig {
		protected JedisPoolConfig config;
		protected String address;
		protected int port;
		protected int timeout;
		protected String auth;

		public JedisClientConfig(JedisPoolConfig config, String address,
				int port, int timeout, String auth) {
			this.config = config;
			this.address = address != null && address.length() > 0 ? address
					: null;
			this.port = port > 0 ? port : 6375;
			this.timeout = timeout > 0 ? timeout : 5000;
			this.auth = auth != null && auth.length() > 0 ? auth
					: null;
		}
	}

	private static Logger logger = LoggerFactory.getLogger(RedisClient.class);

	/**
	 * 连接池
	 */
	private JedisPool jedisPool;

	/**
	 * 取得连接
	 * @param dbIndex
	 * @return
	 */
	public Jedis getJedis(int dbIndex) {
		Jedis jedis = jedisPool.getResource();
		jedis.select(dbIndex);
		return jedis;
	}

	private RedisClient() {}

	/**
	 * 初始化连接池
	 */
	public void init() {
		initJedisClient();
	}

	/**
	 * 连接配置信息
	 */
	private JedisClientConfig config;
	public void setConfig(JedisClientConfig config) {
		this.config = config;
	}

	/**
	 * 初始化连接池
	 */
	private void initJedisClient() {
		try {
			if (jedisPool == null) {
				JedisPoolConfig config = new JedisPoolConfig();
				config.setMaxActive(100);
				config.setMaxIdle(5);
				config.setMaxWait(1000);
				config.setTestOnBorrow(true);
				config = this.config.config;
				int port = this.config.port;
				int timeout = this.config.timeout;
				String address = this.config.address;
				String auth = this.config.auth;
				jedisPool = new JedisPool(config, address, port, timeout,
						auth);
			}
		} catch (IllegalStateException ise) {
			logger.error("init jedis client...connect to REDIS ERROR:"
					+ ise.getMessage());
		}
	}

	/**
	 * 归还连接
	 * @param resource
	 */
	public void returnResource(Jedis resource) {
		jedisPool.returnResource(resource);
	}

	/**
	 * 归还出了异常的连接
	 * @param resource
	 */
	public void returnBrokenResource(Jedis resource) {
		jedisPool.returnBrokenResource(resource);
	}

	/**
	 * 销毁释放资源
	 */
	public void destroy() {
		if (jedisPool != null) {
			jedisPool.destroy();
			jedisPool = null;
		}
		logger.info("destoryed RedisClient.");
	}

	/**
	 * 工厂方创建客户端
	 * @return
	 */
	public static synchronized RedisClient getInstance() {
		return new RedisClient();
	}
}
