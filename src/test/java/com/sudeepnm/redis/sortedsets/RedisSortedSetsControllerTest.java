/**
 * 
 */
package com.sudeepnm.redis.sortedsets;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.sudeepnm.redis.sortedsets.service.RedisSortedSetsController;
import com.sudeepnm.redis.sortedsets.service.RedisUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Sudeep
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class RedisSortedSetsControllerTest {

	@Mock
	private RedisUtil util;
	
	@Mock
	private JedisPool pool;
	
	@Mock
	private Jedis jedis;
	
	@InjectMocks
	private RedisSortedSetsController controller;

	/**
	 * Test method for {@link com.sudeepnm.redis.sortedsets.service.RedisSortedSetsController#insertData()}.
	 */
	@Test
	public void testInsertData() {
		Mockito.when(util.getJedisPool()).thenReturn(pool);
		Mockito.when(pool.getResource()).thenReturn(jedis);
		Mockito.when(jedis.zadd(Mockito.anyString(), Mockito.anyDouble(), 
				Mockito.anyString())).thenReturn(new Long(1));
		String returnMsg = controller.insertData();
		Assert.assertEquals("Data did not get inserted", "Data Inserted", returnMsg);
	}

	/**
	 * Test method for {@link com.sudeepnm.redis.sortedsets.service.RedisSortedSetsController#insertData()}.
	 */
	@Test
	public void testInsertDataUpdate() {
		Mockito.when(util.getJedisPool()).thenReturn(pool);
		Mockito.when(pool.getResource()).thenReturn(jedis);
		Mockito.when(jedis.zadd(Mockito.anyString(), Mockito.anyDouble(), 
				Mockito.anyString())).thenReturn(new Long(0));
		String returnMsg = controller.insertData();
		Assert.assertEquals("Data did not get updated", "Data Updated", returnMsg);
	}
	
	/**
	 * Test method for {@link com.sudeepnm.redis.sortedsets.service.RedisSortedSetsController#insertData()}.
	 */
	@Test
	public void testInsertDataFailure() {
		Mockito.when(util.getJedisPool()).thenReturn(pool);
		Mockito.when(pool.getResource()).thenReturn(jedis);
		Mockito.when(jedis.zadd(Mockito.anyString(), Mockito.anyDouble(), 
				Mockito.anyString())).thenReturn(new Long(-1));
		String returnMsg = controller.insertData();
		Assert.assertEquals("Data did not get updated", "failure", returnMsg);
	}
	
	/**
	 * Test method for {@link com.sudeepnm.redis.sortedsets.service.RedisSortedSetsController#getRate(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetRate() {
		Mockito.when(util.getJedisPool()).thenReturn(pool);
		Mockito.when(pool.getResource()).thenReturn(jedis);
		Set<String> dataSet = new HashSet<>();
		dataSet.add("3.00");
		Mockito.when(jedis.zrevrangeByScore(Mockito.anyString(), Mockito.anyDouble(), 
				Mockito.anyDouble())).thenReturn(dataSet);
		
		String rate = controller.getRate("MD", "20170301");
		Assert.assertEquals("Unexpected Rate", "3.00", rate);
	}

}
