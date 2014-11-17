package cn.cq.shenyun.redis.cache;


import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import cn.cq.shenyun.redis.JedisApi;
import cn.cq.shenyun.redis.impl.callback.AbstractJedisCallBack;


public class CacheInterceptor implements MethodInterceptor {
	
	protected Log logger = LogFactory.getLog(this.getClass());
	
	private JedisApi jedisApi;
	public JedisApi getJedisApi() {
    	return jedisApi;
    }

	public void setJedisApi(JedisApi jedisApi) {
    	this.jedisApi = jedisApi;
    }
	private Gson gson;

	public CacheInterceptor() {
		gson = new Gson();
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		// TODO Auto-generated method stub
		System.out.println("切面begin");
		//获取接口方法
		Method i_m = invocation.getMethod();
		//获取实现类
		Class<?> bean_class=invocation.getThis().getClass();
		String sinple_class_name=bean_class.getSimpleName();
		Method m=bean_class.getMethod(i_m.getName(), i_m.getParameterTypes());
		
//		Object[] agr=invocation.getArguments();
//		AccessibleObject df=invocation.getStaticPart();
		
		Cache cache = m.getAnnotation(Cache.class);
		
		Object obj = null;
		// 没有定义缓存，直接返回
		if (null == cache || cache.value() == 0) {
			obj = invocation.proceed();
			return obj;
		}
		
		// 获取缓存的key
		String key = getCacheKey(sinple_class_name,
		                         m.getName(),
		                         invocation.getArguments());

		String cacheValue = getCacheValue(key);
		// 如果未命中缓存
		if (null == cacheValue) {
			obj = invocation.proceed(); // 方法的原始业务逻辑
			// 缓存指定时间
			putCacheValue(key, obj, cache.value());
			logger.info("存入缓存key="+key);
		}else{//命中
			try {
				obj=gson.fromJson(cacheValue, m.getReturnType());
				System.out.println("命中目标key="+key+"");
				logger.info("命中目标key="+key+"");
            }
            catch (Exception e) {
	            logger.error("json解析redis数据报错");
	            //再取一次数据
	            obj = invocation.proceed(); // 方法的原始业务逻辑
				putCacheValue(key, obj, cache.value());
            }
		}
		return obj;
	}

	/**
	 * 拼接缓存的 key，包括 前缀 + 类名 + 方法名 + 参数的 hashCode
	 */
	private String getCacheKey(String className,
	                           String methodName,
	                           Object[] args) {
		StringBuffer sbs = new StringBuffer("cache_");
		sbs.append(className).append(".").append(methodName).append(".");

		StringBuffer argsbs = new StringBuffer();
		for (Object o : args) {
			// 将每个参数转换为 json 串，再取 hashcode ，已解决参数一样，缓存的 key 必须一样的问题
			argsbs.append(gson.toJson(o).hashCode());
		}

		sbs.append(argsbs.toString().hashCode());
		return sbs.toString();
	}

	/**
	 * 从分布式cache中（redis、ssdb）获取缓存对象
	 */
	private String getCacheValue(final String key) {
		try {
			String ret = jedisApi.get(key);
			return ret;
		}
		catch (Exception e) {
			return null;
		}
	}
	/**
	 * 向分布式cache中（redis、ssdb）写入缓存对象
	 * 
	 * @param key 缓存的key
	 * @param value	缓存的数据
	 * @param seconds	缓存的时间，以秒为单位
	 */
	private void putCacheValue(final String key,
	                           final Object value,
	                           final int seconds) {
		jedisApi.done(new AbstractJedisCallBack<Object>() {
			@Override
			public Object execute(Jedis jedis) {
				try {
					logger.info("数据，key="+key+",val="+gson.toJson(value)+",seconds="+seconds);
					String aa=jedis.setex(key, seconds, gson.toJson(value));
					System.out.println("test="+aa);
					return null;
                }
                catch (Exception e) {
                	logger.error("写入缓存 "+key+" 失败!"+e.getLocalizedMessage(),e);
	                // TODO: handle exception
                }
				return null;
			}
		});
	}

}
