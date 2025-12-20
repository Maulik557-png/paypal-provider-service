package com.hulkhiretech.payments.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class RedisService {

	@SuppressWarnings("unused")
	private final RedisTemplate<String, String> redisTemplate;
	private final ListOperations<String, String> listOperations;
	private final HashOperations<String, String, String> hashOperations;
	private final ValueOperations<String, String> valueOperations;


	public RedisService(RedisTemplate<String, String> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.listOperations = redisTemplate.opsForList();
		this.hashOperations = redisTemplate.opsForHash();
		this.valueOperations = redisTemplate.opsForValue();
	}

	public void addValueToList(String key, String value) {
		log.info("Adding value to Redis list||key: {}||value: {}", key, value);
		listOperations.rightPush(key, value);
	}

	public List<String> getAllValuesFromList(String key) {
		log.info("Retrieving all values from Redis list||key: {}", key);
		return listOperations.range(key, 0, -1);
	}

	public void setValueInHash(String hashName, String key, String value) {
		log.info("Setting value in Redis hash||hashName: {}||key: {}||value: {}", hashName, key, value);
		hashOperations.put(hashName, key, value);
	}

	public String getValueFromHash(String hashName, String key) {
		log.info("Getting value from Redis hash||hashName: {}||key: {}", hashName, key);
		return hashOperations.get(hashName, key);
	}

	public Map<String, String> getAllEntriesFromHash(String hashName) {
		log.info("Getting all entries from Redis hash||hashName: {}", hashName);
		return hashOperations.entries(hashName);
	}

	public void setValue(String key, String value) {
		log.info("Setting value in Redis||key: {}||value: {}", key, value);
		valueOperations.set(key, value);
	}

	//setValue with expiry
	public void setValueWithExpiry(String key, String value, long timeoutInSecs) {
		log.info("Setting value in Redis with expiry||key: {}||value: {}||timeoutInSecs: {}", key, value, timeoutInSecs);
		valueOperations.set(key, value, timeoutInSecs, TimeUnit.SECONDS);
	}

	public String getValue(String key) {
		log.info("Getting value from Redis||key: {}", key);
		return valueOperations.get(key);
	}
}

