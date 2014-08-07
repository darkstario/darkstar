package io.darkstar.http;

/** Some common HTTP Header names, useful as constants to help avoid typos. */
@SuppressWarnings("UnusedDeclaration")
public final class HeaderName {

    /** {@code "Accept"} */
    public static final CharSequence ACCEPT                           = "Accept";
    /** {@code "Accept-Charset"} */
    public static final CharSequence ACCEPT_CHARSET                   = "Accept-Charset";
    /** {@code "Accept-Encoding"} */
    public static final CharSequence ACCEPT_ENCODING                  = "Accept-Encoding";
    /** {@code "Accept-Language"} */
    public static final CharSequence ACCEPT_LANGUAGE                  = "Accept-Language";
    /** {@code "Accept-Ranges"} */
    public static final CharSequence ACCEPT_RANGES                    = "Accept-Ranges";
    /** {@code "Accept-Patch"} */
    public static final CharSequence ACCEPT_PATCH                     = "Accept-Patch";
    /** {@code "Access-Control-Allow-Credentials"} */
    public static final CharSequence ACCESS_CONTROL_ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials";
    /** {@code "Access-Control-Allow-Headers"} */
    public static final CharSequence ACCESS_CONTROL_ALLOW_HEADERS     = "Access-Control-Allow-Headers";
    /** {@code "Access-Control-Allow-Methods"} */
    public static final CharSequence ACCESS_CONTROL_ALLOW_METHODS     = "Access-Control-Allow-Methods";
    /** {@code "Access-Control-Allow-Origin"} */
    public static final CharSequence ACCESS_CONTROL_ALLOW_ORIGIN      = "Access-Control-Allow-Origin";
    /** {@code "Access-Control-Expose-Headers"} */
    public static final CharSequence ACCESS_CONTROL_EXPOSE_HEADERS    = "Access-Control-Expose-Headers";
    /** {@code "Access-Control-Max-Age"} */
    public static final CharSequence ACCESS_CONTROL_MAX_AGE           = "Access-Control-Max-Age";
    /** {@code "Access-Control-Request-Headers"} */
    public static final CharSequence ACCESS_CONTROL_REQUEST_HEADERS   = "Access-Control-Request-Headers";
    /** {@code "Access-Control-Request-Method"} */
    public static final CharSequence ACCESS_CONTROL_REQUEST_METHOD    = "Access-Control-Request-Method";
    /** {@code "Age"} */
    public static final CharSequence AGE                              = "Age";
    /** {@code "Allow"} */
    public static final CharSequence ALLOW                            = "Allow";
    /** {@code "Authorization"} */
    public static final CharSequence AUTHORIZATION                    = "Authorization";
    /** {@code "Cache-Control"} */
    public static final CharSequence CACHE_CONTROL                    = "Cache-Control";
    /** {@code "Connection"} */
    public static final CharSequence CONNECTION                       = "Connection";
    /** {@code "Content-Base"} */
    public static final CharSequence CONTENT_BASE                     = "Content-Base";
    /** {@code "Content-Encoding"} */
    public static final CharSequence CONTENT_ENCODING                 = "Content-Encoding";
    /** {@code "Content-Language"} */
    public static final CharSequence CONTENT_LANGUAGE                 = "Content-Language";
    /** {@code "Content-Length"} */
    public static final CharSequence CONTENT_LENGTH                   = "Content-Length";
    /** {@code "Content-Location"} */
    public static final CharSequence CONTENT_LOCATION                 = "Content-Location";
    /** {@code "Content-Transfer-Encoding"} */
    public static final CharSequence CONTENT_TRANSFER_ENCODING        = "Content-Transfer-Encoding";
    /** {@code "Content-MD5"} */
    public static final CharSequence CONTENT_MD5                      = "Content-MD5";
    /** {@code "Content-Range"} */
    public static final CharSequence CONTENT_RANGE                    = "Content-Range";
    /** {@code "Content-Type"} */
    public static final CharSequence CONTENT_TYPE                     = "Content-Type";
    /** {@code "Cookie"} */
    public static final CharSequence COOKIE                           = "Cookie";
    /** {@code "Date"} */
    public static final CharSequence DATE                             = "Date";
    /** {@code "ETag"} */
    public static final CharSequence ETAG                             = "ETag";
    /** {@code "Expect"} */
    public static final CharSequence EXPECT                           = "Expect";
    /** {@code "Expires"} */
    public static final CharSequence EXPIRES                          = "Expires";
    /** {@code "From"} */
    public static final CharSequence FROM                             = "From";
    /** {@code "Host"} */
    public static final CharSequence HOST                             = "Host";
    /** {@code "If-Match"} */
    public static final CharSequence IF_MATCH                         = "If-Match";
    /** {@code "If-Modified-Since"} */
    public static final CharSequence IF_MODIFIED_SINCE                = "If-Modified-Since";
    /** {@code "If-None-Match"} */
    public static final CharSequence IF_NONE_MATCH                    = "If-None-Match";
    /** {@code "If-Range"} */
    public static final CharSequence IF_RANGE                         = "If-Range";
    /** {@code "If-Unmodified-Since"} */
    public static final CharSequence IF_UNMODIFIED_SINCE              = "If-Unmodified-Since";
    /** {@code "Last-Modified"} */
    public static final CharSequence LAST_MODIFIED                    = "Last-Modified";
    /** {@code "Location"} */
    public static final CharSequence LOCATION                         = "Location";
    /** {@code "Max-Forwards"} */
    public static final CharSequence MAX_FORWARDS                     = "Max-Forwards";
    /** {@code "Origin"} */
    public static final CharSequence ORIGIN                           = "Origin";
    /** {@code "Pragma"} */
    public static final CharSequence PRAGMA                           = "Pragma";
    /** {@code "Proxy-Authenticate"} */
    public static final CharSequence PROXY_AUTHENTICATE               = "Proxy-Authenticate";
    /** {@code "Proxy-Authorization"} */
    public static final CharSequence PROXY_AUTHORIZATION              = "Proxy-Authorization";
    /** {@code "Range"} */
    public static final CharSequence RANGE                            = "Range";
    /** {@code "Referer"} */
    public static final CharSequence REFERER                          = "Referer";
    /** {@code "Retry-After"} */
    public static final CharSequence RETRY_AFTER                      = "Retry-After";
    /** {@code "Sec-WebSocket-Key1"} */
    public static final CharSequence SEC_WEBSOCKET_KEY1               = "Sec-WebSocket-Key1";
    /** {@code "Sec-WebSocket-Key2"} */
    public static final CharSequence SEC_WEBSOCKET_KEY2               = "Sec-WebSocket-Key2";
    /** {@code "Sec-WebSocket-Location"} */
    public static final CharSequence SEC_WEBSOCKET_LOCATION           = "Sec-WebSocket-Location";
    /** {@code "Sec-WebSocket-Origin"} */
    public static final CharSequence SEC_WEBSOCKET_ORIGIN             = "Sec-WebSocket-Origin";
    /** {@code "Sec-WebSocket-Protocol"} */
    public static final CharSequence SEC_WEBSOCKET_PROTOCOL           = "Sec-WebSocket-Protocol";
    /** {@code "Sec-WebSocket-Version"} */
    public static final CharSequence SEC_WEBSOCKET_VERSION            = "Sec-WebSocket-Version";
    /** {@code "Sec-WebSocket-Key"} */
    public static final CharSequence SEC_WEBSOCKET_KEY                = "Sec-WebSocket-Key";
    /** {@code "Sec-WebSocket-Accept"} */
    public static final CharSequence SEC_WEBSOCKET_ACCEPT             = "Sec-WebSocket-Accept";
    /** {@code "Server"} */
    public static final CharSequence SERVER                           = "Server";
    /** {@code "Set-Cookie"} */
    public static final CharSequence SET_COOKIE                       = "Set-Cookie";
    /** {@code "Set-Cookie2"} */
    public static final CharSequence SET_COOKIE2                      = "Set-Cookie2";
    /** {@code "TE"} */
    public static final CharSequence TE                               = "TE";
    /** {@code "Trailer"} */
    public static final CharSequence TRAILER                          = "Trailer";
    /** {@code "Transfer-Encoding"} */
    public static final CharSequence TRANSFER_ENCODING                = "Transfer-Encoding";
    /** {@code "Upgrade"} */
    public static final CharSequence UPGRADE                          = "Upgrade";
    /** {@code "User-Agent"} */
    public static final CharSequence USER_AGENT                       = "User-Agent";
    /** {@code "Vary"} */
    public static final CharSequence VARY                             = "Vary";
    /** {@code "Via"} */
    public static final CharSequence VIA                              = "Via";
    /** {@code "Warning"} */
    public static final CharSequence WARNING                          = "Warning";
    /** {@code "WebSocket-Location"} */
    public static final CharSequence WEBSOCKET_LOCATION               = "WebSocket-Location";
    /** {@code "WebSocket-Origin"} */
    public static final CharSequence WEBSOCKET_ORIGIN                 = "WebSocket-Origin";
    /** {@code "WebSocket-Protocol"} */
    public static final CharSequence WEBSOCKET_PROTOCOL               = "WebSocket-Protocol";
    /** {@code "WWW-Authenticate"} */
    public static final CharSequence WWW_AUTHENTICATE                 = "WWW-Authenticate";
    /** {@code "X-Forwarded-For"} */
    public static final CharSequence X_FORWARDED_FOR                  = "X-Forwarded-For";
    /** {@code "X-Forwarded-Host"} */
    public static final CharSequence X_FORWARDED_HOST                 = "X-Forwarded-Host";

    private HeaderName() {
    }
}
