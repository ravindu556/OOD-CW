package teammate;

import java.util.logging.*;
import java.io.IOException;

    public class AppLogger {
        private static FileHandler fileHandler;

        static {
            try {
                new java.io.File("logs").mkdirs();
                fileHandler = new FileHandler("logs/system.log", true);
                fileHandler.setFormatter(new SimpleFormatter());

                Logger rootLogger = Logger.getLogger("");
                rootLogger.setLevel(Level.ALL);

                for (Handler handler : rootLogger.getHandlers()) {
                    rootLogger.removeHandler(handler);
                }



                rootLogger.addHandler(fileHandler);

            } catch (IOException e) {
                System.err.println("Failed to initialize logger: " + e.getMessage());
            }
        }

        public static Logger getLogger(Class<?> clazz) {
            Logger logger = Logger.getLogger(clazz.getName());

            if (fileHandler != null) {
                boolean exists = false;
                for (Handler handler : logger.getHandlers()) {
                    if (handler instanceof FileHandler) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) logger.addHandler(fileHandler);
            }

            logger.setUseParentHandlers(false);
            return logger;
        }
    }

