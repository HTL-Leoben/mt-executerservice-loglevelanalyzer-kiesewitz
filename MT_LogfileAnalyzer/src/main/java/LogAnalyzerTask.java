import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<LogAnalysisResult> {
    private final File file;

    public LogAnalyzerTask(File file) {
        this.file = file;
    }

    @Override
    public LogAnalysisResult call() throws Exception {
        Map<String, Integer> logCounts = LogCountUtils.createEmptyLogLevelMap();
        Map<String, Integer> errorTypes = new HashMap<>();
        List<String> errorWarnLines = new ArrayList<>();

        List<String> errorKeywords = Arrays.asList("NullPointerException", "FileNotFoundException", "SQLException");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogCountUtils.updateLogCount(line, logCounts);
                if (line.contains("WARN") || line.contains("ERROR")) {
                    errorWarnLines.add(line);
                }
                if (line.contains("ERROR")) {
                    for (String keyword : errorKeywords) {
                        if (line.contains(keyword)) {
                            errorTypes.put(keyword, errorTypes.getOrDefault(keyword, 0) + 1);
                        }
                    }
                }
            }
        }

        return new LogAnalysisResult(logCounts, errorWarnLines, errorTypes);
    }
}
