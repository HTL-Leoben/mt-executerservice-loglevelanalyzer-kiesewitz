import java.io.File;
import java.util.*;

public class LogCountUtils {

    public static Map<String, Integer> createEmptyLogLevelMap() {
        Map<String, Integer> map = new HashMap<>();
        for (String level : List.of("TRACE", "DEBUG", "INFO", "WARN", "ERROR")) {
            map.put(level, 0);
        }
        return map;
    }

    public static Map<String, Integer> countLogLevels(File file) {
        Map<String, Integer> counts = createEmptyLogLevelMap();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                updateLogCount(line, counts);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counts;
    }

    public static void updateLogCount(String line, Map<String, Integer> counts) {
        for (String level : counts.keySet()) {
            if (line.contains(level)) {
                counts.put(level, counts.get(level) + 1);
            }
        }
    }

    public static void mergeCounts(Map<String, Integer> target, Map<String, Integer> source) {
        for (Map.Entry<String, Integer> entry : source.entrySet()) {
            target.put(entry.getKey(), target.getOrDefault(entry.getKey(), 0) + entry.getValue());
        }
    }

    public static void printErrorLines(List<String> lines) {
        int limit = 5;
        for (int i = 0; i < Math.min(lines.size(), limit); i++) {
            System.out.println(lines.get(i));
        }
        if (lines.size() > limit) {
            System.out.println("... (" + (lines.size() - limit) + " weitere Zeilen)");
        }
    }
}
