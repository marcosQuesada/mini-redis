package com.marcosquesada.miniredis;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.marcosquesada.miniredis.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.lang.System.exit;

public class Main
{
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        configureLogger();

        Server server = new Server(8080);

        logger.info("Starting Server");
        server.start();

        waitUntilKeypressed();

        logger.info("Closing Server");
        server.terminate();

        exit(0);
    }

    private static void waitUntilKeypressed() {
        try {
            System.in.read();
            while (System.in.available() > 0) {
                System.in.read();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void configureLogger(){
        try {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure("logback.xml");
        } catch (JoranException je) {
            logger.error("Unexpected exception configuring log4j, err {}", je.getMessage());
        }

        System.setProperty("logback.configurationFile", "/logback.xml");
    }
}
