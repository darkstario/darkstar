package io.darkstar.log;

import io.netty.handler.codec.http.HttpResponse;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;

public class AccessLogHttpServletResponse extends AccessLogHttpMessage implements HttpServletResponse {

    private final HttpResponse response;

    public AccessLogHttpServletResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean containsHeader(String name) {
        return response.headers().get(name) != null;
    }

    @Override
    public String encodeURL(String url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String encodeRedirectURL(String url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String encodeUrl(String url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void sendError(int sc) throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setDateHeader(String name, long date) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void addDateHeader(String name, long date) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setHeader(String name, String value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setIntHeader(String name, int value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void addIntHeader(String name, int value) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setStatus(int sc) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getStatus() {
        return response.getStatus().code();
    }

    @Override
    public String getHeader(String name) {
        return response.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return response.headers().getAll(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.headers().names();
    }

    @Override
    public String getCharacterEncoding() {
        return response.headers().get(CONTENT_ENCODING);
    }

    @Override
    public String getContentType() {
        return response.headers().get(CONTENT_TYPE);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setCharacterEncoding(String charset) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setContentLength(int len) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setContentLengthLong(long len) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setContentType(String type) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setBufferSize(int size) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void flushBuffer() throws IOException {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public boolean isCommitted() {
        return true; //should pretty much always be considered committed for access log entry purposes
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public void setLocale(Locale loc) {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
}
