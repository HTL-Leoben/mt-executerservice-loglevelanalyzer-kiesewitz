import java.io.File;

public class LogFileScanner {
    public static File[] findLogFiles(String path) {
        File dir = new File(path);
        File[] files = dir.listFiles((d, name) -> name.startsWith("app-") && name.endsWith(".log"));
        return files != null ? files : new File[0];
    }
}
