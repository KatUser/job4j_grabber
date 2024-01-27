package ru.job4j.grabber;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class HabrCareerParseTest {

    @Test
    public void whenParseAndInstanceOfLocalDateTime() {
        HabrCareerParse habrCareerParse = new HabrCareerParse();
        String testDate = "2024-01-07T15:32:11+03:00";
        assertThat(habrCareerParse.parse(testDate)).isInstanceOf(LocalDateTime.class);
    }

    @Test
    public void whenParsingIsOk() {
        HabrCareerParse habrCareerParse = new HabrCareerParse();
        String testDate = "2024-01-07T15:32:11+03:00";
        String str = "2024-01-07T15:32:11";
        LocalDateTime dateTime = LocalDateTime.parse(str, ISO_LOCAL_DATE_TIME);
        assertThat(habrCareerParse.parse(testDate)).isEqualTo(dateTime);
    }
}