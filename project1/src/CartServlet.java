

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
import java.util.ArrayList;
import java.util.List;

import static java.lang.Double.parseDouble;


@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        // Assuming you stored cart as a list or map in session
        List<Item> cart = (List<Item>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
        }

        response.setContentType("application/json");
        System.out.println(response.toString());
        String action = request.getParameter("action");
        if (action == null) {
            System.out.println("action is null");
            return;
        }
        switch(action){
            case "display":
                System.out.println("Displaying cart");
                double total = 0.0;
                //cart.add(new Item("name", 50.0, 1));
                JsonArray jsonArray = new JsonArray();
                for (Item item : cart) {
                    JsonObject jsonItem = new JsonObject();
                    System.out.println(item.getName() + " " + item.getPrice() + " " + item.getQuantity());
                    jsonItem.addProperty("item_id", item.getId());
                    jsonItem.addProperty("item_name", item.getName());
                    jsonItem.addProperty("item_price", item.getPrice());
                    jsonItem.addProperty("item_quantity", item.getQuantity());
                    jsonArray.add(jsonItem);
                    total += item.getPrice() * item.getQuantity();
                }
                session.setAttribute("cart", cart);
                session.setAttribute("total", total);
                response.getWriter().write(jsonArray.toString());
                response.setStatus(200);
                break;
            case "add":
                System.out.println("CartServlet.java: adding a movie to cart");
                System.out.println(request.getParameter("movieId") + " " +request.getParameter("movieName") + " " + request.getParameter("moviePrice"));
                for (Item item : cart) {
                    if (item.getId().equals(request.getParameter("movieId"))) {
                        item.incrementQuantity();
                        session.setAttribute("cart", cart);
                        session.setAttribute("total", (double)session.getAttribute("total") + item.getPrice());
                        return;
                    }
                }

                cart.add(new Item(request.getParameter("movieId"), request.getParameter("movieName"), parseDouble(request.getParameter("moviePrice")), 1));

                session.setAttribute("cart", cart);
                break;
            case "increment":
                for (Item item : cart) {
                    if (item.getId().equals(request.getParameter("itemId"))) {
                        item.incrementQuantity();

                        //session.setAttribute("cart", cart);
                        return;
                    }
                }
                break;
            case "decrement":
                for (Item item : cart) {
                    if (item.getId().equals(request.getParameter("itemId"))) {
                        item.decrementQuantity();

                        session.setAttribute("total", (double)session.getAttribute("total") - item.getPrice());
                        if (item.getQuantity() == 0) {
                            cart.remove(item);
                        }
                        //session.setAttribute("cart", cart);
                        return;
                    }
                }
                break;
            case "delete":
                for (Item item : cart) {
                    if (item.getId().equals(request.getParameter("itemId"))) {
                        cart.remove(item);

                        session.setAttribute("total", (double)session.getAttribute("total") - (item.getQuantity() * item.getPrice()));
                        //session.setAttribute("cart", cart);
                        return;
                    }
                }
                break;

        }
        return;

    }
}
