package com.sudeepnm.redis.sortedsets.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.sudeepnm.redis.sortedsets.service.RedisUtil;

@SpringBootApplication
public class RedisSortedsetsApplication {

	@Autowired
	private RedisUtil util;
	
	public static void main(String[] args) {
		SpringApplication.run(RedisSortedsetsApplication.class, args);
	}

	/**
	 * This method triggers initialization of the Jedis Pool. Only one Pool will be available
	 * in the life of the running application. 
	 * Running this as @PostConstruct ensures that all the static variables 
	 * needed to initialize the pool are available. 
	 * Triggering the initialization from "main" method will result in Null Pointr
	 * as the static variables would not have been initialized.  
	 */
	@PostConstruct
	private void initializeJedisPool() {
		util.initializeJedisPool();
	}
	
	/**
	 * This method triggers the Jedis Pool closure. 
	 * Running this as @PreDestroy will ensure that the pool is closed after
	 * the application is shut down.
	 */
	@PreDestroy
	private void closeJedisPool() {
		util.destroyJedisPool();
	}
}
