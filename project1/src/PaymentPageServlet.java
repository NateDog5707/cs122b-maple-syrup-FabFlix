import com.google.gson.JsonObject;
import com.mysql.cj.Session;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PaymentPageServlet", urlPatterns = "/api/paymentpage")
public class PaymentPageServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) throws ServletException {
        try{
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        }catch (NamingException e){
            e.printStackTrace();
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        HttpSession session = request.getSession(false);
        double cartTotal = (double) session.getAttribute("total");
        String action = request.getParameter("action");
        System.out.println(action);
        if (action == null) {
            System.out.println("PaymentPageServlet: action is null");
            return;
        }
        switch(action){
            case "getCartTotal":
                System.out.println("PaymentPageServlet: getCartTotal");
                JsonObject json = new JsonObject();
                json.addProperty("total", cartTotal);
                response.getWriter().write(json.toString());
                break;
            case "validateCard":
                try (Connection conn = dataSource.getConnection()){
                    Statement statement = conn.createStatement();
                    String query = "SELECT id, firstName, lastName, expiration " +
                            "FROM creditcards;";
                    ResultSet rs = statement.executeQuery(query);

                    String id = request.getParameter("id");
                    String firstName = request.getParameter("firstName");
                    String lastName = request.getParameter("lastName");
                    String expiration = request.getParameter("expiration");
                    System.out.println(id + " " + firstName + " " + lastName + " " + expiration);
                    while (rs.next()) {
                        if (rs.getString("id").equals(id) &&
                                rs.getString("firstName").equals(firstName) &&
                                rs.getString("lastName").equals(lastName) &&
                                rs.getString("expiration").equals(expiration)){

                            System.out.println("CreditCard Success");
                            response.getWriter().write("{\"status\": \"success\"}");

                            LocalDate today = LocalDate.now();
                            //create a new sale
                            List<Item> cart = (List<Item>) session.getAttribute("cart");
                            for (Item item : cart){

                                String query1 = "INSERT INTO sales (customerId, movieId, saleDate, quantity)" +
                                        " VALUES (" +
                                        session.getAttribute("id") + ", '" +
                                        item.getId() + "', '" +
                                        today + "', " +
                                        item.getQuantity() + ");";
                                System.out.println(query1);
                                int rowsAffected = statement.executeUpdate(query1);
                                System.out.println("Rows inserted: " + rowsAffected);
                                //conn.commit();
                            }
                            cart.clear();
                            rs.close();
                            statement.close();

                            return;
                        }
                    }

                    System.out.println("CreditCard Fail");

                    rs.close();
                    statement.close();
                }catch (Exception e){
                    System.out.println("validateCard: Failed to open connection " + e.getMessage());
                    e.printStackTrace();
                }
                break;

        }

    }
}
