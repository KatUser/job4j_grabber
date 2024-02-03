package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private final static String HABR_LINK = "https://career.habr.com";

    public Grabber() {

    }

    private Properties getConfig() {
        Properties properties = new Properties();
        try (InputStream reader = Grabber.class
                .getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private Scheduler scheduler() {
        Scheduler scheduler = null;
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return scheduler;
    }

    private Store store() {
        return new PsqlStore(getConfig());
    }

    private Parse parse() {
        return new HabrCareerParse(new HabrCareerDateTimeParser());
    }

    @Override
    public void init(HabrCareerParse habrCareerParse,
                     Store store,
                     Scheduler scheduler)
            throws SchedulerException {
        JobDataMap data = new JobDataMap();
        data.put("store", store());
        data.put("parse", parse());
        JobDetail job = newJob(GrabJob.class)
                .usingJobData(data)
                .build();
        SimpleScheduleBuilder times = simpleSchedule()
                .withIntervalInSeconds(Integer
                        .parseInt(getConfig().getProperty("time")))
                .repeatForever();
        Trigger trigger = newTrigger()
                .startNow()
                .withSchedule(times)
                .build();
        scheduler.scheduleJob(job, trigger);
    }

    public static class GrabJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {
            JobDataMap map = context.getJobDetail().getJobDataMap();
            Store store = (Store) map.get("store");
            Parse parse = (Parse) map.get("parse");
            try {
                List<Post> postList = parse.list(HABR_LINK);
                postList.forEach(store::save);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(
                    Integer.parseInt(getConfig().getProperty("port")))) {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();
                    try (OutputStream outputStream = socket.getOutputStream()) {
                        outputStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            outputStream.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            outputStream.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        Grabber grab = new Grabber();
        Scheduler scheduler = grab.scheduler();
        scheduler.start();
        Store store = grab.store();
        grab.init(new HabrCareerParse(new HabrCareerDateTimeParser()), store, scheduler);
        grab.web(store);
    }
}
