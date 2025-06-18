import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-movie"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Get a connection from dataSource
            // Construct a query with parameter represented by "?"
            String query = "SELECT \n" +
                    "    m.*,\n" +
                    "    r.rating," +
                    "    GROUP_CONCAT(DISTINCT g.name ORDER BY g.name SEPARATOR ', ') AS genres,\n" +
                    "    (\n" +
                    "       SELECT GROUP_CONCAT(\n" +
                    "            CONCAT(s.id, ':', s.name)\n" +
                            "            ORDER BY movie_count DESC, s.name ASC\n" +
                            "            SEPARATOR ', '\n" +
                            "        )\n" +
                            "        FROM stars_in_movies sim\n" +
                            "        JOIN (\n" +
                            "            SELECT s.id, s.name, COUNT(sim2.movieId) AS movie_count\n" +
                            "            FROM stars s\n" +
                            "            JOIN stars_in_movies sim2 ON s.id = sim2.starId\n" +
                            "            GROUP BY s.id, s.name\n" +
                            "        ) AS s ON sim.starId = s.id\n" +
                            "        WHERE sim.movieId = m.id\n" +
                            "    ) AS stars\n" +
                    "FROM movies m\n" +
                    "LEFT JOIN ratings r ON r.movieId = m.id\n" +
                    "JOIN genres_in_movies gim ON gim.movieId = m.id\n" +
                    "JOIN genres g ON g.id = gim.genreId\n" +
                    "JOIN stars_in_movies sim ON sim.movieId = m.id\n" +
                    "JOIN stars s ON s.id = sim.starId\n" +
                    "WHERE m.id = ?\n" +
                    "GROUP BY m.id, m.title, m.year, m.director, r.rating\n" +
                    ";\n";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {

                String smovie_id = rs.getString("m.id");
                String smovie_title = rs.getString("m.title");
                int smovie_year = rs.getInt("m.year");
                String smovie_director = rs.getString("m.director");
                String smovie_genres = rs.getString("genres");
                float smovie_rating = rs.getFloat("r.rating");
                String smovie_stars = rs.getString("stars");
                float smovie_price = rs.getFloat("m.price");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("smovie_id", smovie_id);
                jsonObject.addProperty("smovie_title", smovie_title);
                jsonObject.addProperty("smovie_year", smovie_year);
                jsonObject.addProperty("smovie_director", smovie_director);
                jsonObject.addProperty("smovie_genres", smovie_genres);
                jsonObject.addProperty("smovie_rating", smovie_rating);
                jsonObject.addProperty("smovie_stars", smovie_stars);
                jsonObject.addProperty("smovie_price", smovie_price);

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

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

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
