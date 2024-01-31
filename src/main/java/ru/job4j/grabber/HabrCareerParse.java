package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class HabrCareerParse implements Parse {
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }


    public static void main(String[] args) throws IOException {
        HabrCareerParse habrCareerParse = new HabrCareerParse(new DateTimeParser());
        habrCareerParse.list(SOURCE_LINK);
    }

    public LocalDateTime parseDate(String parse) {
        String p = parse.split("\\+")[0];
        return LocalDateTime.parse(p, ISO_LOCAL_DATE_TIME);
    }

    private String retrieveDescription(String link) throws IOException { /*linkToVacancy*/
        Connection connection = Jsoup.connect(link);
        Document document = connection.get();
        Elements description = document.select(".vacancy-description__text");
        return description.text();
    }

    @Override
    public List<Post> list(String link) throws IOException {
        int pageNumber = 2;
        int vacancyNumber = 0;
        List<Post> postList = new ArrayList<>();
        for (int i = 1; i <= pageNumber; i++) {
            String fullLink = "%s%s%d%s".formatted(link, PREFIX, i, SUFFIX);
            Connection connection = Jsoup.connect(fullLink);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            for (Element row : rows) {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();

                Element vacancyDate = row.select(".vacancy-card__date").first();
                Element dateTime = vacancyDate.child(0);
                String time = dateTime.attr("datetime");

                HabrCareerParse habrCareerParse = new HabrCareerParse(dateTimeParser);
                LocalDateTime localDateTime = habrCareerParse.parseDate(time);

                String  vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                String vacancyDescription;
                try {
                    vacancyDescription = habrCareerParse.retrieveDescription(vacancyLink);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                postList.add(new Post(
                        ++vacancyNumber,
                        vacancyName,
                        vacancyLink,
                        vacancyDescription,
                        localDateTime
                ));
            }
        }
        return postList;
    }
}
