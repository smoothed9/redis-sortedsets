/**
 * 
 */
package com.sudeepnm.redis.sortedsets.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * Controller class that declares the WebService operations
 * @author Sudeep
 *
 */
@RestController
@RequestMapping("/sortedsets")
public class RedisSortedSetsController {

	private static Logger log = LoggerFactory.getLogger(RedisSortedSetsController.class);
	// Key is formed as TABLE_NAME + |. Usage of | symbol is purely personal choice
	private static final String TABLE_KEY = "RateByStateAndDate|"; 
			
	@Autowired
	private RedisUtil util;
	
	private JedisPool jedisPool = null;
	
	/**
	 * Operation to test the Redis connection
	 * @return {@link String} Connection Status
	 */
	@RequestMapping(path="/testConnection")
	public String testConnection() {
		String message = null;
		jedisPool = util.getJedisPool();
		try (Jedis jedis = jedisPool.getResource()){
			if(jedis.isConnected()) {
				message = "connection successful";
			} else {
				message = "connection failed";
			}
		} catch (Exception e) {
			message = "connection failed";
		}
		
		return message;
	}
	
	/**
	 * Operation to insert the data. In this example, I have written
	 * the method to read from a static file.  
	 * @return {@link String} Data Insertion Status
	 */
	@RequestMapping(path="/insertData")
	public String insertData() {
		String key = null;
		String returnMsg = null;
		List<LoanData> loanDataList = null;
		Long returnCode = new Long(-1);
		
		try {
			loanDataList = readInputData();
		} catch (IOException e) {
			returnMsg = "Unable to read input file";
		}
		
		jedisPool = util.getJedisPool();
		try (Jedis jedis = jedisPool.getResource()) {
			for (LoanData data : loanDataList) {
				// Key is created by appending the Table Name and state separated by | 
				key = TABLE_KEY+data.getState();
				/*
				 * zadd method adds saves the data as a Sorted Set. 
				 * Score is mandatory for a sorted set and Redis uses the score to sort multiple 
				 * values for the same key. In this example, I've used the Double representation
				 * of Effective Date in yyyyMMdd format as the score. This ensures that when a
				 * query is execute with the effective date that falls between two entries in the 
				 * database, Redis returns the correct record. For more details, read
				 * @see https://redis.io/commands/zadd, 
				 * @see https://github.com/xetorthio/jedis/blob/master/src/main/java/redis/clients/jedis/Jedis.java
				 */
				returnCode = jedis.zadd(key, new Double(data.getEffDt()), data.getRate().toString());
				log.info("Data Inserted/Updated Successfully for "+data.toString());
			}
		}
		if(returnCode == 1) {
			returnMsg = "Data Inserted";
		} else if (returnCode == 0) {
			returnMsg = "Data Updated";
		} else {
			returnMsg = "failure";
		}
		return returnMsg;
	}
	
	/**
	 * Retrieves interest rate effective for the given date in the given State 
	 * @param state
	 * @param effDt
	 * @return {@link String} Interest rate retrieved from Redis
	 */
	@RequestMapping(path="/getRate")
	public String getRate(@RequestParam(name="state") String state, @RequestParam(name="effDt") String effDt) {
		jedisPool = util.getJedisPool();
		try (Jedis jedis = jedisPool.getResource()) {
			// Key is created by appending the Table Name and state separated by | 
			String key = TABLE_KEY+state;
			/*
			 * zrevrangeByScore method accepts a key, score and minimum value. There are several variations of 
			 * this method. For this example, we need to retrieve only a single row, the rate effective on the
			 * given effective date for that State. 
			 * This method can be used only to retrieve data that was stored with a Score. If the data was 
			 * stored as a pure key-value pair, trying to retrieve with this method will result in error. 
			 * The minimum value is given as negative infinity to cover any potential negative values in the 
			 * score
			 */
			Set<String> rateSet = jedis.zrevrangeByScore(key, new Double(effDt), Double.NEGATIVE_INFINITY);
			// Retrieved the first value since we are expecting only one row
			return rateSet.iterator().next();
		}
		
	}

	/**
	 * Method to read the Loan Data stored in a csv file format and populate LoanData object list
	 * @return {@link List<LoanData>} List of LoanData objects populated based on the data in csv
	 * @throws IOException
	 */
	private List<LoanData> readInputData() throws IOException {
		List<LoanData> loanDataList = new ArrayList<>();
		LoanData data = null;
		boolean header = true;
		String line = null;
		String[] rows = null;
		
		try (BufferedReader br = new BufferedReader(new InputStreamReader(
				this.getClass().getClassLoader().getResourceAsStream("rate_data.csv")))) {
			while ((line = br.readLine()) != null) {
				if (header) {
					// The first row is considered as a header and should be ignored
					header = false;
					continue;
				} else {
					rows = line.split(",");
					data = new LoanData(rows[0], rows[1], rows[2]);
					loanDataList.add(data);
				}
			}
		} catch (IOException e) {
			log.error("Exception while reading the input file", e.getMessage());
			throw e;
		}
		return loanDataList;
	}
}
