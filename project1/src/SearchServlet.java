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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;


// Declaring a WebServlet called SearchServlet, which maps to url "/api/search"
@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {

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

        String col1 = request.getParameter("col1");
//        String col2 = request.getParameter("col2");
        String order1 = request.getParameter("order1");
        String order2 = request.getParameter("order2");

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
        HttpSession session = request.getSession();

        try (Connection connection = dataSource.getConnection()){

            // Retrieve parameter "name" from the http request, which refers to the value of <input name="name"> in index.html
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star_name = request.getParameter("star_name");
            session.setAttribute("lastTitle", title);
            session.setAttribute("lastYear", year);
            session.setAttribute("lastDirector", director);
            session.setAttribute("lastStarName", star_name);
            session.setAttribute("lastLimit", limit);
            session.setAttribute("lastOffset", offset);

            // Generate a SQL query
            // add all queries, remove formatting, replace with questions marks, add user entries to a dictionary
            // to check whether setInt vs setString, check type (only year should be int) using instanceOf

            String query = "SELECT DISTINCT m.*, r.rating,\n" +
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
                    "\tratings r ON r.movieId = m.id\n";

            boolean first = true;
            List<Object> search_params = new ArrayList<>();
            // add where statement
            if (title != null && !title.isEmpty())
            {
//                query += String.format("WHERE \n" +
//                        " \tm.title like '%%%s%%'\n", title);
                query += "WHERE m.title LIKE ?";
                search_params.add("%" + title + "%");
                first = false;
                session.setAttribute("searchTitle", title);
            }
            if (year != null && !year.isEmpty())
            {
                if (first)
                {
                    query += "WHERE \n";
                }
                else
                {
                    query += " and\n";
                }
//                query += String.format("\tm.year = '%s'", year);
                query += "m.year = ?";
                search_params.add(Integer.parseInt(year));
                first = false;
            }
            if (director != null && !director.isEmpty())
            {
                if (first)
                {
                    query += "WHERE \n";
                }
                else
                {
                    query += " and\n";
                }
//                query += String.format("\tm.director like '%%%s%%'", director);
                query += "m.director LIKE ?";
                search_params.add("%" + director + "%");
                first = false;
            }
            if (star_name != null && !star_name.isEmpty())
            {
                if (first)
                {
                    query += "WHERE \n";
                }
                else
                {
                    query += " and\n";
                }
                //query += String.format("\ts.name like '%%%s%%'", star_name);
                query += "s.name LIKE ?";
                search_params.add("%" + star_name + "%");
                first = false;
            }

            query += "\nORDER BY\n" +
                    sorting +
                    limitOffset;

            PreparedStatement ps = connection.prepareStatement(query);

            for (int i = 0; i < search_params.size(); i++)
            {
                Object param = search_params.get(i);
                if (param instanceof String)
                {
                    ps.setString(i + 1, (String) param);
                }
                else
                {
                    ps.setInt(i + 1, (Integer) param);
                }
            }

            // Log to localhost log
            request.getServletContext().log("query: " + query);

            session.setAttribute("lastQuery", query);
            // Perform the query
            ResultSet rs = ps.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs

            while (rs.next()) {
                //System.out.println("SearchServlet: "+rs.toString());

                String movie_id = rs.getString("m.id");
                String movie_title = rs.getString("m.title");
                String movie_year = rs.getString("m.year");
                String movie_director = rs.getString("m.director");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                float movie_price = rs.getFloat("m.price");
                float movie_rating = rs.getFloat("r.rating");
                boolean nullRating = rs.wasNull();

                System.out.println("SearchServlet: " + movie_id + " " + movie_title);
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

            // Close all structures
            rs.close();
            ps.close();


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