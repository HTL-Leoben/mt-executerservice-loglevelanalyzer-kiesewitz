package com.htl.loganalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * LogAnalyzerTask analysiert eine einzelne Logdatei.
 * Dabei werden:
 * - Die Häufigkeit der LogLevel (TRACE, DEBUG, INFO, WARN, ERROR) gezählt.
 * - Alle Zeilen, die WARN oder ERROR enthalten, extrahiert.
 * - Spezifische Fehlertypen (z.B. NullPointerException, FileNotFoundException, SQLException) in ERROR-Zeilen gezählt.
 */
public class LogAnalyzerTask implements Callable<Map<String, Object>> {

    private final File logFile;

    public LogAnalyzerTask(File logFile) {
        this.logFile = logFile;
    }

    @Override
    public Map<String, Object> call() throws Exception {
        // Initialisiere die Zähler für die LogLevel
        Map<String, Integer> logCounts = new HashMap<>();
        logCounts.put("TRACE", 0);
        logCounts.put("DEBUG", 0);
        logCounts.put("INFO", 0);
        logCounts.put("WARN", 0);
        logCounts.put("ERROR", 0);

        // Liste zum Speichern von WARN- und ERROR-Zeilen
        List<String> errorWarnLines = new ArrayList<>();

        // Map zur Zählung spezifischer Fehlertypen in ERROR-Zeilen
        Map<String, Integer> errorTypes = new HashMap<>();
        List<String> errorKeywords = Arrays.asList("NullPointerException", "FileNotFoundException", "SQLException");

        // Lese die Logdatei zeilenweise ein
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Zähle die Vorkommen der einzelnen LogLevel
                if (line.contains("TRACE")) {
                    logCounts.put("TRACE", logCounts.get("TRACE") + 1);
                }
                if (line.contains("DEBUG")) {
                    logCounts.put("DEBUG", logCounts.get("DEBUG") + 1);
                }
                if (line.contains("INFO")) {
                    logCounts.put("INFO", logCounts.get("INFO") + 1);
                }
                if (line.contains("WARN")) {
                    logCounts.put("WARN", logCounts.get("WARN") + 1);
                    errorWarnLines.add(line);
                }
                if (line.contains("ERROR")) {
                    logCounts.put("ERROR", logCounts.get("ERROR") + 1);
                    errorWarnLines.add(line);
                    // Suche in ERROR-Zeilen nach spezifischen Fehlertypen
                    for (String keyword : errorKeywords) {
                        if (line.contains(keyword)) {
                            errorTypes.put(keyword, errorTypes.getOrDefault(keyword, 0) + 1);
                        }
                    }
                }
            }
        }

        // Ergebnis-Mapping: Enthält LogLevel-Zählungen, extrahierte ERROR/WARN-Zeilen und Fehlertypen-Zählungen
        Map<String, Object> result = new HashMap<>();
        result.put("logCounts", logCounts);
        result.put("errorWarnLines", errorWarnLines);
        result.put("errorTypes", errorTypes);

        return result;
    }
}
