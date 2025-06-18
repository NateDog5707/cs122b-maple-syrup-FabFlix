import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.sql.DataSource;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


// Declaring a WebServlet called LoginServlet, which maps to url "/api/login"
@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
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
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        HttpSession session = request.getSession(true);
        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        /*
        // try recaptcha response first before proceeding with username and pwd
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            // if it fails, reject login attempt
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorType", e.getClass().getName());
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
            out.close();
            return;
        }

        */
        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            String inputEmail = request.getParameter("email");
            String inputPassword = request.getParameter("password");

            // Declare our statement
            String query = "SELECT email, password, id, firstName, lastName FROM customers WHERE email = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, inputEmail);

            // Perform the query
            ResultSet rs = ps.executeQuery();
            boolean success = false;

            if (rs.next()) {
                out.print("{\"status\": \"success\"}");

                session.setAttribute("id", rs.getString("id"));
                session.setAttribute("firstName", rs.getString("firstName"));
                session.setAttribute("lastName", rs.getString("lastName"));
                session.setAttribute("email", rs.getString("email"));
                session.setAttribute("password", rs.getString("password"));

            } else {
                out.print("{\"status\": \"error\", \"message\": \"Incorrect email or password\"}");
            }

            rs.close();
            ps.close();

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
