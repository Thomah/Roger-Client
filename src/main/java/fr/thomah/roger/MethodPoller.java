package fr.thomah.roger;

import java.time.Duration;
import java.time.LocalTime;
import java.util.function.Predicate;
import java.util.function.Supplier;

class MethodPoller<T> {

    private Duration pollDuration;
    private int pollIntervalMillis;

    private Supplier<T> pollMethod = null;
    private Predicate<T> pollResultPredicate = null;

    MethodPoller() {
    }

    MethodPoller<T> poll(Duration pollDuration, int pollIntervalMillis) {
        this.pollDuration = pollDuration;
        this.pollIntervalMillis = pollIntervalMillis;
        return this;
    }

    MethodPoller<T> method(Supplier<T> supplier) {
        pollMethod = supplier;
        return this;
    }

    MethodPoller<T> until(Predicate<T> predicate) {
        pollResultPredicate = predicate;
        return this;
    }

    T execute() {
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

                System.out.println("Result : " + result);
                System.out.println("Poll Succeeded : " + pollSucceeded);
                System.out.println("Duration : " + currentDuration.getSeconds());

                Thread.sleep(pollIntervalMillis);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }
}