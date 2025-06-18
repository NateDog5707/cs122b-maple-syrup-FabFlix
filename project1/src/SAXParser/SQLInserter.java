package SAXParser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class SQLInserter implements Runnable{
    private BlockingQueue<String> queue;
    private DataSource dataSource;
    private CountDownLatch latch;
    //private Connection connection;

    public SQLInserter(BlockingQueue queue, DataSource ds, CountDownLatch latch){
        this.queue = queue;
        dataSource = ds;
    }
    @Override
    public void run() {
        try (Connection connection = dataSource.getConnection()){
            System.out.println("SAXParser.SQLInserter: Connection established");
            Statement statement = connection.createStatement();
            String query;

            while (true){
                query = queue.take();
                //System.out.println("SQLInserter query: " + query);
                //System.out.println("queue size: " + queue.size());

                if (query.equals(MySAXParser.exitMsg) ){
                    //System.out.println("SQLInserter exit message received");
                    break;
                }
                statement.executeUpdate(query);

            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        finally{
            System.out.println("SQLInserter: latch countdown");
//            latch.countDown();
        }
    }
}
