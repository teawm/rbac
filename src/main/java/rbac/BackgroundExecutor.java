package rbac;

import java.util.concurrent.*;

public class BackgroundExecutor {
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduledExecutor;

    public BackgroundExecutor() {
        this.executor = Executors.newFixedThreadPool(4);
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    public void execute(Runnable task) {
        executor.execute(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executor.submit(task);
    }

    public void scheduleAtFixedRate(Runnable task, long initialDelay, long period) {
        scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executor.shutdown();
        scheduledExecutor.shutdown();
    }

    public void shutdownAndWait(long timeout) throws InterruptedException {
        executor.shutdown();
        scheduledExecutor.shutdown();

        if (!executor.awaitTermination(timeout, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        if (!scheduledExecutor.awaitTermination(timeout, TimeUnit.SECONDS)) {
            scheduledExecutor.shutdownNow();
        }
    }
}