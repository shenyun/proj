package cn.cq.shenyun.redis.impl.callback;

import redis.clients.jedis.Jedis;

public abstract class AbstractJedisCallBack<T> {

	public abstract T execute(Jedis jedis);
	
}