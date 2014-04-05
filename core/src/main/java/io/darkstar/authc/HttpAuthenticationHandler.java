package io.darkstar.authc;

import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.lang.Assert;
import com.stormpath.sdk.resource.ResourceException;

import java.util.Collections;
import java.util.List;

public class HttpAuthenticationHandler {

    /**
     * This class's private logger.
     */
    //private static final Logger log = LoggerFactory.getLogger(HttpAuthenticationHandler.class);

    /**
     * HTTP Authentication header, equal to <code>WWW-Authenticate</code>
     */
    protected static final String AUTHENTICATE_HEADER = "WWW-Authenticate";

    private List<HttpAuthenticationScheme> schemes = Collections.emptyList();

    private boolean propagateAuthenticationExceptions = false; //compatible with Shiro 1.2 and earlier

    private final Client client;
    private final Application application;

    public HttpAuthenticationHandler(Client client, String applicationHref) {
        this.client = client;
        this.application = this.client.getResource(applicationHref, Application.class);
    }

    /**
     * Returns the name to use in the ServletResponse's <b><code>WWW-Authenticate</code></b> header.
     *
     * <p>Per RFC 2617, this name name is displayed to the end user when they are asked to authenticate.</p>
     *
     * @return the name to use in the ServletResponse's 'WWW-Authenticate' header.
     */
    public String getApplicationName() {
        return this.application.getName();
    }

    public boolean isPropagateAuthenticationExceptions() {
        return propagateAuthenticationExceptions;
    }

    public void setPropagateAuthenticationExceptions(boolean propagateAuthenticationExceptions) {
        this.propagateAuthenticationExceptions = propagateAuthenticationExceptions;
    }

    public void setSchemes(List<HttpAuthenticationScheme> schemes) {
        Assert.notEmpty(schemes, HttpAuthenticationScheme.class.getSimpleName() + " list cannot be empty.");
        this.schemes = schemes;
    }

    public boolean authenticate(String username, String password, String host) {
        UsernamePasswordRequest request = new UsernamePasswordRequest(username, password, host);
        AuthenticationResult result = null;
        try {
            result = application.authenticateAccount(request);
            return true;
        } catch (ResourceException e) {
            return false;
        }
    }

    /**
     * Processes unauthenticated requests. It handles the two-stage request/challenge authentication protocol.
     *
     * @param request  incoming ServletRequest
     * @param response outgoing ServletResponse
     * @return true if the request should be processed; false if the request should not continue to be processed
     *
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
    boolean loggedIn = false; //false by default or we wouldn't be in this method
    AuthenticationToken token = getAuthenticationToken(request, response);
    if (token != null) {
    loggedIn = executeLogin(request, response, token);
    }
    if (!loggedIn) {
    sendChallenge(request, response, token);
    }
    return loggedIn;
    }

    protected boolean executeLogin(ServletRequest request, ServletResponse response, AuthenticationToken token)
    throws Exception {
    Assert.notNull(token);
    try {
    Subject subject = getSubject(request, response);
    subject.login(token);
    return onLoginSuccess(token, subject, request, response);
    } catch (AuthenticationException e) {
    if (isPropagateAuthenticationExceptions()) {
    throw e;
    } else {
    return onLoginFailure(token, e, request, response);
    }
    }
    }

     @SuppressWarnings("UnusedParameters")
     protected boolean onLoginSuccess(AuthenticationToken token, Subject subject,
     ServletRequest request, ServletResponse response) throws Exception {
     return true;
     }

     @SuppressWarnings("UnusedParameters")
     protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
     ServletRequest request, ServletResponse response) throws Exception {
     return false;
     }

     protected AuthenticationToken getAuthenticationToken(ServletRequest request, ServletResponse response)
     throws Exception {

     HttpServletRequest httpRequest = WebUtils.toHttp(request);
     HttpServletResponse httpResponse = WebUtils.toHttp(response);

     AuthenticationToken token = null;

     for (HttpAuthenticationScheme scheme : this.schemes) {
     token = scheme.getAuthenticationToken(httpRequest, httpResponse);
     if (token != null) {
     break;
     }
     }

     return token;
     }

     protected boolean sendChallenge(@SuppressWarnings("UnusedParameters") ServletRequest request,
     ServletResponse response, AuthenticationToken token) {
     if (log.isDebugEnabled()) {
     log.debug("Authentication required: sending 401 Authentication challenge response.");
     }

     HttpServletRequest httpRequest = WebUtils.toHttp(request);
     HttpServletResponse httpResponse = WebUtils.toHttp(response);
     httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

     for (HttpAuthenticationScheme scheme : this.schemes) {
     String authcHeader = getAuthenticateHeader(scheme);
     httpResponse.addHeader(AUTHENTICATE_HEADER, authcHeader);
     }

     if (this.authenticationChallengeListener != null) {
     this.authenticationChallengeListener.onHttpAuthenticationChallengeEvent(
     new DefaultHttpAuthenticationChallengeEvent(httpRequest, httpResponse, token)
     );
     }

     return false;
     }

     protected String getAuthenticateHeader(HttpAuthenticationScheme scheme) {
     return scheme.getName() + " realm=\"" + getApplicationName() + "\"";
     }

     @Override public void doFilterInternal(ServletRequest request, ServletResponse response, FilterChain chain)
     throws ServletException, IOException {

     Exception exception = null;

     //If an authentication mechanism requires the entire body (e.g. as would be the case for a
     //digest-based authentication scheme that uses the body when calculating the digest), we need
     //to wrap the original input stream in case we need to roll back later for normal request
     //processing:

     HttpServletRequest originalRequest = (HttpServletRequest)request;

     request = new BodyCachingRequestWrapper(originalRequest);

     try {

     boolean continueChain = preHandle(request, response);
     if (log.isTraceEnabled()) {
     log.trace("Invoked preHandle method.  Continuing chain?: [" + continueChain + "]");
     }

     if (!((BodyCachingRequestWrapper)request).isBodyTransferredToMemory()) {
     //no need to pass on our wrapper if the body wasn't read:
     request = originalRequest;
     }

     if (continueChain) {
     executeChain(request, response, chain);
     }

     postHandle(request, response);
     if (log.isTraceEnabled()) {
     log.trace("Successfully invoked postHandle method");
     }

     } catch (Exception e) {
     exception = e;
     } finally {
     cleanup(request, response, exception);
     }
     }

     private static class BodyCachingRequestWrapper extends HttpServletRequestWrapper {

     private final ServletInputStream inputStream;

     private BufferedReader reader;

     private BodyCachingRequestWrapper(HttpServletRequest request) throws IOException {
     super(request);
     this.inputStream = new CachingInputStream(request.getInputStream());
     }

     public boolean isBodyTransferredToMemory() {
     return this.inputStream != null && ((CachingInputStream)this.inputStream).isTransferredToMemory();
     }

     @Override public ServletInputStream getInputStream() throws IOException {
     return inputStream;
     }

     @Override public String getCharacterEncoding() {
     return super.getCharacterEncoding() != null ? super.getCharacterEncoding() :
     org.springframework.web.util.WebUtils.DEFAULT_CHARACTER_ENCODING;
     }

     @Override public BufferedReader getReader() throws IOException {
     if (this.reader == null) {
     this.reader = new BufferedReader(new InputStreamReader(inputStream, getCharacterEncoding()));
     }
     return this.reader;
     }

     private class CachingInputStream extends ServletInputStream {

     private final ServletInputStream source;

     private boolean transferredToMemory = false;
     private InputStream bais;

     private CachingInputStream(ServletInputStream source) {
     this.source = source;
     }

     public boolean isTransferredToMemory() {
     return transferredToMemory;
     }

     @Override public int available() throws IOException {
     ensureInMemory();
     return bais.available();
     }

     @Override public void close() throws IOException {
     if (bais != null) {
     bais.close();
     }
     }

     @Override public boolean markSupported() {
     return true;
     }

     @Override public synchronized void mark(int i) {
     if (bais == null) {
     if (i > 0) {
     try {
     ensureInMemory();
     this.bais.mark(i);
     } catch (IOException e) {
     throw new IllegalStateException(e);
     }
     }
     } else {
     this.bais.mark(i);
     }
     }

     @Override public synchronized void reset() throws IOException {
     if (bais != null) {
     bais.reset();
     }
     }

     @Override public int read() throws IOException {
     ensureInMemory();
     return bais.read();
     }

     private void ensureInMemory() throws IOException {
     if (!transferredToMemory) {
     //TODO THIS WILL CAUSE SERVER MEMORY ERRORS IF NOT CLEANED UP
     //(need to throw an exception if the payload is too large, i.e. we've consumed
     // more than N bytes)
     byte[] bytes = toBytes(source);
     this.bais = new ByteArrayInputStream(bytes);
     transferredToMemory = true;
     }
     }

     protected byte[] toBytes(InputStream in) throws IOException {
     if (in == null) {
     throw new IllegalArgumentException("InputStream argument cannot be null.");
     }
     final int BUFFER_SIZE = 512;
     ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
     byte[] buffer = new byte[BUFFER_SIZE];
     int bytesRead;
     try {
     while ((bytesRead = in.read(buffer)) != -1) {
     out.write(buffer, 0, bytesRead);
     }
     return out.toByteArray();
     } finally {
     try {
     in.close();
     } catch (IOException ignored) {
     }
     try {
     out.close();
     } catch (IOException ignored) {
     }
     }
     }

     }

     }
     */

}
