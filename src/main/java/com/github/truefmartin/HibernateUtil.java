package com.github.truefmartin;

import com.github.truefmartin.api.MicroserviceController;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    /**
     * Builds a new Hibernate SessionFactory.
     * @throws HibernateException if there is a problem creating the SessionFactory
     */
    private static void buildSession() throws HibernateException {
        String url = System.getenv("DATABASE_URL");
        if (url == null) {
            throw new HibernateException("DATABASE_URL is not set");
        }
        String username = System.getenv("DATABASE_USER");
        if (username == null) {
            throw new HibernateException("DATABASE_USER is not set");
        }
        String password = System.getenv("DATABASE_PASSWORD");
        if (password == null) {
            throw new HibernateException("DATABASE_PASSWORD is not set");
        }
        String debug;
        if ((debug = System.getenv("DATABASE_DEBUG")).isEmpty()) {
            debug = "false";
        }
        sessionFactory = new Configuration().configure()
                .setProperty("hibernate.connection.url", url)
                .setProperty("hibernate.connection.username", username)
                .setProperty("hibernate.connection.password", password)
                .setProperty("hibernate.show_sql", debug)
                .buildSessionFactory();
    }

    public static void start() {
        if (sessionFactory == null) {
            try {
                buildSession();
            } catch (HibernateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static boolean isOpen() {
        return sessionFactory != null && sessionFactory.isOpen();
    }

    public static Session openSession() {
        if (sessionFactory == null || !sessionFactory.isOpen()) {
            buildSession();
            logger.info("factory was null on openSession call");
        }
        return sessionFactory.openSession();
    }

}
