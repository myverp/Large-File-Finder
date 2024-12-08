import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;

public class LargeFileFinder {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Введення шляху до папки
        System.out.print("Enter the directory path: ");
        String directoryPath = scanner.nextLine();

        // Введення порогового розміру файлу (у байтах)
        System.out.print("Enter the file size threshold (in bytes): ");
        long fileSizeThreshold = scanner.nextLong();

        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.out.println("The provided path is not a valid directory.");
            return;
        }

        // Виконання задачі за допомогою Fork/Join Framework
        long startTimeForkJoin = System.currentTimeMillis();
        int fileCountForkJoin = countLargeFilesForkJoin(directory, fileSizeThreshold);
        long endTimeForkJoin = System.currentTimeMillis();
        System.out.println("\nLarge Files Found (Fork/Join): " + fileCountForkJoin);
        System.out.println("Execution Time (Fork/Join): " + (endTimeForkJoin - startTimeForkJoin) + " ms");

        // Виконання задачі за допомогою ThreadPool
        long startTimeThreadPool = System.currentTimeMillis();
        int fileCountThreadPool = countLargeFilesThreadPool(directory, fileSizeThreshold);
        long endTimeThreadPool = System.currentTimeMillis();
        System.out.println("\nLarge Files Found (ThreadPool): " + fileCountThreadPool);
        System.out.println("Execution Time (ThreadPool): " + (endTimeThreadPool - startTimeThreadPool) + " ms");
    }

    // Розрахунок кількості файлів за допомогою Fork/Join Framework
    public static int countLargeFilesForkJoin(File directory, long sizeThreshold) {
        ForkJoinPool pool = new ForkJoinPool();
        return pool.invoke(new FileSearchTask(directory, sizeThreshold));
    }

    // Клас для Fork/Join Task
    static class FileSearchTask extends RecursiveTask<Integer> {
        private final File directory;
        private final long sizeThreshold;

        public FileSearchTask(File directory, long sizeThreshold) {
            this.directory = directory;
            this.sizeThreshold = sizeThreshold;
        }

        @Override
        protected Integer compute() {
            int count = 0;
            File[] files = directory.listFiles();
            if (files == null) return 0;

            // Завдання для піддиректорій
            List<FileSearchTask> subTasks = new ArrayList<>();
            for (File file : files) {
                if (file.isDirectory()) {
                    subTasks.add(new FileSearchTask(file, sizeThreshold));
                } else if (file.length() > sizeThreshold) {
                    count++;
                }
            }

            // Виконати підзадачі паралельно
            invokeAll(subTasks);

            // Збираємо результати
            for (FileSearchTask task : subTasks) {
                count += task.join();
            }

            return count;
        }
    }

    // Розрахунок кількості файлів за допомогою ThreadPool
    public static int countLargeFilesThreadPool(File directory, long sizeThreshold) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        BlockingQueue<Future<Integer>> results = new LinkedBlockingQueue<>();
        int count = 0;

        try {
            // Початкова задача
            results.add(executor.submit(new FileSearchWorker(directory, sizeThreshold)));

            // Обробка результатів
            while (!results.isEmpty()) {
                Future<Integer> future = results.poll();
                if (future != null) {
                    count += future.get();
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        return count;
    }

    // Клас для ThreadPool Worker
    static class FileSearchWorker implements Callable<Integer> {
        private final File directory;
        private final long sizeThreshold;

        public FileSearchWorker(File directory, long sizeThreshold) {
            this.directory = directory;
            this.sizeThreshold = sizeThreshold;
        }

        @Override
        public Integer call() {
            int count = 0;
            File[] files = directory.listFiles();
            if (files == null) return 0;

            for (File file : files) {
                if (file.isDirectory()) {
                    count += new FileSearchWorker(file, sizeThreshold).call();
                } else if (file.length() > sizeThreshold) {
                    count++;
                }
            }
            return count;
        }
    }
}
