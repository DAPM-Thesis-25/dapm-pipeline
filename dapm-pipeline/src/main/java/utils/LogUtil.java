package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private static final Logger logger = LoggerFactory.getLogger(LogUtil.class);

    public static void error(Throwable t, String message, Object... args) {
        if (args == null || args.length == 0) {
            logger.error(message, t);
        } else {
            Object[] newArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[args.length] = t;
            logger.error(message, newArgs);
        }
    }


    public static void info(String message, Object... args) {
        logger.info(message, args);
    }

    public static void debug(String message, Object... args) {
        logger.debug(message, args);
    }
}


