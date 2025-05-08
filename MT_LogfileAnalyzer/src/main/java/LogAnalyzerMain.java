import java.io.File;

public class LogAnalyzerMain {
    public static void main(String[] args) {
        File[] logFiles = LogFileScanner.findLogFiles(".");
        if (logFiles.length == 0) {
            System.out.println("Keine Logdateien gefunden.");
            return;
        }

        LogAnalyzerController controller = new LogAnalyzerController(logFiles);
        controller.runAnalysis();
    }
}
