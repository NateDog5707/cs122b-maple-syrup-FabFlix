package SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.crypto.Data;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;


public class ActorParser extends DefaultHandler {

    public static final String xmlActors =  "actors63.xml";
    public static final String starsIdFront = "nm";

    public String query = "";
    public String tempVal = "";
    private int dob;
    private String stageName = "";
    private List<String> lastAddedNames = new ArrayList<String>();

    private int currId = -1;
    //sql connection
    private DataSource dataSource;
    private Statement statement;
    private Statement statement1;

    private BlockingQueue<String> queue;

    private File xmlLogFile;
    private int elementsRead = 0;
    private int incDOB = 0;
    private int repeatElements = 0;

    public ActorParser(){
        for (int i =0 ; i < 5 ;i++){
            lastAddedNames.add("");
        }
    }
    public ActorParser(BlockingQueue<String> queue, DataSource ds, File xmlLogFile){
        for (int i = 0; i < 5; i++) {
            lastAddedNames.add("");
        }
        this.queue = queue;
        dataSource = ds;
        this.xmlLogFile = xmlLogFile;
    }

    public void startParse()  throws Exception{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);

        try {
            if (dataSource == null){
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("ActorParser: Connected to database");
            statement = connection.createStatement();
            statement1 = connection.createStatement();

            createDBIndexes(statement, true);
            //createDBIndexes(statement, false);


            try {
                query = "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) AS max_id\n" +
                        "FROM stars\n" +
                        "WHERE id LIKE 'nm%';";
                ResultSet rs = statement.executeQuery(query);
                if (rs.next()){
                    currId = rs.getInt("max_id") + 1;
                }

//                query = "CREATE UNIQUE INDEX uniquestar ON stars(name, birthYear);\n";
//                statement.executeUpdate(query);


                SAXParser saxParser = spf.newSAXParser();

                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlActors);

                saxParser.parse(inputStream, this);


//                query = "ALTER TABLE stars drop index uniquestar";
//                statement.executeUpdate(query);

                rs.close();

            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally{
            statement.close();
            statement1.close();

            String logtext = "ActorParser: Writing to file\n\n" +
                    "Elements read: " + elementsRead + "\n" +
                    "Incorrect DOB: " + incDOB + "\n" +
                    "Repeat movies: " + repeatElements + "\n\n";

            logToFile(xmlLogFile, logtext);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elementsRead+= 1;
        if (qName.equals("stagename")){
            stageName = tempVal;
        }
        if (qName.equals("dob")){
            //since dob is all we need, we also submit here

            tempVal = tempVal.replaceAll("[+.\\-/~ ]", ""); // gets rid of blanks or other misc chars
            if (tempVal.contains("dob") || tempVal.isBlank() || tempVal.contains("na") || tempVal.contains("n.a") || tempVal.contains("*")){
                dob = -1;
                incDOB += 1;
            }
            else if (tempVal.matches(".*[a-zA-Z]/*")){
                incDOB += 1;
                dob = Integer.parseInt(tempVal.replaceAll(".*[a-zA-Z].*","0"));
            } else if (tempVal.contains("[1]")){
                incDOB += 1;
                dob = Integer.parseInt(tempVal.replace("[1]",""));
            }
            else {
                dob = Integer.parseInt(tempVal);
            }

            //submit
            for ( String prev : lastAddedNames){
                if (prev.equals(stageName)) {
//                    System.out.println("hi");
                    repeatElements +=1;
                    lastAddedNames.remove(prev);
                    lastAddedNames.add("");
                    return;
                }
            }
            addStarToDB(stageName, dob);

        }
    }
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public void addStarToDB(String stageName, int dob){
        String stringID = starsIdFront + ""  + String.format("%06d",currId);
        //System.out.println(stringID);

        stageName = MySAXParser.addEscapeCharactersSQL(stageName);
        if (dob == -1){
            query = "INSERT IGNORE INTO stars (id, name) VALUES ('" + stringID + "', '" + stageName + "')";
        }
        else{
            query = "INSERT IGNORE INTO stars (id, name, birthYear) VALUES ('" + stringID + "', '" + stageName + "', " + dob + ")";
        }
        //System.out.println(query);
        try {
            queue.put(query);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
//        try {
//            statement.executeUpdate(query);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        currId += 1;
        lastAddedNames.add(stageName);
        lastAddedNames.remove(0);
    }

    public void createDBIndexes(Statement statement, boolean createNew){
        //movie director and year
        ResultSet rs;
        try {
            query = "SHOW INDEX FROM stars WHERE Key_name = 'idx_birthYear';";
            rs = statement.executeQuery(query);
            if (rs.next()){ // if there is an index, drop it
                //drop the indexs mayhaps
                query = "DROP INDEX idx_birthYear ON stars";
                statement.executeUpdate(query);
                if (createNew){
                    query = "CREATE INDEX idx_birthYear ON stars(birthYear)";
                    statement.executeUpdate(query);
                }
            }
            else if (createNew){
                query = "CREATE INDEX idx_birthYear ON stars(birthYear)";
                statement.executeUpdate(query);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public synchronized static void logToFile(File file, String message) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}