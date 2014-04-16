package io.darkstar.log;

import io.netty.handler.codec.http.CookieDecoder;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.apache.shiro.util.CollectionUtils;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

/**
 * Implements *only* the functionality required to support AccessLog entries via Logback's AccessLog implementation.
 */
@SuppressWarnings("deprecation")
public class AccessLogHttpServletRequest extends AccessLogHttpMessage implements HttpServletRequest {

    private final HttpRequest request;
    private final URI requestUri;

    private Cookie[] decodedServletCookies;

    public AccessLogHttpServletRequest(HttpRequest request) {
        this.request = request;
        try {
            this.requestUri = new URI(request.getUri());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("HttpRequest has an invalid URI", e);
        }
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Cookie[] getCookies() {

        if (decodedServletCookies != null) {
            return decodedServletCookies;
        }

        List<String> cookieStrings = request.headers().getAll(COOKIE);
        if (CollectionUtils.isEmpty(cookieStrings)) {
            decodedServletCookies = new Cookie[0];
            return decodedServletCookies;
        }

        List<Cookie> servletCookies = new ArrayList<>(cookieStrings.size());

        for (String cookieString : cookieStrings) {

            Set<io.netty.handler.codec.http.Cookie> nettyCookies = CookieDecoder.decode(cookieString);

            for (io.netty.handler.codec.http.Cookie nettyCookie : nettyCookies) {
                Cookie servletCookie = new Cookie(nettyCookie.getName(), nettyCookie.getValue());
                servletCookie.setComment(nettyCookie.getComment());
                servletCookie.setDomain(nettyCookie.getDomain());
                servletCookie.setHttpOnly(nettyCookie.isHttpOnly());
                servletCookie.setMaxAge((int) nettyCookie.getMaxAge());
                servletCookie.setPath(nettyCookie.getPath());
                servletCookie.setSecure(nettyCookie.isSecure());
                servletCookie.setVersion(nettyCookie.getVersion());

                servletCookies.add(servletCookie);
            }
        }

        decodedServletCookies = new Cookie[servletCookies.size()];
        servletCookies.toArray(decodedServletCookies);

        return decodedServletCookies;
    }

    @Override
    public long getDateHeader(String name) {
        try {
            return HttpHeaders.getDateHeader(request, name).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Unable to parse header " + name + " for date value: ", e);
        }
    }

    @Override
    public String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(request.headers().getAll(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.headers().names());
    }

    @Override
    public int getIntHeader(String name) {
        return HttpHeaders.getIntHeader(request, name);
    }

    @Override
    public String getMethod() {
        return request.getMethod().name();
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getQueryString() {
        return requestUri.getQuery();
    }

    @Override
    public String getRemoteUser() {
        //This value should be inserted into the modified request by any authentication module.
        //TODO: get value from modified request
        //(the request object wrapped by this instance is obtained too 'early' during request chain processing and
        //therefore cannot obtain any modified request values
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getRequestURI() {
        return request.getUri();
    }

    @Override
    public StringBuffer getRequestURL() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public HttpSession getSession(boolean create) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Object getAttribute(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getCharacterEncoding() {
        return request.headers().get(CONTENT_ENCODING);
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getContentLength() {
        return (int) getContentLengthLong();
    }

    @Override
    public long getContentLengthLong() {
        return HttpHeaders.getContentLength(request, -1l);
    }

    @Override
    public String getContentType() {
        return request.headers().get(CONTENT_TYPE);
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getParameter(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String[] getParameterValues(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getProtocol() {
        return "HTTP/" + request.getProtocolVersion();
    }

    @Override
    public String getScheme() {
        if (isSecure()) {
            return "https";
        }
        return "http";
    }

    @Override
    public String getServerName() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getServerPort() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getRemoteAddr() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getRemoteHost() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setAttribute(String name, Object o) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void removeAttribute(String name) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isSecure() {
        //This requires access to knowledge of ssl being used or not - that is not currently supported by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getRealPath(String path) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getRemotePort() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getLocalName() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String getLocalAddr() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getLocalPort() {
        //This requires access to the request host information - that is not currently retained by
        //the netty HttpRequest.  This means Darkstar needs to create its own request/response
        //object (that wraps the netty request/response) that provides this additional information.
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
}
