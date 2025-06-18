import SAXParser.ActorParser;
import SAXParser.CastParser;
import SAXParser.MySAXParser;
import SAXParser.SQLInserter;
import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import java.sql.Driver;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import main.java.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;

//@WebListener
public class MyStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent ev){
        System.out.println("Startup listener disabled.");
        return;
        /*
        System.out.println("MyStartupListener contextInitialized");

        try {
            UpdateSecurePassword.main(null);
            UpdateSecurePasswordEmployees.main(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String basePath = System.getProperty("user.dir"); // Gets project root dir in dev mode
        System.out.println("Basefilepath: " + basePath);
        File xmlLogFile = new File(basePath + "/logs/xmlLog.txt");
        xmlLogFile.getParentFile().mkdirs(); // just in case
        try (FileWriter writer = new FileWriter(xmlLogFile, false)) {
            writer.write("Start of inconsistency log file\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        //xmlParsing(xmlLogFile);



        DataSource ds;
        try {
            ds =
                     (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        long startTime, endTime;
        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        CountDownLatch latch = new CountDownLatch(numThreads);
        MySAXParser mySAXParser = new MySAXParser(queue, ds, xmlLogFile);
        ActorParser actorParser = new ActorParser(queue, ds, xmlLogFile);
        CastParser castParser = new CastParser(queue,ds, xmlLogFile);

        Thread parserThread = new Thread(() -> {
            try {
                mySAXParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread actorThread = new Thread(() -> {
            try {
                actorParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });Thread castThread = new Thread(() -> {
            try {
                castParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        List<Future<?>> futures = new ArrayList<>();
        System.out.println("starting SQLInserter threads");
        for (int i = 0; i < numThreads ; i++){
            //executor.submit(new SQLInserter(queue, ds, latch));
            futures.add(executor.submit(new SQLInserter(queue, ds, latch)));
        }

        try {
            System.out.println("starting MySAXParser thread");
            startTime = System.currentTimeMillis();
            parserThread.start();
            parserThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished MySAXParser thread");
            System.out.println("Total Movie time: " + (endTime - startTime) + "ms");


            System.out.println("starting ActorParser thread");
            startTime = System.currentTimeMillis();
            actorThread.start();
            actorThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished ActorParser thread");
            System.out.println("Total Actor time: " + (endTime - startTime) + "ms");


            System.out.println("starting CastParser thread");
            startTime = System.currentTimeMillis();
            castThread.start();
            castThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished CastParser thread");
            System.out.println("Total Cast time: " + (endTime - startTime) + "ms");



        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < numThreads ; i++){
            try {
                queue.put(MySAXParser.exitMsg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Exit Message put into queue");


//        System.out.println("Latch wait");
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            System.out.println("Latch interrupted");
//            throw new RuntimeException(e);
//        }
//        System.out.println("Latch passed");

        while (!queue.isEmpty()){
            System.out.println("Waiting for queue to empty... size: " + queue.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Queue is empty, shutdown commence");


//        System.out.println("queue: " + queue.)
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("finished SQLInserter threads");


        for (Future<?> future : futures) {
            try {
                future.get(); // throws ExecutionException if a thread threw a RuntimeException
            } catch (ExecutionException e) {
                System.err.println("SQLInserter thread threw an exception: " + e.getCause());
                e.getCause().printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread interrupted while waiting for SQLInserter.");
            }
        }*/
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("MyStartupListener contextDestroyed: Cleaning up resources...");

        // Deregister JDBC drivers to prevent memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                System.err.println("Error deregistering JDBC driver: " + driver);
                e.printStackTrace();
            }
        }

        // Shutdown MySQL abandoned connection cleanup thread
        AbandonedConnectionCleanupThread.checkedShutdown();
        System.out.println("Successfully shutdown AbandonedConnectionCleanupThread.");
    }



    private void xmlParsing(File xmlLogFile){

        DataSource ds;
        try {
            ds =
                    (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
        long startTime, endTime;
        int numThreads = 4;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        CountDownLatch latch = new CountDownLatch(numThreads);
        MySAXParser mySAXParser = new MySAXParser(queue, ds, xmlLogFile);
        ActorParser actorParser = new ActorParser(queue, ds, xmlLogFile);
        CastParser castParser = new CastParser(queue,ds, xmlLogFile);

        Thread parserThread = new Thread(() -> {
            try {
                mySAXParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        Thread actorThread = new Thread(() -> {
            try {
                actorParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });Thread castThread = new Thread(() -> {
            try {
                castParser.startParse();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


        List<Future<?>> futures = new ArrayList<>();
        System.out.println("starting SQLInserter threads");
        for (int i = 0; i < numThreads ; i++){
            //executor.submit(new SQLInserter(queue, ds, latch));
            futures.add(executor.submit(new SQLInserter(queue, ds, latch)));
        }

        try {
            System.out.println("starting MySAXParser thread");
            startTime = System.currentTimeMillis();
            parserThread.start();
            parserThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished MySAXParser thread");
            System.out.println("Total Movie time: " + (endTime - startTime) + "ms");


            System.out.println("starting ActorParser thread");
            startTime = System.currentTimeMillis();
            actorThread.start();
            actorThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished ActorParser thread");
            System.out.println("Total Actor time: " + (endTime - startTime) + "ms");


            System.out.println("starting CastParser thread");
            startTime = System.currentTimeMillis();
            castThread.start();
            castThread.join();
            endTime = System.currentTimeMillis();

            System.out.println("finished CastParser thread");
            System.out.println("Total Cast time: " + (endTime - startTime) + "ms");



        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < numThreads ; i++){
            try {
                queue.put(MySAXParser.exitMsg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("Exit Message put into queue");


//        System.out.println("Latch wait");
//        try {
//            latch.await();
//        } catch (InterruptedException e) {
//            System.out.println("Latch interrupted");
//            throw new RuntimeException(e);
//        }
//        System.out.println("Latch passed");

        while (!queue.isEmpty()){
            System.out.println("Waiting for queue to empty... size: " + queue.size());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("Queue is empty, shutdown commence");


//        System.out.println("queue: " + queue.)
        executor.shutdown();
        try {
            executor.awaitTermination(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("finished SQLInserter threads");


        for (Future<?> future : futures) {
            try {
                future.get(); // throws ExecutionException if a thread threw a RuntimeException
            } catch (ExecutionException e) {
                System.err.println("SQLInserter thread threw an exception: " + e.getCause());
                e.getCause().printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Main thread interrupted while waiting for SQLInserter.");
            }
        }
    }
}
