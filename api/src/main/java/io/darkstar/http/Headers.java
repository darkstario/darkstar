package io.darkstar.http;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

public interface Headers {

    Set<String> getNames();

    String getValue(CharSequence headerName);

    List<String> getValues(CharSequence headerName);

    /**
     * Returns the date value of the header with the specified name.  If there is more than one header value, the first
     * value is returned.
     *
     * @return the header value as a date
     * @throws ParseException if there is no such header or the value is not an HTTP-compatible formatted date
     */
    ZonedDateTime getDate(CharSequence headerName) throws ParseException;

    /**
     * Returns the date value of the header with the specified name or the {@code defaultValue} if the header does not
     * exist or cannot be parsed.  If there is more than one value, the first will be parsed.
     *
     * @return the first date value or the {@code defaultValue} if the value does not exist or it is not a an
     *         HTTP-compatible formatted date
     */
    ZonedDateTime getDate(CharSequence headerName, ZonedDateTime defaultValue);

    Integer getInteger(CharSequence headerName) throws NumberFormatException;

    Integer getInteger(CharSequence headerName, Integer defaultValue);

    Long getLong(CharSequence headerName) throws NumberFormatException;

    Long getLong(CharSequence headerName, Long defaultValue);

    Float getFloat(CharSequence headerName) throws NumberFormatException;

    Float getFloat(CharSequence headerName, Float defaultValue);

    Double getDouble(CharSequence headerName) throws NumberFormatException;

    Double getDouble(CharSequence headerName, Double defaultValue);

    BigInteger getBigInteger(CharSequence headerName) throws NumberFormatException;

    BigInteger getBigInteger(CharSequence headerName, BigInteger defaultValue);

    BigDecimal getBigDecimal(CharSequence headerName) throws NumberFormatException;

    BigDecimal getBigDecimal(CharSequence headerName, BigDecimal defaultValue);

}
