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


// Declaring a WebServlet called SearchServlet, which maps to url "/api/maintain-search"
@WebServlet(name = "MaintainSearchServlet", urlPatterns = "/api/maintain-search")
public class MaintainSearchServlet extends HttpServlet {

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    // Use http GET
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");    // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();


        JsonObject json = new JsonObject();
        json.addProperty("lastTitle", (String) session.getAttribute("lastTitle"));
        json.addProperty("lastYear", (String) session.getAttribute("lastYear"));
        json.addProperty("lastDirector", (String) session.getAttribute("lastDirector"));
        json.addProperty("lastStarName", (String) session.getAttribute("lastStarName"));

        json.addProperty("lastLimit", (String) session.getAttribute("limit"));
        json.addProperty("lastOffset", (String) session.getAttribute("offset"));

        out.write(json.toString());
        System.out.println(json.toString());

        out.close();
    }
}