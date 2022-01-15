package fr.thomah.roger.common;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Predicate;
import java.util.function.Supplier;

@Slf4j
public class MethodPoller<T> {

    private Duration pollDuration;
    private int pollIntervalMillis;

    private Supplier<T> pollMethod = null;
    private Predicate<T> pollResultPredicate = null;

    public MethodPoller() {
    }

    public MethodPoller<T> poll(Duration pollDuration, int pollIntervalMillis) {
        this.pollDuration = pollDuration;
        this.pollIntervalMillis = pollIntervalMillis;
        return this;
    }

    public MethodPoller<T> method(Supplier<T> supplier) {
        pollMethod = supplier;
        return this;
    }

    public MethodPoller<T> until(Predicate<T> predicate) {
        pollResultPredicate = predicate;
        return this;
    }

    public T execute() {
        T result = null;
        boolean pollSucceeded = false;
        LocalTime beginTime = LocalTime.now();
        LocalTime now = LocalTime.now();
        Duration currentDuration = Duration.between(beginTime, now);
        try {
            while (!pollSucceeded && currentDuration.compareTo(pollDuration) < 0) {
                result = pollMethod.get();
                pollSucceeded = pollResultPredicate.test(result);
                now = LocalTime.now();
                currentDuration = Duration.between(beginTime, now);

                log.debug("Result : " + result);
                log.debug("Poll Succeeded : " + pollSucceeded);
                log.debug("Duration : " + currentDuration.getSeconds());

                Thread.sleep(pollIntervalMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}