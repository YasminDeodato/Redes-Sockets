import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LogManagement {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE_PATH = "log" + DATE_FORMAT.format(new Date()) + ".txt";
    public static String message = "";

    public static void executeLogManagement() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable logTask = () -> {
            try {
                String timestamp = DATE_FORMAT.format(new Date());
                writeLog();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        // a tarefa para ser executada a cada 1 minuto
        scheduler.scheduleAtFixedRate(logTask, 0, 30, TimeUnit.SECONDS);
    }

    private static void writeLog() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.write(message);
            writer.newLine();
        }
    }
}
