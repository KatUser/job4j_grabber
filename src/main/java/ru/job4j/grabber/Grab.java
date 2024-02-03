package ru.job4j.grabber;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

public interface Grab {
    void init(HabrCareerParse habrCareerParse,
              Store store,
              Scheduler scheduler)
            throws SchedulerException;

}
