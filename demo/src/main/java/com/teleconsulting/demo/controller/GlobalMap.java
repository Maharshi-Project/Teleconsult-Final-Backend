package com.teleconsulting.demo.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class GlobalMap {
    // Static map to store key-value pairs
    static Map<AbstractMap.SimpleEntry<Long, AbstractMap.SimpleEntry<LocalDate, LocalTime>>, Boolean> globalMap = new HashMap<>();

    // Other methods and logic...
}
