package src;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.swing.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

@WebServlet (name="WelcomeServlet", urlPatterns= "/api/welcome")
public class WelcomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type
        response.setCharacterEncoding("UTF-8");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        if (session ==null){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String firstName = (String) session.getAttribute("firstName");
        String lastName = (String) session.getAttribute("lastName");

        JsonObject returnJson = new JsonObject();
        returnJson.addProperty("firstName", firstName);
        returnJson.addProperty("lastName", lastName);


        out.write(returnJson.toString());

    }

}
