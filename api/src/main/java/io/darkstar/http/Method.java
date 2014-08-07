package io.darkstar.http;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * The request getMethod of HTTP or its derived protocols, such as
 * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
 * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>.
 */
public class Method implements CharSequence, Comparable<Method> {

    /**
     * The OPTIONS getMethod represents a request for information about the communication options
     * available on the request/response chain identified by the Request-URI. This getMethod allows
     * the client to determine the options and/or requirements associated with a resource, or the
     * capabilities of a server, without implying a resource action or initiating a resource
     * retrieval.
     */
    public static final Method OPTIONS = new Method("OPTIONS");

    /**
     * The GET getMethod means retrieve whatever information (in the form of an entity) is identified
     * by the Request-URI.  If the Request-URI refers to a data-producing process, it is the
     * produced data which shall be returned as the entity in the response and not the source text
     * of the process, unless that text happens to be the output of the process.
     */
    public static final Method GET = new Method("GET");

    /**
     * The HEAD getMethod is identical to GET except that the server MUST NOT return a message-body
     * in the response.
     */
    public static final Method HEAD = new Method("HEAD");

    /**
     * The POST getMethod is used to request that the origin server accept the entity enclosed in the
     * request as a new subordinate of the resource identified by the Request-URI in the
     * Request-Line.
     */
    public static final Method POST = new Method("POST");

    /** The PUT getMethod requests that the enclosed entity be stored under the supplied Request-URI. */
    public static final Method PUT = new Method("PUT");

    /**
     * The PATCH getMethod requests that a set of changes described in the
     * request entity be applied to the resource identified by the Request-URI.
     */
    public static final Method PATCH = new Method("PATCH");

    /**
     * The DELETE getMethod requests that the origin server delete the resource identified by the
     * Request-URI.
     */
    public static final Method DELETE = new Method("DELETE");

    /**
     * The TRACE getMethod is used to invoke a remote, application-layer loop- back of the request
     * message.
     */
    public static final Method TRACE = new Method("TRACE");

    /**
     * This specification reserves the getMethod name CONNECT for use with a proxy that can dynamically
     * switch to being a tunnel
     */
    public static final Method CONNECT = new Method("CONNECT");

    private static final Map<String, Method> methodMap = new HashMap<>();

    static {
        methodMap.put(OPTIONS.toString(), OPTIONS);
        methodMap.put(GET.toString(), GET);
        methodMap.put(HEAD.toString(), HEAD);
        methodMap.put(POST.toString(), POST);
        methodMap.put(PUT.toString(), PUT);
        methodMap.put(PATCH.toString(), PATCH);
        methodMap.put(DELETE.toString(), DELETE);
        methodMap.put(TRACE.toString(), TRACE);
        methodMap.put(CONNECT.toString(), CONNECT);
    }

    private final String name;

    /**
     * Returns the {@link Method} represented by the specified name.
     * If the specified name is a standard HTTP getMethod name, a cached instance
     * will be returned.  Otherwise, a new instance will be returned.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static Method valueOf(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        Method result = methodMap.get(name.toUpperCase(Locale.ENGLISH));
        if (result != null) {
            return result;
        } else {
            return new Method(name);
        }
    }

    /**
     * Creates a new HTTP getMethod with the specified name.  You will not need to
     * create a new getMethod unless you are implementing a protocol derived from
     * HTTP, such as
     * <a href="http://en.wikipedia.org/wiki/Real_Time_Streaming_Protocol">RTSP</a> and
     * <a href="http://en.wikipedia.org/wiki/Internet_Content_Adaptation_Protocol">ICAP</a>
     */
    private Method(String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }

        name = name.trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("empty name");
        }

        for (int i = 0; i < name.length(); i++) {
            if (Character.isISOControl(name.charAt(i)) ||
                Character.isWhitespace(name.charAt(i))) {
                throw new IllegalArgumentException("invalid character in name");
            }
        }

        this.name = name;
        //this.byteValue = this.name.getBytes(Charsets.US_ASCII);
    }

    /** Returns the name of this getMethod. */
    public String name() {
        return name;
    }

    @Override
    public int hashCode() {
        return name().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Method)) {
            return false;
        }

        Method that = (Method) o;
        return name().equals(that.name());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Method o) {
        return name().compareTo(o.name());
    }

    @Override
    public int length() {
        return name.length();
    }

    @Override
    public char charAt(int index) {
        return name.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return name.subSequence(start, end);
    }

    @Override
    public IntStream chars() {
        return name.chars();
    }

    @Override
    public IntStream codePoints() {
        return name.codePoints();
    }
}
