package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class HabrCareerParse implements DateTimeParser {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES = 5;

    public static void main(String[] args) throws IOException {
        for (int i = 1; i <= PAGES; i++) {
            System.out.printf("====== Page %d ======%n", i);
            String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();

                Element vacancyDate = row.select(".vacancy-card__date").first();
                Element dt = vacancyDate.child(0);
                String t = dt.attr("datetime");
                HabrCareerParse habrCareerParse = new HabrCareerParse();
                LocalDateTime res = habrCareerParse.parse(t);

                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", res, vacancyName, link);
            });
        }
    }

    @Override
    public LocalDateTime parse(String parse) {
        String p = parse.split("\\+")[0];
        return LocalDateTime.parse(p, ISO_LOCAL_DATE_TIME);
    }
}
