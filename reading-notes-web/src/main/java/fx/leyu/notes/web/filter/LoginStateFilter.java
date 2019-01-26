package fx.leyu.notes.web.filter;

import fx.leyu.notes.service.PeopleService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class LoginStateFilter implements Filter {
    private PeopleService peopleService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext context = filterConfig.getServletContext();
        WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(context);
        peopleService = springContext.getBean(PeopleService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (checkLoginStatus(httpServletRequest.getSession())) {
            chain.doFilter(request, response);
            return;
        }

        String pin = httpServletRequest.getParameter("pin");
        if (StringUtils.isBlank(pin)) {
            write(response, "请携带 pin 信息");
            return;
        }


        if (peopleService.isValid(pin) || peopleService.register(pin)) {
            logOnStatus(httpServletRequest.getSession());
            chain.doFilter(request, response);
        } else {
            write(response, "请携带一个独特的 pin");
        }
    }

    private void logOnStatus(HttpSession session) {
        if (session != null) {
            session.setAttribute("login_status", Boolean.TRUE);
        }
    }

    private boolean checkLoginStatus(HttpSession session) {
        if (session == null) {
            return false;
        }

        return Boolean.TRUE.equals(session.getAttribute("login_status"));
    }

    @Override
    public void destroy() {
        // do nothing
    }

    private void write(ServletResponse resp, String json) {
        resp.setCharacterEncoding("utf-8");
        try (PrintWriter writer = resp.getWriter()) {
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
