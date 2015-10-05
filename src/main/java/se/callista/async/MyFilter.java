package se.callista.async;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter(
        urlPatterns = {"/example"},
        asyncSupported = true,
        dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.ASYNC}
)
public class MyFilter implements Filter {

    private AsyncHttpClient client;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        client = (AsyncHttpClient)filterConfig.getServletContext().getAttribute("httpClient");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        response.getWriter().write("Filter::doFilter: >>> Start<br>");

        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            response.getWriter().write("Filter::doFilter: [REQUEST] Start >>> (" + Thread.currentThread() + ")<br>");
            final AsyncContext ac = request.startAsync();
            ac.addListener(new MyAsyncListener());

            client.prepareGet("http://checkip.amazonaws.com").execute(new AsyncCompletionHandler<Object>() {
                @Override
                public Object onCompleted(Response response) throws Exception {
                    ac.getRequest().setAttribute("ip", response.getResponseBody());
                    ac.getResponse().getWriter().write("Filter::doFilter: [REQUEST] AsyncResponse <<< (" + Thread.currentThread() + "), attr=" + response.getResponseBody() +" <br>");
                    ac.dispatch();
                    return null;
                }
            });
            response.getWriter().write("Filter::doFilter: [REQUEST] End <<< (" + Thread.currentThread() + ")<br>");
        } else if (request.getDispatcherType() == DispatcherType.ASYNC) {
            response.getWriter().write("Filter::doFilter: [ASYNC] before doFilter >>> (" + Thread.currentThread() + ")<br>");
            chain.doFilter(request, response);
            response.getWriter().write("Filter::doFilter: [ASYNC] after doFilter <<< (" + Thread.currentThread() + ")<br>");
            if (request.isAsyncStarted()) {
                MyAsyncListener l = request.getAsyncContext().createListener(MyAsyncListener.class);
                request.getAsyncContext().addListener(l);
            }
        }
        response.getWriter().write("Filter::doFilter: <<< End <br>");
    }

    @Override
    public void destroy() {

    }

    private static class MyAsyncListener implements AsyncListener {

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("AsyncListener::onComplete(" + Thread.currentThread() + ")<br>");
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("onTimeout\n");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("onError\n");
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getSuppliedResponse().getWriter().write("AsyncListener::onStartAsync(" + Thread.currentThread() + ")<br>");
        }
    }

}