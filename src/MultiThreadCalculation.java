public class MultiThreadCalculation {

    private static final int THREAD_COUNT = 10;
    private static final int CALCULATION_LENGTH = 50;
    private static final int DELAY_MS = 100;
    private static final Object outputLock = new Object();

    public static void pause(long milliseconds) {
        long endTime = System.currentTimeMillis() + milliseconds;
        while (System.currentTimeMillis() < endTime) {
            // 'while' statement has empty body, но здесь мой комментарий (зачем-то)
        }
    }

    public static void main(String[] args) {

        System.out.print("\033[2J\033[H");
        System.out.flush();

        System.out.println("[Имитация многопоточного расчета.]\n");
        pause(500);
        System.out.print("\033[" + (THREAD_COUNT + 4) + ";1H");
        System.out.println("[Расчеты в процессе...]");
        System.out.println("\033[2;6H");
        pause(500);
        int[] progress = new int[THREAD_COUNT];
        long[] threadIds = new long[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.printf("  Поток #%-2d | ID: -- | [%s]%n",
                    i + 1, createEmptyProgressBar());
        }
        pause(500);

        Thread[] threads = new Thread[THREAD_COUNT];

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadIndex = i;
            final int threadNum = i + 1;

            threads[i] = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                long threadId = Thread.currentThread().threadId();

                synchronized (outputLock) {
                    threadIds[threadIndex] = threadId;
                }

                for (int pos = 1; pos <= CALCULATION_LENGTH; pos++) {
                    try {
                        Thread.sleep(DELAY_MS);
                    } catch (InterruptedException e) {
                        System.err.println("Поток " + threadNum + " прерван");
                        Thread.currentThread().interrupt();
                        return;
                    }

                    synchronized (outputLock) {
                        progress[threadIndex] = pos;

                        int lineNumber = 3 + threadIndex;
                        System.out.print("\033[" + lineNumber + ";1H");

                        String progressBar = createProgressBar(pos);

                        System.out.printf("  Поток #%-2d | ID: %d | [%s]%s",
                                threadNum, threadId, progressBar,
                                "\033[K");
                        System.out.flush();
                    }
                }

                long totalTime = System.currentTimeMillis() - startTime;

                synchronized (outputLock) {
                    int lineNumber = 3 + threadIndex;
                    System.out.print("\033[" + lineNumber + ";1H");

                    String progressBar = createProgressBar(CALCULATION_LENGTH);
                    System.out.printf("  Поток #%-2d | ID: %d | [%s] | Время: %d мс%s",
                            threadNum, threadId, progressBar, totalTime,
                            "\033[K");
                    System.out.flush();
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Основной поток прерван");
                Thread.currentThread().interrupt();
            }
        }

        pause(500);
        System.out.print("\033[" + (THREAD_COUNT + 4) + ";1H");
        System.out.println("[Все расчеты завершены.]");
    }

    private static String createProgressBar(int current) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CALCULATION_LENGTH; i++) {
            sb.append(i < current ? "|" : "-");
        }
        return sb.toString();
    }

    private static String createEmptyProgressBar() {
        return "-".repeat(CALCULATION_LENGTH);
    }
}