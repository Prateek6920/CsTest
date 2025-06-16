import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;

public class TaskApp {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletHandler handler = new ServletHandler();
        server.setHandler(handler);
        handler.addServletWithMapping(TaskServlet.class, "/");
        server.start();
        System.out.println("Server running at http://localhost:8080");
        server.join();
    }

    @WebServlet("/")
    public static class TaskServlet extends HttpServlet {
        private List<Task> tasks = new ArrayList<>();
        private int counter = 1;

        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.setContentType("text/html");
            var out = resp.getWriter();
            out.println("<html><body><h2>Task List</h2>");
            out.println("<form method='POST'>"
                      + "<input name='desc' required>"
                      + "<button type='submit'>Add Task</button>"
                      + "</form><ul>");
            for (Task task : tasks) {
                out.printf("<li>%s %s <a href='?done=%d'>✓</a> <a href='?del=%d'>🗑</a></li>",
                        task.done ? "<s>" + task.desc + "</s>" : task.desc,
                        task.done ? "(Done)" : "",
                        task.id, task.id);
            }
            out.println("</ul></body></html>");
        }

        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            String desc = req.getParameter("desc");
            if (desc != null && !desc.isBlank()) {
                tasks.add(new Task(counter++, desc));
            }
            resp.sendRedirect("/");
        }

        protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            int id = Integer.parseInt(req.getParameter("id"));
            tasks.removeIf(t -> t.id == id);
            resp.sendRedirect("/");
        }

        protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            int id = Integer.parseInt(req.getParameter("id"));
            tasks.stream().filter(t -> t.id == id).findFirst().ifPresent(t -> t.done = true);
            resp.sendRedirect("/");
        }

        protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
            if ("GET".equals(req.getMethod())) {
                if (req.getParameter("del") != null) {
                    tasks.removeIf(t -> t.id == Integer.parseInt(req.getParameter("del")));
                    resp.sendRedirect("/");
                    return;
                }
                if (req.getParameter("done") != null) {
                    tasks.stream().filter(t -> t.id == Integer.parseInt(req.getParameter("done"))).findFirst().ifPresent(t -> t.done = true);
                    resp.sendRedirect("/");
                    return;
                }
            }
            super.service(req, resp);
        }
    }

    public static class Task {
        int id;
        String desc;
        boolean done;
        public Task(int id, String desc) {
            this.id = id;
            this.desc = desc;
            this.done = false;
        }
    }
}
