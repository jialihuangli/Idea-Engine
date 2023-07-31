package sample;

import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class focusTimer {

    public ScheduledExecutorService executor;
    public int timeCounted = 0; /// counts time tabbed out **workaround to use only 1 counter

    public ScheduledExecutorService setUpTimer(Stage window) {

        Runnable addSecond = () -> {
            if (window.isFocused()) {
                timeCounted = timeCounted + 1;
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(0);
        executor.scheduleAtFixedRate(addSecond, 0, 1, TimeUnit.SECONDS);

        return this.executor;
    }

    public void resetTime() {
        this.timeCounted = 0;
    }

    public int getTimeElapsed() {
        return this.timeCounted;
    }
}
