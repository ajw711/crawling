package com.example.crawling.controller;

import com.example.crawling.dto.CrawlingDTO;
import com.example.crawling.service.CrawlingService;
import com.example.crawling.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CrawlingController {

    private final CrawlingService crawlingService;
    private final KafkaService kafkaService;

    @GetMapping("/crawling1/{page}")
    public List<List<String>> crawling1(@PathVariable int page) throws IOException {
        return  crawlingService.getKospiData(page);
    }

    @GetMapping("/crawling2/{page}")
    public List<CrawlingDTO> crawling2(@PathVariable int page) throws IOException {
        return kafkaService.crawl(page);
    }
}
