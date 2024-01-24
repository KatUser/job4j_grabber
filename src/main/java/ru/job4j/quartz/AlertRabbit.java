package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {

    public static Connection connection;

    public static void main(String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDetail job = newJob(Rabbit.class)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            try (InputStream input = Rabbit.class
                    .getClassLoader().getResourceAsStream("rabbit.properties")) {
                Properties config = new Properties();
                config.load(input);
                String drivers = config.getProperty("driver");
                String connectionURL = config.getProperty("url");
                String username = config.getProperty("login");
                String password = config.getProperty("password");
                Class.forName(drivers);
                connection = DriverManager.getConnection(connectionURL, username, password);
            } catch (IOException | ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void execute(JobExecutionContext context) {
            Long jobExecutionTime = System.currentTimeMillis();
            try (Statement statement = connection.createStatement()) {
                statement.execute(String.format(
                        "INSERT INTO rabbit VALUES ('%d')",
                        jobExecutionTime));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
}