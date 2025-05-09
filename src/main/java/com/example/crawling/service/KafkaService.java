package com.example.crawling.service;

import com.example.crawling.dto.CrawlingDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaService {

    private static final String TOPIC = "my-topic";
    private final KafkaTemplate<String, CrawlingDTO> kafkaTemplate;

    public List<CrawlingDTO> crawl(int page) throws IOException {

        String url = "https://finance.naver.com/sise/sise_market_sum.naver?&page=" + page;
        Document doc = Jsoup.connect(url).get();
        log.info("크롤링 URL: {}", url);
        log.info("페이지 제목: {}", doc.title());

        Elements headerElements = doc.select("table.type_2 thead tr th");
        Elements rows = doc.select("table.type_2 tbody tr");
        List<String> headers = new ArrayList<>();
        for (Element th : headerElements) {
            String text = th.text().trim();
            // 불필요한 툴팁 텍스트 제거 (예: "N")
            if (!text.isEmpty() && !text.equals("N")) {
                headers.add(text);
            }
        }
        List<CrawlingDTO> crawlingDTO = new ArrayList<>();
        for (Element row : rows) {
            // 데이터가 있는 행만 처리 (빈 행 제외)
            if (row.childrenSize() > 1) {
                CrawlingDTO stockInfo = new CrawlingDTO();
                // 종목명 (<a> 태그 안의 텍스트)
                Element nameElement = row.selectFirst("td:nth-child(2) a");
                String name = (nameElement != null) ? nameElement.text().trim() : "";
                stockInfo.setName(name);

                // 현재가
                Element currentPriceElement = row.selectFirst("td:nth-child(3)");
                String currentPrice = (currentPriceElement != null) ? currentPriceElement.text().trim() : "";
                stockInfo.setCurrentPrice(currentPrice);


                // 전일비
                Element yesterdayPriceElement = row.selectFirst("td:nth-child(4)");
                String yesterdayPrice = (yesterdayPriceElement != null) ? yesterdayPriceElement.text().trim() : "";
                stockInfo.setYesterdayPrice(yesterdayPrice);

                // 등락률
                Element changeRateElement = row.selectFirst("td:nth-child(5) span");
                String changeRate = (changeRateElement != null) ? changeRateElement.text().trim() : "";
                stockInfo.setChangeRate(changeRate);

                // 액면가
                Element faceElement = row.selectFirst("td:nth-child(6)");
                String face = (faceElement != null) ? faceElement.text().trim() : "";
                stockInfo.setFace(face);

                // 시가총액
                Element capitalizationElement = row.selectFirst("td:nth-child(7)");
                String capitalization = (capitalizationElement != null) ? capitalizationElement.text().trim() : "";
                stockInfo.setCapitalization(capitalization);

                // 상장주식수
                Element NumberElement = row.selectFirst("td:nth-child(8)");
                String Number = (NumberElement != null) ? NumberElement.text().trim() : "";
                stockInfo.setNumber(Number);

                // 외국인비율
                Element foreignerElement = row.selectFirst("td:nth-child(9)");
                String foreigner = (foreignerElement != null) ? foreignerElement.text().trim() : "";
                stockInfo.setForeigner(foreigner);

                // 거래량
                Element volumeElement = row.selectFirst("td:nth-child(10)");
                String volume = (volumeElement != null) ? volumeElement.text().trim() : "";
                stockInfo.setVolume(volume);

                // PER
                Element perElement = row.selectFirst("td:nth-child(11)");
                String per = (perElement != null) ? perElement.text().trim() : "";
                stockInfo.setPer(per);

                // ROE
                Element roeElement = row.selectFirst("td:nth-child(12)");
                String roe = (roeElement != null) ? roeElement.text().trim() : "";
                stockInfo.setRoe(roe);

                // 토론실
                Element urlElement = row.selectFirst("td.center a");
                String href = (urlElement != null) ? "https://finance.naver.com/"+urlElement.attr("href"): "";
                stockInfo.setHref(href);


                kafkaTemplate.send(TOPIC, stockInfo);
            }
        }

        return crawlingDTO;
    }
}
