import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called BrowseServlet, which maps to url "/api/browse"
@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    // Use http GET
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");    // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        String col1 = request.getParameter("col1");
//        String col2 = request.getParameter("col2");
        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");

        String sorting = "";

        // default sorting
        // default sorting
        if (col1 == null) {
            sorting += "r.rating DESC\n";
        }
        else if (col1.equals("title"))
        {

            if (order1.equals("asc"))
            {
                if (order2.equals("asc"))
                {
                    sorting += "m.title ASC, r.rating ASC\n";
                }
                else
                {
                    sorting += "m.title ASC, r.rating DESC\n";
                }
            }
            else
            {
                if (order2.equals("asc"))
                {
                    sorting += "m.title DESC, r.rating ASC\n";
                }
                else
                {
                    sorting += "m.title DESC, r.rating DESC\n";
                }
            }
        }
        else
        {
            // we know col 2 is rating if col 1 is title
            if (order1.equals("asc"))
            {
                if (order2.equals("asc"))
                {
                    sorting += "r.rating ASC, m.title ASC\n";
                }
                else
                {
                    sorting += "r.rating ASC, m.title DESC\n";
                }
            }
            else
            {
                if (order2.equals("asc"))
                {
                    sorting += "r.rating DESC, m.title ASC\n";
                }
                else
                {
                    sorting += "r.rating DESC, m.title DESC\n";
                }
            }
        }

        System.out.println("BrowseServlet debug sorting: " + sorting);
        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");

        if (limit == null)
        {
            limit = "10";
            offset = "0";
        }

        String limitOffset = "\nLIMIT " + limit + " OFFSET " + offset + ";";

        try (Connection connection = dataSource.getConnection()){

            // Declare a new statement
            Statement statement = connection.createStatement();

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String type = request.getParameter("type");
            String keyword = request.getParameter("keyword");
            String query = "";
            if (type == null)
            {
                // If no "type" is given, user wants to get all genres
                query += "SELECT name FROM genres ORDER BY name ASC";
                ResultSet rs = statement.executeQuery(query);

                JsonArray jsonArray = new JsonArray();
                while (rs.next()) {
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("genre_name", rs.getString("name"));
                    jsonArray.add(jsonObject);
                }

                rs.close();
                statement.close();

                out.write(jsonArray.toString());
                response.setStatus(200);
                return;
            }
            else if (type.equals("genre"))
            {
                query += String.format("SELECT DISTINCT m.*, r.rating,\n" +
                        "\t(\n" +
                        "        SELECT GROUP_CONCAT(name ORDER BY name SEPARATOR ', ')\n" +
                        "        FROM (\n" +
                        "            SELECT g.name, gim.genreId\n" +
                        "            FROM genres_in_movies gim\n" +
                        "            JOIN genres g ON gim.genreId = g.id\n" +
                        "            WHERE gim.movieId = m.id\n" +
                        "            ORDER BY g.name\n" +
                        "            LIMIT 3\n" +
                        "        ) AS limited_genres\n" +
                        "    ) AS genres,\n" +
                        "    (\n" +
                        "        SELECT GROUP_CONCAT(starId, ':', name ORDER BY name SEPARATOR ', ')\n" +
                        "        FROM (\n" +
                        "            SELECT s.name, sim.starId\n" +
                        "            FROM stars_in_movies sim\n" +
                        "            JOIN stars s ON sim.starId = s.id\n" +
                        "            WHERE sim.movieId = m.id\n" +
                        "            ORDER BY s.name\n" +
                        "            LIMIT 3\n" +
                        "        ) AS limited_stars\n" +
                        "\t) AS stars\n" +
                        "FROM movies m\n" +
                        "JOIN\n" +
                        "\tgenres_in_movies gim ON gim.movieId = m.id\n" +
                        "JOIN\n" +
                        "\tgenres g ON g.id = gim.genreId\n" +
                        "JOIN\n" +
                        "\tstars_in_movies sim ON sim.movieId = m.id\n" +
                        "JOIN\n" +
                        "\tstars s ON s.id = sim.starId\n" +
                        "LEFT JOIN \n" +
                        "\tratings r ON r.movieId = m.id\n" +
                        "WHERE \n" +
                        "\tg.name = '%s'\n" +
                        "ORDER BY \n" +
                        sorting +
                        limitOffset, keyword);
            }
            // browse by title
            else
            {
                // match for non-alphahnumerical characters
                if (keyword.equals("*"))
                {
                    query += "SELECT DISTINCT m.*, r.rating,\n" +
                            "\t(\n" +
                            "        SELECT GROUP_CONCAT(name  ORDER BY name SEPARATOR ', ')\n" +
                            "        FROM (\n" +
                            "            SELECT g.name, gim.genreId\n" +
                            "            FROM genres_in_movies gim\n" +
                            "            JOIN genres g ON gim.genreId = g.id\n" +
                            "            WHERE gim.movieId = m.id\n" +
                            "            ORDER BY g.name\n" +
                            "            LIMIT 3\n" +
                            "        ) AS limited_genres\n" +
                            "    ) AS genres,\n" +
                            "    (\n" +
                            "        SELECT GROUP_CONCAT(starId, ':', name ORDER BY name SEPARATOR ', ')\n" +
                            "        FROM (\n" +
                            "            SELECT s.name, sim.starId\n" +
                            "            FROM stars_in_movies sim\n" +
                            "            JOIN stars s ON sim.starId = s.id\n" +
                            "            WHERE sim.movieId = m.id\n" +
                            "            ORDER BY s.name\n" +
                            "            LIMIT 3\n" +
                            "        ) AS limited_stars\n" +
                            "\t) AS stars\n" +
                            "FROM movies m\n" +
                            "JOIN\n" +
                            "\tgenres_in_movies gim ON gim.movieId = m.id\n" +
                            "JOIN\n" +
                            "\tgenres g ON g.id = gim.genreId\n" +
                            "JOIN\n" +
                            "\tstars_in_movies sim ON sim.movieId = m.id\n" +
                            "JOIN\n" +
                            "\tstars s ON s.id = sim.starId\n" +
                            "LEFT JOIN \n" +
                            "\tratings r ON r.movieId = m.id\n" +
                            "WHERE \n" +
                            "\tm.title REGEXP '^[^a-zA-Z0-9]'\n" +
                            "ORDER BY \n" +
                            sorting +
                            limitOffset;
                }
                // not *, some alphanumerical character
                else
                {
                    query += String.format("SELECT DISTINCT m.*, r.rating,\n" +
                            "\t(\n" +
                            "        SELECT GROUP_CONCAT(name ORDER BY name SEPARATOR ', ')\n" +
                            "        FROM (\n" +
                            "            SELECT g.name, gim.genreId\n" +
                            "            FROM genres_in_movies gim\n" +
                            "            JOIN genres g ON gim.genreId = g.id\n" +
                            "            WHERE gim.movieId = m.id\n" +
                            "            ORDER BY g.name\n" +
                            "            LIMIT 3\n" +
                            "        ) AS limited_genres\n" +
                            "    ) AS genres,\n" +
                            "    (\n" +
                            "        SELECT GROUP_CONCAT(starId, ':', name ORDER BY name SEPARATOR ', ')\n" +
                            "        FROM (\n" +
                            "            SELECT s.name, sim.starId\n" +
                            "            FROM stars_in_movies sim\n" +
                            "            JOIN stars s ON sim.starId = s.id\n" +
                            "            WHERE sim.movieId = m.id\n" +
                            "            ORDER BY s.name\n" +
                            "            LIMIT 3\n" +
                            "        ) AS limited_stars\n" +
                            "\t) AS stars\n" +
                            "FROM movies m\n" +
                            "JOIN\n" +
                            "\tgenres_in_movies gim ON gim.movieId = m.id\n" +
                            "JOIN\n" +
                            "\tgenres g ON g.id = gim.genreId\n" +
                            "JOIN\n" +
                            "\tstars_in_movies sim ON sim.movieId = m.id\n" +
                            "JOIN\n" +
                            "\tstars s ON s.id = sim.starId\n" +
                            "LEFT JOIN \n" +
                            "\tratings r ON r.movieId = m.id\n" +
                            "WHERE \n" +
                            "\tm.title like '%s%%'\n" +
                            "ORDER BY \n" +
                            sorting +
                            limitOffset, keyword);
                }

            }
            // Log to localhost log
            request.getServletContext().log("query: \n" + query);

            session.setAttribute("lastQuery", query);
            // Perform the query
            ResultSet rs = statement.executeQuery(query);


            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs

            while (rs.next())
            {
                String movie_id = rs.getString("m.id");
                String movie_title = rs.getString("m.title");
                String movie_year = rs.getString("m.year");
                String movie_director = rs.getString("m.director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                float movie_price = rs.getFloat("m.price");
                float movie_rating = rs.getFloat("r.rating");
                boolean nullRating = rs.wasNull();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("movie_price", movie_price);

                if (nullRating)
                {
                    jsonObject.addProperty("movie_rating", "N/A");
                } else {
                    jsonObject.addProperty("movie_rating", movie_rating);
                }

                jsonArray.add(jsonObject);
            }


            // Close all structures
            rs.close();
            statement.close();


            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            e.printStackTrace();
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorType", e.getClass().getName());
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}
