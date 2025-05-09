package com.example.crawling.service;

import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlingService {

    private final S3Client s3Client;

    public List<List<String>> getKospiData(int page) throws IOException {
        List<List<String>> kospiDataList = new ArrayList<>();
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

        for (Element row : rows) {
            // 데이터가 있는 행만 처리 (빈 행 제외)
            if (row.childrenSize() > 1) {
                List<String> stockInfo = new ArrayList<>();
                // 종목명 (<a> 태그 안의 텍스트)
                Element nameElement = row.selectFirst("td:nth-child(2) a");
                String name = (nameElement != null) ? nameElement.text().trim() : "";
                stockInfo.add(name);

                // 현재가
                Element currentPriceElement = row.selectFirst("td:nth-child(3)");
                String currentPrice = (currentPriceElement != null) ? currentPriceElement.text().trim() : "";
                stockInfo.add(currentPrice);


                // 전일비
                Element yesterdayPriceElement = row.selectFirst("td:nth-child(4)");
                String yesterdayPrice = (yesterdayPriceElement != null) ? yesterdayPriceElement.text().trim() : "";
                stockInfo.add(yesterdayPrice);

                // 등락률
                Element changeRateElement = row.selectFirst("td:nth-child(5) span");
                String changeRate = (changeRateElement != null) ? changeRateElement.text().trim() : "";
                stockInfo.add(changeRate);

                // 액면가
                Element faceElement = row.selectFirst("td:nth-child(6)");
                String face = (faceElement != null) ? faceElement.text().trim() : "";
                stockInfo.add(face);

                // 시가총액
                Element capitalizationElement = row.selectFirst("td:nth-child(7)");
                String capitalization = (capitalizationElement != null) ? capitalizationElement.text().trim() : "";
                stockInfo.add(capitalization);

                // 상장주식수
                Element NumberElement = row.selectFirst("td:nth-child(8)");
                String Number = (NumberElement != null) ? NumberElement.text().trim() : "";
                stockInfo.add(Number);

                // 외국인비율
                Element foreignerElement = row.selectFirst("td:nth-child(9)");
                String foreigner = (foreignerElement != null) ? foreignerElement.text().trim() : "";
                stockInfo.add(foreigner);

                // 거래량
                Element volumeElement = row.selectFirst("td:nth-child(10)");
                String volume = (volumeElement != null) ? volumeElement.text().trim() : "";
                stockInfo.add(volume);

                // PER
                Element perElement = row.selectFirst("td:nth-child(11)");
                String per = (perElement != null) ? perElement.text().trim() : "";
                stockInfo.add(per);

                // ROE
                Element roeElement = row.selectFirst("td:nth-child(12)");
                String roe = (roeElement != null) ? roeElement.text().trim() : "";
                stockInfo.add(roe);

                // 토론실
                Element urlElement = row.selectFirst("td.center a");
                String href = (urlElement != null) ? "https://finance.naver.com/"+urlElement.attr("href"): "";
                stockInfo.add(href);

                kospiDataList.add(stockInfo);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        String fileName = formatter.format(now) + "kospi_Data_" + page + ".csv";
        String filePath = "C:\\project\\kospi";
        File csvFile = new File(filePath, fileName);
        CSVWriter csvWrite = null;

        try {

            csvFile.createNewFile();
            csvWrite = new CSVWriter(new FileWriter(csvFile));

            // 헤더 쓰기
            csvWrite.writeNext(headers.toArray(new String[0]));

            // 데이터 쓰기 (예시)
            for (List<String> rowData : kospiDataList) {
                csvWrite.writeNext(rowData.toArray(new String[0])); // String 배열로 변환하여 쓰기
            }

            System.out.println("CSV 파일이 성공적으로 생성되었습니다: " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("CSV 파일 생성 중 오류가 발생했습니다: " + e.getMessage());
        } finally {
            // CSVWriter를 닫아 리소스를 해제합니다.
            if (csvWrite != null) {
                try {
                    csvWrite.close();
                    s3Upload(csvFile);
                } catch (IOException e) {
                    System.err.println("CSVWriter를 닫는 중 오류가 발생했습니다: " + e.getMessage());
                }
            }
        }

        log.info("{} 페이지에서 {}개의 코스피 종목 데이터 추출", page, kospiDataList.size());
        return kospiDataList;
    }

    private void s3Upload(File file){
        String key ="data/" + file.getName();
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket("kospidata")
                .key(key)
                .build();
        s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));

    }



}
