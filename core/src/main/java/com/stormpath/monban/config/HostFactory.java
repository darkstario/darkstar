package com.stormpath.monban.config;

public interface HostFactory {

    /**
     * Returns a new Host instance representing a name and port.
     *
     * @param s host string, for example {@code foo.com:443}.
     * @return a new Host instance representing a name and port.
     */
    Host getHost(String s);

}
