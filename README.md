# async-servlet-with-filter

This project demonstrates asynchronous Servlet 3 filters.

Run the application in a Tomcat container or directly with Jetty
~~~
> ./gradlew runJetty
~~~

## Description
1. First the filter is called with dispatch type REQUEST
1. An _AsyncContext_ is started
1. A asynchronous request (to http://checkip.amazonaws.com) is done and the _doFilter_ method exits
1. When the callback on the asynchronous call returns the _dispatch()_ is called on the _AsyncContext_
1. The request is dispatched again through the _doFilter_ method, this time with dispatch type ASYNC
1. This time we call _chain.doFilter()_ and the request enters the servlets _doGet()_ method
1. A new _AsyncContext_ is started and a asynchronous dispatch to a JSP page is done on an other thread
1. The servlets _doGet()_ method exits and control is returned to the filter that registers an _AsyncListener_
2. The _onStartAsync()_ is called on the _AsyncListener_
3. The filters _doFilter()_ method exits
4. The response from the jsp is returned


Printout is something like this in Tomcat 8:
(In Jetty 9 the last row is missing...)
~~~
WaitFilter::doFilter: >>> Start
Filter::doFilter: [REQUEST] Start >>> (Thread[http-nio-8080-exec-6,5,main])
Filter::doFilter: [REQUEST] End <<< (Thread[http-nio-8080-exec-6,5,main])
Filter::doFilter: <<< End 
Filter::doFilter: [REQUEST] AsyncResponse <<< (Thread[New I/O worker #1,5,RMI Runtime]), attr=81.170.155.156 
Filter::doFilter: >>> Start
Filter::doFilter: [ASYNC] before doFilter >>> (Thread[http-nio-8080-exec-7,5,main])
Servlet::doGet: Start >>> (Thread[http-nio-8080-exec-7,5,main])
AsyncListener::onStartAsync(Thread[http-nio-8080-exec-7,5,main])
Servlet::doGet: End <<< (Thread[http-nio-8080-exec-7,5,main])
Filter::doFilter: [ASYNC] after doFilter <<< (Thread[http-nio-8080-exec-7,5,main])
Servlet::In Async(Thread[pool-1-thread-1,5,RMI Runtime])
Filter::doFilter: <<< End 
Hello in JSP (Thread[http-nio-8080-exec-8,5,main]) attr=81.170.155.156 
AsyncListener::onComplete(Thread[http-nio-8080-exec-8,5,main])
~~~
