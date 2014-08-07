package io.darkstar.http;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpVersion;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class NettyHeaders implements MutableHeaders {

    private final HttpHeaders NETTY_HEADERS;

    private final ZoneId GMT = ZoneId.of("GMT");

    public NettyHeaders(HttpHeaders nettyHeaders) {
        NETTY_HEADERS = nettyHeaders == null ? HttpHeaders.EMPTY_HEADERS : nettyHeaders;
    }

    @Override
    public Set<String> getNames() {
        return NETTY_HEADERS.names();
    }

    @Override
    public String getValue(CharSequence headerName) {
        return NETTY_HEADERS.get(headerName);
    }

    @Override
    public List<String> getValues(CharSequence headerName) {
        return NETTY_HEADERS.getAll(headerName);
    }

    @Override
    public void setValue(CharSequence headerName, Object object) {
        object = toDateIfNecessary(object);
        NETTY_HEADERS.set(headerName, object);
    }

    protected Object toDateIfNecessary(Object object) {
        if (object instanceof LocalDate) {
            LocalDate ld = (LocalDate)object;
            object = ld.atStartOfDay(GMT);
        }
        if (object instanceof LocalTime) {
            LocalTime lt = (LocalTime)object;
            object = lt.atDate(LocalDate.now(GMT));
        }
        if (object instanceof LocalDateTime) {
            LocalDateTime ldt = (LocalDateTime)object;
            object = ZonedDateTime.of(ldt, GMT);
        }
        if (object instanceof ZonedDateTime) {
            ZonedDateTime zdt = (ZonedDateTime)object;
            ZonedDateTime gmt = zdt.withZoneSameInstant(GMT);
            object = Date.from(gmt.toInstant());
        }

        return object;
    }

    @Override
    public void setValues(CharSequence headerName, Object... values) {
        Object[] curated = new Object[values.length];
        for(int i = 0; i < values.length; i++) {
            curated[i] = toDateIfNecessary(values[i]);
        }
        NETTY_HEADERS.set(headerName, curated);
    }

    @Override
    public ZonedDateTime getDate(CharSequence headerName) throws ParseException {
        OnlyHeadersMessage msg = new OnlyHeadersMessage(NETTY_HEADERS);
        Date date = HttpHeaders.getDateHeader(msg, headerName);
        LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("GMT"));
        return ldt.atZone(ZoneId.of("UTC"));
    }

    @Override
    public ZonedDateTime getDate(CharSequence headerName, ZonedDateTime defaultValue) {
        try {
            return getDate(headerName);
        } catch (ParseException e) {
            return defaultValue;
        }
    }

    private String getRequiredNumericValue(CharSequence headerName) throws NumberFormatException {
        String value = getValue(headerName);
        if (value == null) {
            throw new NumberFormatException("Header '" + headerName + "' does not exist.");
        }
        return value;
    }

    @Override
    public Integer getInteger(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String msg = "Unable to parse header value [" + value + "] to an Integer.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public Integer getInteger(CharSequence headerName, Integer defaultValue) {
        try {
            return getInteger(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Long getLong(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            String msg = "Unable to parse header value [" + value + "] to a Long.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public Long getLong(CharSequence headerName, Long defaultValue) {
        try {
            return getLong(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Float getFloat(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            String msg = "Unable to parse header value [" + value + "] to a Float.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public Float getFloat(CharSequence headerName, Float defaultValue) {
        try {
            return getFloat(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Double getDouble(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            String msg = "Unable to parse header value [" + value + "] to a Double.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public Double getDouble(CharSequence headerName, Double defaultValue) {
        try {
            return getDouble(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public BigInteger getBigInteger(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return new BigInteger(value);
        } catch (Exception e) {
            String msg = "Unable to parse header value [" + value + "] to a BigInteger.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public BigInteger getBigInteger(CharSequence headerName, BigInteger defaultValue) {
        try {
            return getBigInteger(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public BigDecimal getBigDecimal(CharSequence headerName) throws NumberFormatException {
        String value = getRequiredNumericValue(headerName);
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            String msg = "Unable to parse header value [" + value + "] to a BigDecimal.";
            throw new NumberFormatException(msg);
        }
    }

    @Override
    public BigDecimal getBigDecimal(CharSequence headerName, BigDecimal defaultValue) {
        try {
            return getBigDecimal(headerName);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static class OnlyHeadersMessage implements HttpMessage {
        private final HttpHeaders HEADERS;

        private OnlyHeadersMessage(HttpHeaders HEADERS) {
            this.HEADERS = HEADERS;
        }
        @Override
        public HttpVersion getProtocolVersion() {
            throw new UnsupportedOperationException("Not supported for date parsing.");
        }

        @Override
        public HttpMessage setProtocolVersion(HttpVersion version) {
            throw new UnsupportedOperationException("Not supported for date parsing.");
        }

        @Override
        public HttpHeaders headers() {
            return HEADERS;
        }

        @Override
        public DecoderResult getDecoderResult() {
            throw new UnsupportedOperationException("Not supported for date parsing.");
        }

        @Override
        public void setDecoderResult(DecoderResult result) {
            throw new UnsupportedOperationException("Not supported for date parsing.");
        }

    }
}
