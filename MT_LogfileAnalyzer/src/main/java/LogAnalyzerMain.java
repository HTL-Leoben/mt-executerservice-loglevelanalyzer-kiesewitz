import java.io.File;
import java.util.*;
import java.util.concurrent.*;

/**
 * LogAnalyzerMain führt die sequentielle sowie die parallele Analyse der Logdateien durch.
 * Die Logdateien liegen im Projektstammverzeichnis und haben die Endung ".log".
 */
public class LogAnalyzerMain {

    public static void main(String[] args) {
        File logDir = new File("."); // Aktuelles Verzeichnis
        File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("app-") && name.endsWith(".log"));

        if (logFiles == null || logFiles.length == 0) {
            System.out.println("Keine Logdateien im angegebenen Verzeichnis gefunden.");
            return;
        }

        System.out.println("Sequentielle Analyse:");
        sequentialAnalysis(logFiles);

        System.out.println("\nParallele Analyse:");
        parallelAnalysis(logFiles);
    }

    /**
     * Führt die sequentielle Analyse der Logdateien durch.
     * Jede Datei wird einzeln gelesen und die LogLevel werden gezählt.
     */
    private static void sequentialAnalysis(File[] logFiles) {
        long startTime = System.currentTimeMillis();

        // Gesamte Zähler für alle LogLevel initialisieren
        Map<String, Integer> overallCounts = new HashMap<>();
        overallCounts.put("TRACE", 0);
        overallCounts.put("DEBUG", 0);
        overallCounts.put("INFO", 0);
        overallCounts.put("WARN", 0);
        overallCounts.put("ERROR", 0);

        // Analysiere jede Logdatei sequentiell
        for (File file : logFiles) {
            System.out.println("Analysiere Datei: " + file.getName());
            Map<String, Integer> fileCounts = analyzeFile(file);
            System.out.println("Ergebnis: " + fileCounts);
            // Aggregiere die Ergebnisse
            for (Map.Entry<String, Integer> entry : fileCounts.entrySet()) {
                overallCounts.put(entry.getKey(), overallCounts.get(entry.getKey()) + entry.getValue());
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Gesamtergebnis: " + overallCounts);
        System.out.println("Ausführungszeit (sequentiell): " + (endTime - startTime) + " ms");
    }

    /**
     * Liest eine einzelne Logdatei ein und zählt die Vorkommen der LogLevel.
     *
     * @param file Logdatei, die analysiert wird
     * @return Map mit Zählungen für TRACE, DEBUG, INFO, WARN und ERROR
     */
    private static Map<String, Integer> analyzeFile(File file) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("TRACE", 0);
        counts.put("DEBUG", 0);
        counts.put("INFO", 0);
        counts.put("WARN", 0);
        counts.put("ERROR", 0);

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.contains("TRACE")) counts.put("TRACE", counts.get("TRACE") + 1);
                if (line.contains("DEBUG")) counts.put("DEBUG", counts.get("DEBUG") + 1);
                if (line.contains("INFO")) counts.put("INFO", counts.get("INFO") + 1);
                if (line.contains("WARN")) counts.put("WARN", counts.get("WARN") + 1);
                if (line.contains("ERROR")) counts.put("ERROR", counts.get("ERROR") + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counts;
    }

    /**
     * Führt die parallele Analyse der Logdateien durch.
     * Die einzelnen LogAnalyzerTasks werden einem Fixed-Thread-Pool (ExecutorService) übergeben,
     * wodurch kein manueller Umgang mit Threads nötig ist.
     */
    private static void parallelAnalysis(File[] logFiles) {
        long startTime = System.currentTimeMillis();

        // Erzeuge einen Fixed-Thread-Pool – hier so viele Threads wie Prozessorkerne verfügbar sind.
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        // Reiche für jede Logdatei einen LogAnalyzerTask beim Executor ein.
        for (File file : logFiles) {
            com.htl.loganalysis.LogAnalyzerTask task = new com.htl.loganalysis.LogAnalyzerTask(file);
            Future<Map<String, Object>> future = executor.submit(task);
            futures.add(future);
        }

        // Aggregiere die Ergebnisse
        Map<String, Integer> overallCounts = new HashMap<>();
        overallCounts.put("TRACE", 0);
        overallCounts.put("DEBUG", 0);
        overallCounts.put("INFO", 0);
        overallCounts.put("WARN", 0);
        overallCounts.put("ERROR", 0);

        Map<String, Integer> overallErrorTypes = new HashMap<>();

        // Verarbeite die Ergebnisse der parallelen Aufgaben
        for (Future<Map<String, Object>> future : futures) {
            try {
                Map<String, Object> result = future.get();
                @SuppressWarnings("unchecked")
                Map<String, Integer> fileCounts = (Map<String, Integer>) result.get("logCounts");
                @SuppressWarnings("unchecked")
                List<String> errorWarnLines = (List<String>) result.get("errorWarnLines");
                @SuppressWarnings("unchecked")
                Map<String, Integer> errorTypes = (Map<String, Integer>) result.get("errorTypes");

                System.out.println("Ergebnisse für Datei: ");
                System.out.println("LogLevel-Zählungen: " + fileCounts);
                System.out.println("Extrahierte ERROR/WARN-Zeilen (max. 5 Zeilen):");
                int maxLines = 5;
                for (int i = 0; i < Math.min(errorWarnLines.size(), maxLines); i++) {
                    System.out.println(errorWarnLines.get(i));
                }
                if (errorWarnLines.size() > maxLines) {
                    System.out.println("... (" + (errorWarnLines.size() - maxLines) + " weitere Zeilen)");
                }
                System.out.println("Fehlertypen-Zählungen: " + errorTypes);
                System.out.println("----------------------------------");

                // Aggregiere die LogLevel-Zählungen
                for (Map.Entry<String, Integer> entry : fileCounts.entrySet()) {
                    overallCounts.put(entry.getKey(), overallCounts.get(entry.getKey()) + entry.getValue());
                }
                // Aggregiere die Fehlertypen-Zählungen
                for (Map.Entry<String, Integer> entry : errorTypes.entrySet()) {
                    overallErrorTypes.put(entry.getKey(), overallErrorTypes.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Gesamtergebnis LogLevel: " + overallCounts);
        System.out.println("Gesamtergebnis Fehlertypen: " + overallErrorTypes);
        System.out.println("Ausführungszeit (parallel): " + (endTime - startTime) + " ms");

        // ExecutorService sauber herunterfahren
        executor.shutdown();
    }
}
