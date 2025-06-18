package SAXParser;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
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

import static SAXParser.MySAXParser.convertAccent;


public class CastParser extends DefaultHandler {

    public static final String xmlCasts =  "casts124.xml";


    public String query = "";
    public String tempVal = "";

    private String currFilmId;
    private String currActor;
    private List<String> currActors = new ArrayList<String>();

    //sql connection
    private DataSource dataSource;
    private Statement statement;
    private Statement statement1;

    private BlockingQueue<String> queue;
    private File xmlLogFile;

    private int elementsRead = 0;
    private int addedEscapeCharacters = 0;


    public CastParser(BlockingQueue<String> queue, DataSource ds, File xmlLogFile){
        this.queue = queue;
        dataSource = ds;
        this.xmlLogFile = xmlLogFile;
    }


    public void startParse() throws Exception{
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(true);

        try {
            if (dataSource == null) {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            }
        }catch (NamingException e){
            e.printStackTrace();
        }

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Connected to database");
            statement = connection.createStatement();
            statement1 = connection.createStatement();

            try {
                SAXParser saxParser = spf.newSAXParser();

                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlCasts);

                saxParser.parse(inputStream, this);


            } catch (ParserConfigurationException e) {
                throw new RuntimeException(e);
            } catch (SAXException e) {
                throw new RuntimeException(e);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally{
            statement.close();
            statement1.close();

            String logtext = "CastParser: Writing to file\n\n" +
                    "Elements read: " + elementsRead + "\n" +
                    "Escape characters formatted: " + addedEscapeCharacters + "\n\n";

            logToFile(xmlLogFile, logtext);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        elementsRead += 1;
        if (qName.equals("f")){
            currFilmId = tempVal;
            return;
        }
        if (qName.equals("a")){
            //currActor = tempVal;
            //query for the actor's id and add id to id
            if (tempVal.equals("s a") || tempVal.equals("sa")){

                return;
            }
            currActors.add(tempVal);
            return;
        }
        if (qName.equals("filmc")){

            ResultSet rs = findActorIDs(currActors, currFilmId);

            addCastToDB(rs, currFilmId);
            currActors.clear();
        }
    }
    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        tempVal = new String(ch, start, length);
    }

    public ResultSet findActorIDs(List<String> currActors, String currFilmId){
        query = "SELECT id from stars WHERE name = ''";
        for (String curr : currActors){
            curr = addEscapeCharactersSQL(curr);
            query += " OR name = '" + curr + "'";
        }
        //System.out.println("findActorsID: " + query);

        try {
            return statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void addCastToDB(ResultSet rs, String currFilmId){
        try{
            while (rs.next()){
                query = "INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES ('" + rs.getString("id") + "', '" + currFilmId + "')";
                //System.out.println("addCastToDB: " +query);
                //statement1.executeUpdate(query);
                queue.put(query);
                //System.out.println("addCastToDB: " +query);
                //System.out.println("queue size: " + queue.size());
            }
        } catch (SQLException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
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
    public String addEscapeCharactersSQL(String word) {
        if (word == null) return null;
        addedEscapeCharacters += 1;
        StringBuilder newWord = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c == '\\'){
                if (i +3 > word.length()) return newWord.toString();
                newWord.append(convertAccent(word.substring(i, i+3)));
            }
            else if (c == '\'') {
                newWord.append("''");
            } else {
                newWord.append(c);
            }
        }
        return newWord.toString();
    }

}