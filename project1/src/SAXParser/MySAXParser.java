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
import java.util.concurrent.LinkedBlockingQueue;


public class MySAXParser extends DefaultHandler {

    public static final int numThreads = 4;
    public static final String xmlMain = "mains243.xml";
    public static final String exitMsg = "$$$Exit";
    public String query = "";
    public String tempVal = "";


    public String currDirID= "";
    public String currDirName ="";
    public String currFilmId;
    public String currTitle = "";
    public int currYear;
    public List<String> lastAddedTitles = new ArrayList<String>();

    public List<String> genres = new ArrayList<String>();
    public List<String> currGenres = new ArrayList<String>();
    //sql connection
    private DataSource dataSource;
    private Statement statement;
    private Statement statement1;

    private BlockingQueue<String> queue;

    private File xmlLogFile;
    private int elementsRead = 0;
    private int elementsSplit =0;
    private int genresCorrected = 0;
    private int otherInconsistencies = 0;
    private int repeatElements = 0;
    private static int addedEscapeCharacters = 0;

    public MySAXParser(){
        for (int i = 0; i < 5; i++) {
            lastAddedTitles.add("");
        }
    }
    
    public MySAXParser(BlockingQueue<String> queue, DataSource ds, File xmlLogFile){
        for (int i = 0; i < 5; i++) {
            lastAddedTitles.add("");
        }
        this.queue = queue;
        dataSource = ds;
        this.xmlLogFile = xmlLogFile;
    }


    public void startParse() throws Exception{
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(true);

        try {
            if (dataSource == null) {
                dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
            }
        }catch (NamingException e){
            e.printStackTrace();
        }

        try ( Connection connection = dataSource.getConnection()){
            System.out.println("MySAXParser: Connected to database");
             statement = connection.createStatement();
             statement1 = connection.createStatement();
            ResultSet rs;
            //get rid of duplicates
            handleDupl(statement);

            //toggle true/false index data or not
            createDBIndexes(statement, true); //creates indexes by movie director, year, etc.
            //createDBIndexes(statement, false);

//            query = "SELECT COUNT(*) AS column_exists\n" +
//                    "FROM INFORMATION_SCHEMA.COLUMNS\n" +
//                    "WHERE TABLE_SCHEMA = 'your_database_name'\n" +
//                    "  AND TABLE_NAME = 'movies'\n" +
//                    "  AND COLUMN_NAME = 'price';\n";
//
//            rs = statement.executeQuery(query);
//            if (rs.next()){
//                if (rs.getInt("column_exists") != 1){
//                    String[] setupPriceArray = {"ALTER TABLE movies ADD COLUMN price INTEGER;",
//                            "UPDATE movies SET price = FLOOR(1 + (RAND(CAST(SUBSTRING(id, LENGTH(id) - 6, 7) AS SIGNED))) * 20);",
//                            "ALTER TABLE sales ADD COLUMN quantity INT;"};
//
//                    for (int i = 0; i < 3; i++){
//                        query = setupPriceArray[i];
//                        statement.executeUpdate(query);
//                    }
//                }
//            }

            query = "SELECT name FROM genres"; //select genre names already inside
            rs = statement.executeQuery(query);

            while (rs.next()) {
                genres.add(rs.getString("name"));
                //System.out.println("genres: " + rs.getString("name"));
            }


            SAXParser sax = factory.newSAXParser();

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(xmlMain);
            System.out.println("MySAXParser: parsing");
            sax.parse(inputStream, this);
            //
            // sax.parse("C:")
            //sax.parse(xmlMain,this);
            rs.close();

        } catch (SAXParseException e){

            e.printStackTrace();
        } catch (ParserConfigurationException e) {

            e.printStackTrace();
        } catch (SAXException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            System.out.println("MySAXParser: done parsing");
            System.out.println("MySAXParser: file writing");

            //rs.close();
            statement.close();
            statement1.close();
            String logtext = "MySAXParser: Writing to file\n\n" +
                        "Elements read: " + elementsRead + "\n" +
                        "Elements split: " + elementsSplit + "\n" +
                        "Genres corrected: " + genresCorrected + "\n" +
                        "Repeat movies: " + repeatElements + "\n" +
                        "Other inconsistencies: " + otherInconsistencies + "\n" +
                        "Escape characters formatted: " + addedEscapeCharacters + "\n\n";

            logToFile(xmlLogFile, logtext);


        }

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        tempVal = "";

    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        //System.out.println("endElement: " + qName);
        //per director
        //System.out.println(qName);
        elementsRead+= 1;
        if (qName.equals("dirid")){
            currDirID = tempVal;
            return;
        }
        if (qName.equals("dirname")){
            currDirName = tempVal;
            return;
        }

        //per movie
        if (qName.equals("fid")){
            currFilmId = tempVal;
            return;
        }
        if (qName.equals("t")){
            currTitle = tempVal;
            return;
        }
        if (qName.equals("year")){
            try {
                currYear = Integer.parseInt(tempVal);
            } catch (NumberFormatException e){
                currYear = 0;
            }
            return;
        }
        if (qName.equals("cat")){//category, genre
            //System.out.println(qName + " "  + tempVal);
            if (tempVal.isEmpty() || tempVal.isBlank() || tempVal.contains("Ctxx") || tempVal.contains("xx") || tempVal.contains("*") || tempVal.equals("col") || tempVal.matches("H.*")){
                otherInconsistencies++;
                return;
            }
            //handle special genres that are two words ex. avant garde or anti-
            if (tempVal.toLowerCase().contains("avan") || (tempVal.length() >=4 && tempVal.substring(0,4).toLowerCase().contains("anti"))){
                //System.out.println("avan or anti: " + tempVal);
                addGenreToDB(tempVal, false);
                return;
            }
            //if genre is multiple words split by space or dashes or ., parse them
            String[] genresToAdd = tempVal.split("[\\s.-]+");
            if(genresToAdd.length >= 2){
                elementsSplit+=1;
            }
            for (String genre : genresToAdd){
                //genre = parseGenre(genre);
                if (!genres.contains(genre)){
                    addGenreToDB(genre, true);
                }
                //addGenreToDB(genre);
            }
            return;

        }
        if(qName.equals("film")){
            //add to db
            for (String prev: lastAddedTitles) {
                if (prev.equals(currTitle)) {
                    //System.out.println("Hey");
                    repeatElements+= 1;
                    lastAddedTitles.remove(prev);
                    lastAddedTitles.add("");
                    return;
                }
            }
                addMovieToDB(currFilmId, currTitle, currYear, currDirName);
                //link genres to movie
                linkGenresToMovie(statement, statement1, currFilmId, currGenres);
                currGenres.clear();
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        tempVal = new String(ch, start, length);
        //System.out.println("characters: "  + tempVal);
    }

    public void addGenreToDB(String tempVal, boolean skipParse){
        if (skipParse == true){
            tempVal = parseGenre(tempVal);

        }
        currGenres.add(tempVal); //adds read genre to genrelist for curr movie, to link later
        for (String genre : genres){ //checks if already exists in database (local array representation)
            if (genre.toLowerCase().contains(tempVal.toLowerCase())){
                return;
            }
        }
        //once parsed and new genre, add the genre!
        query = "INSERT INTO genres (name) VALUES ('" + tempVal + "')";
        //System.out.println("MySAXParser: inserting genre : " + query);
        try {
            statement.executeUpdate(query);
            genres.add(tempVal);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void addMovieToDB(String currFilmId, String currTitle, int currYear, String currDirName){
        //generate random num for price 1-20
        int rand = (int) (Math.random() * 20 + 1);
        currTitle = addEscapeCharactersSQL(currTitle);
        currDirName = addEscapeCharactersSQL(currDirName);
        query = "INSERT IGNORE INTO movies (id, title, year, director, price) VALUES ('" + currFilmId + "', '" + currTitle+ "', " + currYear +", '" + currDirName + "', " +  rand + ")";
        //System.out.println("addMovieToDB: " + query);
//        try {
        try {
            queue.put(query);
            //System.out.println("Queue size: " + queue.size());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("Queue: \n:" + queue.toString());
            //statement.executeUpdate(query);
//        }
//        catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        addRatingsToDB(currFilmId);
        lastAddedTitles.add(currTitle);
        lastAddedTitles.remove(0);
    }

    public void addRatingsToDB(String currFilmId){
        double rand = (Math.random() * 6.9) + 3.0;
        String formattedRand = String.format("%.1f", rand);
        query = "INSERT IGNORE INTO ratings (movieId, rating, numVotes) VALUES ('" + currFilmId +"', " + formattedRand + ", 1)";
//        try {
            queue.add(query);
//            statement.executeUpdate(query);
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
    }


    public String parseGenre(String cur){
        //System.out.println("cur: " + cur);
        genresCorrected+=1;
        if (cur.toLowerCase().contains("actn") || cur.toLowerCase().contains("ctn") || (cur.toLowerCase().contains("a") && cur.toLowerCase().contains("tn"))) {
            //System.out.println("parseGenre() inconsistency: " + cur + " -> Action");
            return "Action";
        }
        else if (cur.toLowerCase().contains("advt") || (cur.toLowerCase().contains("ad") && cur.toLowerCase().contains("t"))) return "Adventure";
        else if (cur.toLowerCase().contains("avga")) return "Avant Garde";
        else if (cur.toLowerCase().contains("bio")) return "Biography";
        else if (cur.toLowerCase().contains("bnw")) return "BnW";
        else if (cur.toLowerCase().contains("cart")) return "Cartoon";
        else if (cur.toLowerCase().contains("comd") ||cur.toLowerCase().contains("cond") ) return "Comedy";
        else if (cur.toLowerCase().contains("cnr")) return "CnRb";
        else if (cur.toLowerCase().contains("crim") || cur.toLowerCase().contains("cmr")) return "Crime";
        else if (cur.toLowerCase().contains("disa")) return "Disaster";
        else if (cur.toLowerCase().matches(".*d.c.+")) return "Documentary";
        else if (cur.toLowerCase().contains("dram") || cur.contains("Dr")) return "Drama";
        else if (cur.toLowerCase().contains("exp")) return "Experimental";
        else if (cur.toLowerCase().contains("fam")) return "Family";
        else if (cur.toLowerCase().contains("fant")) return "Fantasy";
        else if (cur.toLowerCase().contains("hist")) return "History";
        else if (cur.toLowerCase().contains("horr")) return "Horror";
        else if (cur.toLowerCase().contains("musc") || (cur.toLowerCase().contains("mu") && cur.toLowerCase().contains("sc"))) return "Musical";
        else if (cur.toLowerCase().contains("myst")) return "Mystery";
        else if (cur.toLowerCase().contains("nat")) return "Nature";
        else if (cur.contains("Por")) return "Porn";
        else if (cur.toLowerCase().contains("rom") || cur.toLowerCase().contains("ron") ) return "Romance";
        else if (cur.toLowerCase().contains("sati")) return "Satire";
        else if (cur.toLowerCase().contains("scif") || cur.toLowerCase().contains("scfi") || (cur.contains("S") && cur.contains("Fi"))) return "SciFi";
        else if (cur.toLowerCase().contains("sport")) return "Sports";
        else if (cur.toLowerCase().contains("stag")) return "Stage";
        else if (cur.toLowerCase().contains("surl") || cur.toLowerCase().contains("surr")) return "Surreal";
        else if (cur.toLowerCase().contains("susp")) return "Suspence";
        else if (cur.toLowerCase().contains("tvmini")) return "TVmini";
        else if (cur.toLowerCase().contains("tv")) return "TV";
        else if (cur.toLowerCase().contains("vio")) return "Violence";
        else if (cur.toLowerCase().contains("verit")) return "Verite";
        else if (cur.toLowerCase().contains("west")) return "Western";

        //it was normal, undo +1
        genresCorrected-=1;
        return cur;
    }

    public void createDBIndexes(Statement statement, boolean createNew){
        //movie director and year
        ResultSet rs;
        try {
            query = "SHOW INDEX FROM movies WHERE Key_name = 'idx_director';";
            rs = statement.executeQuery(query);
            if (rs.next()){ // if there is an index, drop it
                //drop the indexs mayhaps
                query = "DROP INDEX idx_director ON movies";
                statement.executeUpdate(query);
                if (createNew){
                    query = "CREATE INDEX idx_director ON movies(director)";
                    statement.executeUpdate(query);
                }
            }
            else if (createNew){
                query = "CREATE INDEX idx_director ON movies(director)";
                statement.executeUpdate(query);
            }
            query = "SHOW INDEX FROM movies WHERE Key_name = 'idx_year';";
            rs = statement.executeQuery(query);
            if (rs.next()){ // if there is an index, drop it
                query = "DROP INDEX idx_year ON movies";
                statement.executeUpdate(query);
                if (createNew){
                    query = "CREATE INDEX idx_year ON movies(year)";
                    statement.executeUpdate(query);

                }
            }else if (createNew){
                query = "CREATE INDEX idx_year ON movies(year)";
                statement.executeUpdate(query);
            }

//            query = "SHOW INDEX FROM ratings WHERE Key_name = 'idx_movieId';";
//            rs = statement.executeQuery(query);
//            if (rs.next()){ // if there is an index, drop it
//                //drop the indexs mayhaps
//                query = "DROP INDEX idx_movieId ON ratings";
//                statement.executeUpdate(query);
//                if (createNew){
//                    query = "CREATE UNIQUE INDEX idx_movieId ON ratings(movieId)";
//                    statement.executeUpdate(query);
//                }
//            }
//            else if (createNew){
//                query = "CREATE UNIQUE INDEX idx_movieId ON ratings(movieId)";
//                statement.executeUpdate(query);
//            }

            query = "SHOW INDEX FROM stars WHERE Key_name = 'unique_name';";
            rs = statement.executeQuery(query);
            if (rs.next()){ // if there is an index, drop it
                //drop the indexs mayhaps
                query = "DROP INDEX unique_name ON stars";
                statement.executeUpdate(query);
                if (createNew){
                    query = "CREATE UNIQUE INDEX unique_name ON stars(name)";
                    statement.executeUpdate(query);
                }
            }
            else if (createNew){
                query = "CREATE UNIQUE INDEX unique_name ON stars(name)";
                statement.executeUpdate(query);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static String addEscapeCharactersSQL(String word) {
        if (word == null) return null;
        addedEscapeCharacters +=1;
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
    public static String convertAccent(String cur) {
        switch (cur) {
            case "\\'a": return "á";
            case "\\'e": return "é";
            case "\\'i": return "í";
            case "\\'o": return "ó";
            case "\\'u": return "ú";
            case "\\^a": return "â";
            case "\\^e": return "ê";
            case "\\^i": return "î";
            case "\\^o": return "ô";
            case "\\^u": return "û";
            case "\\`a": return "à";
            case "\\`e": return "è";
            case "\\`i": return "ì";
            case "\\`o": return "ò";
            case "\\`u": return "ù";
            // Add any others as needed
            default: return cur;  // Return original if no match
        }
    }
    public void linkGenresToMovie(Statement statement, Statement statement1, String currFilmId, List<String> genres){
        query = "SELECT id FROM genres WHERE name = ''";
        for (String genre: genres){
           query +=  " OR name = '" + genre + "'";
        }
        try {
            ResultSet rs = statement.executeQuery(query);
            while (rs.next()){
                query = "INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (" + rs.getInt("id") + ", '" + currFilmId + "')";
                queue.put(query);
                //statement1.executeUpdate(query);
                //System.out.println("linkGenres: " + query);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
    public void handleDupl(Statement statement) throws SQLException {

            query = "CREATE TEMPORARY TABLE star_id_map (\n" +
                    "    keep_id VARCHAR(10),\n" +
                    "    drop_id VARCHAR(10)\n" +
                    ");";
            statement.executeUpdate(query);

            query = "INSERT INTO star_id_map(keep_id, drop_id)\n" +
                    "SELECT g.keep_id, s.id AS drop_id\n" +
                    "FROM (\n" +
                    "    SELECT name, MIN(id) AS keep_id\n" +
                    "    FROM stars\n" +
                    "    GROUP BY name\n" +
                    "    HAVING COUNT(*) > 1\n" +
                    ") AS g\n" +
                    "JOIN stars s\n" +
                    "  ON s.name = g.name\n" +
                    "WHERE s.id <> g.keep_id;";
            statement.executeUpdate(query);

            query = "UPDATE stars_in_movies sim\n" +
                    "JOIN star_id_map m ON sim.starId = m.drop_id\n" +
                    "SET sim.starId = m.keep_id;";
            statement.executeUpdate(query);

            query = "DELETE s\n" +
                    "FROM stars s\n" +
                    "JOIN star_id_map m ON s.id = m.drop_id;";
            statement.executeUpdate(query);

            query = "drop table star_id_map";
            statement.executeUpdate(query);

    }
    public synchronized static void logToFile(File file, String message) {
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
