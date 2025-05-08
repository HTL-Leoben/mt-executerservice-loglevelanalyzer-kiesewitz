import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class ParallelLogAnalyzer implements LogAnalyzer {

    @Override
    public void analyze(File[] logFiles) {
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        List<Future<LogAnalysisResult>> futures = new ArrayList<>();
        for (File file : logFiles) {
            futures.add(executor.submit(new LogAnalyzerTask(file)));
        }

        Map<String, Integer> totalCounts = LogCountUtils.createEmptyLogLevelMap();
        Map<String, Integer> totalErrorTypes = new HashMap<>();

        for (Future<LogAnalysisResult> future : futures) {
            try {
                LogAnalysisResult result = future.get();
                LogCountUtils.mergeCounts(totalCounts, result.getLogCounts());
                LogCountUtils.mergeCounts(totalErrorTypes, result.getErrorTypes());

                System.out.println("LogLevel-Zählungen: " + result.getLogCounts());
                LogCountUtils.printErrorLines(result.getErrorWarnLines());
                System.out.println("Fehlertypen: " + result.getErrorTypes());
                System.out.println("----------------------------------");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        System.out.println("Gesamtergebnis LogLevel: " + totalCounts);
        System.out.println("Gesamtergebnis Fehlertypen: " + totalErrorTypes);
        System.out.println("Ausführungszeit (parallel): " + (System.currentTimeMillis() - start) + " ms");
    }
}
