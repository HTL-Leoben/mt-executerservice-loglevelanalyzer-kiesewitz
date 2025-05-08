import java.util.*;

public class LogAnalysisResult {
    private final Map<String, Integer> logCounts;
    private final List<String> errorWarnLines;
    private final Map<String, Integer> errorTypes;

    public LogAnalysisResult(Map<String, Integer> logCounts, List<String> errorWarnLines, Map<String, Integer> errorTypes) {
        this.logCounts = logCounts;
        this.errorWarnLines = errorWarnLines;
        this.errorTypes = errorTypes;
    }

    public Map<String, Integer> getLogCounts() {
        return logCounts;
    }

    public List<String> getErrorWarnLines() {
        return errorWarnLines;
    }

    public Map<String, Integer> getErrorTypes() {
        return errorTypes;
    }
}
