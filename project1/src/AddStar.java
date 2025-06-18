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
@WebServlet(name = "AddStar", urlPatterns = "/api/_dashboard/add-star")
public class AddStar extends HttpServlet {
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

            String star_name = request.getParameter("star_name");
            String birth_year = request.getParameter("birth_year");

            if (!tableExists(conn, "check_ids")) {
                String query = "CALL init_check_ids();";
                CallableStatement cs = conn.prepareCall(query);
                cs.execute();
                cs.close();
            }

            String id = getId(conn, "star");

            String query;
            PreparedStatement insert;
            if (birth_year == null || birth_year.isEmpty())
            {
                query = "insert into stars (id, name)\n" +
                        "values (?, ?);";
                insert = conn.prepareStatement(query);
                insert.setString(1, id);
                insert.setString(2, star_name);
            }
            else
            {
                query = "insert into stars (id, name, birthYear)\n" +
                        "values (?, ?, ?)";
                insert = conn.prepareStatement(query);
                insert.setString(1, id);
                insert.setString(2, star_name);
                insert.setString(3, birth_year);
            }
            request.getServletContext().log("query: " + query);
            insert.executeUpdate();
            insert.close();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("star_id", id);
            jsonObject.addProperty("status", "success");

            System.out.println("Response JSON: " + jsonObject);
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
