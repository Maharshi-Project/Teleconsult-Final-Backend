package com.teleconsulting.demo.service;

import com.teleconsulting.demo.redis.UserSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, UserSession> redisTemplate;

    public void saveSession(String username, UserSession session) {
        redisTemplate.opsForValue().set(username, session);
    }

    public UserSession getSession(String username) {
        return redisTemplate.opsForValue().get(username);
    }

    public void deleteSession(String username) {
        if(username != null)
            redisTemplate.delete(username);
    }

    
}
