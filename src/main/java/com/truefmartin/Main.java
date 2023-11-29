package com.truefmartin;

import com.truefmartin.builder.InvertedFileBuilder;
import com.truefmartin.querier.FileNameRetriever;
import com.truefmartin.querier.Query;
import com.truefmartin.querier.accumulator.CompareType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import javax.annotation.PreDestroy;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
@SpringBootApplication
public class Main {
    private static final Logger logger =
            LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    //    @Bean
//    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
//        return args -> {
//
//            System.out.println("Provided Beans:");
//
//            String[] beanNames = ctx.getBeanDefinitionNames();
//            Arrays.sort(beanNames);
//            for (String beanName : beanNames) {
//                System.out.println(beanName);
//            }
//
//        };
//    }

    @PreDestroy
    public void tearDown() {
        // Clean up resources on shutdown
        logger.info(Main.class.getSimpleName() + ": received SIGTERM.");
        // Flush async logs if needed - current Logback config does not buffer logs
    }

    public static void Build(String[] args) {
        var builder = new InvertedFileBuilder(args);
        builder.begin();
    }

}