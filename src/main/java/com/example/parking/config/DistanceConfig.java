package com.example.parking.config;

import com.example.parking.util.DistanceEntry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DistanceConfig {
    @Bean
    public Map<String, Integer> distanceMap() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream is = new ClassPathResource("data/parking_distances.json").getInputStream();

        List<DistanceEntry> entries = mapper.readValue(is, new TypeReference<>() {
        });
        Map<String, Integer> distanceMap = new HashMap<>();
        for (DistanceEntry entry : entries) {
            String key = entry.getGateNumber() + "_" + entry.getSlotNumber();
            distanceMap.put(key, entry.getDistance());
        }

        return distanceMap;
    }
}
