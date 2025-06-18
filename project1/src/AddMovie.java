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
import java.sql.*;

// Declaring a WebServlet called LoginServlet, which maps to url "/api/_dashboard/view-metadata"
@WebServlet(name = "AddMovie", urlPatterns = "/api/_dashboard/add-movie")
public class AddMovie extends HttpServlet {
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

    private String getId(Connection conn, String type) {
        String selectQuery = "";
        String updateQuery = "";
        String prefix = "";
        String result_id = "";

        if (type.equals("movie")) {
            selectQuery = "SELECT next_movie_id AS curr_id FROM check_ids";
            updateQuery = "UPDATE check_ids SET next_movie_id = next_movie_id + 1";
            prefix = "tt";
        } else if (type.equals("star")) {
            selectQuery = "SELECT next_star_id AS curr_id FROM check_ids";
            updateQuery = "UPDATE check_ids SET next_star_id = next_star_id + 1";
            prefix = "nm";
        } else {
            selectQuery = "SELECT next_genre_id AS curr_id FROM check_ids";
            updateQuery = "UPDATE check_ids SET next_genre_id = next_genre_id + 1";
        }

        try {
            PreparedStatement select = conn.prepareStatement(selectQuery);
            ResultSet rs = select.executeQuery();

            int curr_id = 0;
            if (rs.next()) {
                curr_id = rs.getInt("curr_id");
            }
            rs.close();
            select.close();

            PreparedStatement update = conn.prepareStatement(updateQuery);
            update.executeUpdate();
            update.close();

            if (type.equals("genre")) {
                result_id = String.valueOf(curr_id);
            } else {
                result_id = prefix + String.format("%07d", curr_id); // pad to 7 digits
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result_id;
    }

    public boolean tableExists(Connection conn, String tableName) {
        String query = "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = DATABASE() AND table_name = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            if (!tableExists(conn, "check_ids")) {
                String query = "CALL init_check_ids();";
                CallableStatement cs = conn.prepareCall(query);
                cs.execute();
                cs.close();
            }

            String movie_title = request.getParameter("movie_title");
            String movie_director = request.getParameter("movie_director");
            int movie_year = Integer.parseInt(request.getParameter("movie_year"));
            String genre_name = request.getParameter("genre_name");
            String star_name = request.getParameter("star_name");

            String birthYearStr = request.getParameter("star_birth_year");
            Integer birthYear = null;

            if (birthYearStr != null && !birthYearStr.trim().isEmpty()) {
                birthYear = Integer.parseInt(birthYearStr);
            }

            String movie_id = getId(conn, "movie");
            String star_id = getId(conn, "star");
            int genre_id = Integer.parseInt(getId(conn, "genre"));

            String query = "CALL add_movie(?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            CallableStatement cs = conn.prepareCall(query);
            cs.setString(1, movie_id);
            cs.setString(2, movie_title);
            cs.setInt(3, movie_year);
            cs.setString(4, movie_director);
            cs.setString(5, star_id);
            cs.setString(6, star_name);

            if (birthYear != null) {
                cs.setInt(7, birthYear);
            } else {
                cs.setNull(7, java.sql.Types.INTEGER);
            }

            cs.setInt(8, genre_id);
            cs.setString(9, genre_name);

            cs.registerOutParameter(10, java.sql.Types.INTEGER);

            cs.execute();

            int unique = cs.getInt(10);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("unique", unique);
            jsonObject.addProperty("movie_id", movie_id);
            jsonObject.addProperty("genre_id", genre_id);
            jsonObject.addProperty("star_id", star_id);
            jsonObject.addProperty("status", "success");

            out.write(jsonObject.toString());
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

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
