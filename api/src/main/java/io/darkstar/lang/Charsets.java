package io.darkstar.lang;

import java.nio.charset.Charset;

public class Charsets {

    /**
     * 16-bit UTF (UCS Transformation Format) whose byte order is identified by
     * an optional byte-order mark
     */
    public static final Charset UTF_16 = Charset.forName("UTF-16");

    /**
     * 16-bit UTF (UCS Transformation Format) whose byte order is big-endian
     */
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");

    /**
     * 16-bit UTF (UCS Transformation Format) whose byte order is little-endian
     */
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    /**
     * 8-bit UTF (UCS Transformation Format)
     */
    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * ISO Latin Alphabet No. 1, as known as <tt>ISO-LATIN-1</tt>
     */
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

    /**
     * 7-bit ASCII, as known as ISO646-US or the Basic Latin block of the
     * Unicode character set
     */
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
}
