package org.example;

// different log level
// singleton - LogService
// chain of responsibility

import java.time.Clock;
import java.time.Instant;

enum LogLevel {
    INFO(1),
    DEBUG(2),
    ERROR(3);

    private final int priority;
    LogLevel(int priority) {
        this.priority = priority;
    }
    public int getPriority() {
        return priority;
    }
}

interface LogWriter {
    void write(LogLevel logLevel, String time, String message);
}

class ConsoleLogWriter implements LogWriter {
    @Override
    public void write(LogLevel logLevel, String time, String message) {
        System.out.println(time + " " + logLevel.name() + " :: " + message);
    }
}

abstract class LogLevelHandler {
    protected LogLevelHandler nextLoggerLevel;
    protected final Clock clock;
    protected final LogWriter logWriter;

    LogLevelHandler(Clock clock, LogWriter logWriter) {
        this.clock = clock;
        this.logWriter = logWriter;
    }

    public void setNextLoggerLevel(LogLevelHandler nextLoggerLevel) {
        this.nextLoggerLevel = nextLoggerLevel;
    }

    abstract void process(LogLevel configuredLevel, LogLevel logLevel, String message);

}

class InfoLogLevelHandler extends LogLevelHandler {


    InfoLogLevelHandler(Clock clock, LogWriter logWriter) {
        super(clock, logWriter);
    }

    @Override
    void process(LogLevel configuredLevel, LogLevel logLevel, String message) {
        if(logLevel.getPriority() >= configuredLevel.getPriority() && logLevel.equals(LogLevel.INFO)) {
            this.logWriter.write(logLevel, String.valueOf(Instant.now(clock)), message);
        }
        if(this.nextLoggerLevel != null) {
            this.nextLoggerLevel.process(configuredLevel, logLevel, message);
        }
    }
}

class DebugLogLevelHandler extends LogLevelHandler {

    DebugLogLevelHandler(Clock clock, LogWriter logWriter) {
        super(clock, logWriter);
    }

    @Override
    void process(LogLevel configuredLevel, LogLevel logLevel, String message) {
        if(logLevel.getPriority() >= configuredLevel.getPriority() && logLevel.equals(LogLevel.DEBUG)) {
            this.logWriter.write(logLevel, String.valueOf(Instant.now(clock)), message);
        }
        if(this.nextLoggerLevel != null) {
            this.nextLoggerLevel.process(configuredLevel, logLevel, message);
        }
    }
}

class ErrorLogLevelHandler extends LogLevelHandler {

    ErrorLogLevelHandler(Clock clock, LogWriter logWriter) {
        super(clock, logWriter);
    }

    @Override
    void process(LogLevel configuredLevel, LogLevel logLevel, String message) {
        if(logLevel.getPriority() >= configuredLevel.getPriority() && logLevel.equals(LogLevel.ERROR)) {
            this.logWriter.write(logLevel, String.valueOf(Instant.now(clock)), message);
        }
        if(this.nextLoggerLevel != null) {
            this.nextLoggerLevel.process(configuredLevel, logLevel, message);
        }
    }
}

class LoggerService {
    private static volatile LoggerService instance = null;
    private final LogLevelHandler logLevelHandler;
    private final LogLevel configuredLevel;

    private LoggerService(LogLevelHandler logLevelHandler, LogLevel configuredLevel){
        this.configuredLevel = configuredLevel;
        this.logLevelHandler = logLevelHandler;
    }

    public static LoggerService getInstance(LogLevelHandler logLevelHandler, LogLevel configuredLevel) {
        if(instance == null) {
            synchronized (LoggerService.class) {
                if(instance == null) {
                    instance = new LoggerService(logLevelHandler, configuredLevel);
                }
            }
        }
        return instance;
    }

    public void log(LogLevel logLevel, String message) {
        this.logLevelHandler.process(configuredLevel, logLevel, message);
    }

}

public class Main {
    public static void main(String[] args) {
        Clock clock = Clock.systemUTC();
        LogWriter logWriter = new ConsoleLogWriter();

        LogLevelHandler error = new ErrorLogLevelHandler(clock, logWriter);
        LogLevelHandler debug = new DebugLogLevelHandler(clock, logWriter);
        LogLevelHandler info = new InfoLogLevelHandler(clock, logWriter);

        error.setNextLoggerLevel(debug);
        debug.setNextLoggerLevel(info);

        LoggerService loggerService = LoggerService.getInstance(error, LogLevel.INFO);

        loggerService.log(LogLevel.ERROR, "Something went wrong!!");
        loggerService.log(LogLevel.INFO, "this works!!");

    }
}