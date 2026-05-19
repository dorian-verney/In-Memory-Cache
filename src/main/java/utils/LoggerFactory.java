package utils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoggerFactory {

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    // Cache : one fileHandler per file
    private static final ConcurrentHashMap<String, FileHandler> handlers = new ConcurrentHashMap<>();

    public static Logger create(Class<?> clazz, String filename) {
        Logger logger = Logger.getLogger(clazz.getName());
        logger.setUseParentHandlers(false);

        // reuse the existing handler, or create a new one
        FileHandler fileHandler = handlers.computeIfAbsent(filename, f -> {
            try {
                String path = System.getProperty("user.dir") + "/logs/" + f;
                FileHandler fh = new FileHandler(path, true);
                fh.setFormatter(new SimpleFormatter() {
                    @Override
                    public String format(LogRecord record) {
                        Instant instant = Instant.ofEpochMilli(record.getMillis());
                        String timestamp = DATE_FMT.format(instant);
                        long millis = record.getMillis() % 1000;
                        String blank = " ".repeat(7 - record.getLevel().getName().length());
                        String simpleClass = record.getSourceClassName();
                        return String.format("[%s,%03d] %s%s - %s - %s\n",
                                timestamp, millis,
                                record.getLevel(), blank,
                                simpleClass,
                                record.getMessage()
                        );
                    }
                });
                return fh;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create FileHandler for " + f, e);
            }
        });

        // avoid adding a new handler if the logger has already been init
        if (logger.getHandlers().length == 0) {
            logger.addHandler(fileHandler);
        }

        return logger;
    }
}