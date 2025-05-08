import java.io.File;

public class LogAnalyzerController {
    private final File[] logFiles;

    public LogAnalyzerController(File[] logFiles) {
        this.logFiles = logFiles;
    }

    public void runAnalysis() {
        System.out.println("Sequentielle Analyse:");
        new SequentialLogAnalyzer().analyze(logFiles);

        System.out.println("\nParallele Analyse:");
        new ParallelLogAnalyzer().analyze(logFiles);
    }
}
