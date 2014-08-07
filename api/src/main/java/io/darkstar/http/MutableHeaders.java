package io.darkstar.http;

public interface MutableHeaders extends Headers {

    void setValue(CharSequence headerName, Object object);

    void setValues(CharSequence headerName, Object... values);

}
