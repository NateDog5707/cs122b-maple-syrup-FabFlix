import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;


// Declaring a WebServlet called MovieListServlet, which maps to url "/api/movie-list"
@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movie-list")
public class MovieListServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        String col1 = request.getParameter("col1");
        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");
        String title = request.getParameter("title");

        String sorting = "";

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

        String limit = request.getParameter("limit");
        String offset = request.getParameter("offset");
        if (limit == null)
        {
            limit = "10";
            offset = "0";
        }

        String limitOffset = "\nLIMIT " + limit + " OFFSET " + offset + ";";

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();
            // query includes star ids
            String query = "";
            query = "SELECT \n" +
                    "    m.id,\n" +
                    "    m.title,\n" +
                    "    m.year,\n" +
                    "    m.director,\n" +
                    "    m.price,\n" +
                    "    r.rating,\n" +
                    "\n" +
                    "    (\n" +
                    "        SELECT GROUP_CONCAT(lg.name ORDER BY lg.name SEPARATOR ', ')\n" +
                    "        FROM (\n" +
                    "            SELECT g.name\n" +
                    "            FROM genres_in_movies gim\n" +
                    "            LEFT JOIN genres g ON gim.genreId = g.id\n" +
                    "            WHERE gim.movieId = m.id\n" +
                    "            ORDER BY g.name\n" +
                    "            LIMIT 3\n" +
                    "        ) AS lg\n" +
                    "    ) AS genres,\n" +
                    "\n" +
                    "    (\n" +
                    "        SELECT GROUP_CONCAT(CONCAT(ls.starId, ':', ls.name) ORDER BY ls.name SEPARATOR ', ')\n" +
                    "        FROM (\n" +
                    "            SELECT sim.starId, s.name\n" +
                    "            FROM stars_in_movies sim\n" +
                    "            LEFT JOIN stars s ON sim.starId = s.id\n" +
                    "            WHERE sim.movieId = m.id\n" +
                    "            ORDER BY s.name\n" +
                    "            LIMIT 3\n" +
                    "        ) AS ls\n" +
                    "    ) AS stars\n" +
                    "\n" +
                    "FROM \n" +
                    "    movies m\n" +
                    "LEFT JOIN \n" +
                    "    ratings r ON m.id = r.movieId\n" +
                    "\n" +
                    "ORDER BY " + sorting + limitOffset;

            // Perform the query

            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                int movie_year = rs.getInt("year");
                String movie_director = rs.getString("director");
                float movie_price = rs.getFloat("price");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                float movie_rating = rs.getFloat("rating");
                boolean nullRating = rs.wasNull();

                System.out.println("MovieListServlet: " + movie_id + " " + movie_title);
                // Create a JsonObject based on the data we retrieve from rs
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
