import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Servlet Filter implementation class LoginFilter
 */
@WebFilter(filterName = "LoginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    private final ArrayList<String> allowedURIs = new ArrayList<>();

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String uri = httpRequest.getRequestURI().toLowerCase();
        HttpSession session = httpRequest.getSession(false);

        //System.out.println("LoginFilter: " + httpRequest.getRequestURI());

        // Check if this URL is allowed to access without logging in
        if (this.isUrlAllowedWithoutLogin(httpRequest.getRequestURI())) {
            // Keep default action: pass along the filter chain
            chain.doFilter(request, response);
            return;
        }

        boolean isDashboardAccess = uri.contains("_dashboard");

        if (isDashboardAccess) {
            boolean isEmployeeLoggedIn = session != null && session.getAttribute("employeeEmail") != null;
            if (!isEmployeeLoggedIn) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/_dashboard.html");
                return;
            }
        } else {
            boolean isCustomerLoggedIn = session != null && session.getAttribute("email") != null;
            if (!isCustomerLoggedIn) {
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login.html");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isUrlAllowedWithoutLogin(String requestURI) {
        /*
         Setup your own rules here to allow accessing some resources without logging in
         Always allow your own login related requests(html, js, servlet, etc..)
         You might also want to allow some CSS files, etc..
         */
        String lowerCaseURI = requestURI.toLowerCase();
        return allowedURIs.stream().anyMatch(lowerCaseURI::endsWith)
                || lowerCaseURI.endsWith(".js")
                || lowerCaseURI.contains("recaptcha")
                || lowerCaseURI.contains("api");
    }

    public void init(FilterConfig fConfig) {
        allowedURIs.add("login.html");
        allowedURIs.add("_dashboard.html");
    }

    public void destroy() {
        // ignored.
    }

}
