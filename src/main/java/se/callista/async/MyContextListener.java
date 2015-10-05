package se.callista.async;


import com.ning.http.client.AsyncHttpClient;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@WebListener
public class MyContextListener implements ServletContextListener {

    private ExecutorService executorService;

    private AsyncHttpClient httpClient;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        executorService = Executors.newFixedThreadPool(25);
        httpClient = new AsyncHttpClient();
        sce.getServletContext().setAttribute("executorService", executorService);
        sce.getServletContext().setAttribute("httpClient", httpClient);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        executorService.shutdownNow();
        httpClient.close();
    }
}