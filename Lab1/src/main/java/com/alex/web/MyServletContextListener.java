package com.alex.web;

import com.alex.utils.MyFileReader;
import com.alex.utils.Utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@WebListener
public class MyServletContextListener implements ServletContextListener {

    private Logger log = Logger.getLogger(MyServletContextListener.class.getName());

    private final static int POOL_SIZE = 4;

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        //Notification that the servlet context is about to be shut down.
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        log.info("before starting reload db or create db");
//        Utils.getMainConnection();
        Utils.init();
        // do all the tasks that you need to perform just after the server starts
        //Notification that the web application initialization process is starting
        ExecutorService pool = Executors.newFixedThreadPool(POOL_SIZE);
        File dir = new File(getClass().getClassLoader().getResource("book").getFile());
        for (File file: dir.listFiles()) {
            if (file.isDirectory()) {
                for (File tempFile : Objects.requireNonNull(file.listFiles())) {
                    log.info("Path == " + tempFile.getAbsolutePath());
                    MyFileReader fileReader = new MyFileReader(tempFile);
                    pool.execute(fileReader);
                }
            } else {
                log.info("Path == " + file.getAbsolutePath());
                MyFileReader fileReader = new MyFileReader(file);
                pool.execute(fileReader);
            }

        }
        pool.shutdown();
    }

}
