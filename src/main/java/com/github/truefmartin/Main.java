package com.github.truefmartin;

import com.github.truefmartin.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication
public class Main {
    private static final Logger logger =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }


    @PreDestroy
    public void tearDown() {
        // Clean up resources on shutdown
        logger.info(Main.class.getSimpleName() + ": received SIGTERM.");
        HibernateUtil.close();
        // Flush async logs if needed - current Logback config does not buffer logs
    }

}