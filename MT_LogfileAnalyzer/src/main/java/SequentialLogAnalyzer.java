import java.io.File;
import java.util.*;

public class SequentialLogAnalyzer implements LogAnalyzer {

    @Override
    public void analyze(File[] logFiles) {
        long start = System.currentTimeMillis();
        Map<String, Integer> totalCounts = LogCountUtils.createEmptyLogLevelMap();

        for (File file : logFiles) {
            System.out.println("Analysiere Datei: " + file.getName());
            Map<String, Integer> counts = LogCountUtils.countLogLevels(file);
            LogCountUtils.mergeCounts(totalCounts, counts);
            System.out.println("Ergebnis: " + counts);
        }

        System.out.println("Gesamtergebnis: " + totalCounts);
        System.out.println("Ausführungszeit (sequentiell): " + (System.currentTimeMillis() - start) + " ms");
    }
}
