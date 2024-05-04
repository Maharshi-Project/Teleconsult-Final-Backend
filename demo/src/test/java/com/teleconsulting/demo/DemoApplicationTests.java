package com.teleconsulting.demo;

import org.apache.el.stream.StreamELResolverImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
class DemoApplicationTests {

	@Autowired
	private RedisTemplate<String,String> redisTemplate;

	@Test
	void contextLoads() {
		redisTemplate.opsForValue().set("Guru","RAJ");
		Object ans = redisTemplate.opsForValue().get("Guru");

	}

}
