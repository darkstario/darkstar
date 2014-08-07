package io.darkstar.http;

/**
 * Some common HTTP Header values, useful as constants to help avoid typos.
 */
@SuppressWarnings("UnusedDeclaration")
public final class HeaderValue {

    /** {@code "application/x-www-form-urlencoded"} */
    public static final CharSequence APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";
    /** {@code "base64"} */
    public static final CharSequence BASE64                            = "base64";
    /** {@code "binary"} */
    public static final CharSequence BINARY                            = "binary";
    /** {@code "boundary"} */
    public static final CharSequence BOUNDARY                          = "boundary";
    /** {@code "bytes"} */
    public static final CharSequence BYTES                             = "bytes";
    /** {@code "charset"} */
    public static final CharSequence CHARSET                           = "charset";
    /** {@code "chunked"} */
    public static final CharSequence CHUNKED                           = "chunked";
    /** {@code "close"} */
    public static final CharSequence CLOSE                             = "close";
    /** {@code "compress"} */
    public static final CharSequence COMPRESS                          = "compress";
    /** {@code "100-continue"} */
    public static final CharSequence CONTINUE                          = "100-continue";
    /** {@code "deflate"} */
    public static final CharSequence DEFLATE                           = "deflate";
    /** {@code "gzip"} */
    public static final CharSequence GZIP                              = "gzip";
    /** {@code "identity"} */
    public static final CharSequence IDENTITY                          = "identity";
    /** {@code "keep-alive"} */
    public static final CharSequence KEEP_ALIVE                        = "keep-alive";
    /** {@code "max-age"} */
    public static final CharSequence MAX_AGE                           = "max-age";
    /** {@code "max-stale"} */
    public static final CharSequence MAX_STALE                         = "max-stale";
    /** {@code "min-fresh"} */
    public static final CharSequence MIN_FRESH                         = "min-fresh";
    /** {@code "multipart/form-data"} */
    public static final CharSequence MULTIPART_FORM_DATA               = "multipart/form-data";
    /** {@code "must-revalidate"} */
    public static final CharSequence MUST_REVALIDATE                   = "must-revalidate";
    /** {@code "no-cache"} */
    public static final CharSequence NO_CACHE                          = "no-cache";
    /** {@code "no-store"} */
    public static final CharSequence NO_STORE                          = "no-store";
    /** {@code "no-transform"} */
    public static final CharSequence NO_TRANSFORM                      = "no-transform";
    /** {@code "none"} */
    public static final CharSequence NONE                              = "none";
    /** {@code "only-if-cached"} */
    public static final CharSequence ONLY_IF_CACHED                    = "only-if-cached";
    /** {@code "private"} */
    public static final CharSequence PRIVATE                           = "private";
    /** {@code "proxy-revalidate"} */
    public static final CharSequence PROXY_REVALIDATE                  = "proxy-revalidate";
    /** {@code "public"} */
    public static final CharSequence PUBLIC                            = "public";
    /** {@code "quoted-printable"} */
    public static final CharSequence QUOTED_PRINTABLE                  = "quoted-printable";
    /** {@code "s-maxage"} */
    public static final CharSequence S_MAXAGE                          = "s-maxage";
    /** {@code "trailers"} */
    public static final CharSequence TRAILERS                          = "trailers";
    /** {@code "Upgrade"} */
    public static final CharSequence UPGRADE                           = "Upgrade";
    /** {@code "WebSocket"} */
    public static final CharSequence WEBSOCKET                         = "WebSocket";

    private HeaderValue() {
    }
}
