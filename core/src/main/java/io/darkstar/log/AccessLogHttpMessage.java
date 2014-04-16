package io.darkstar.log;

public abstract class AccessLogHttpMessage {

    protected static final String ISSUES_URL = "https://github.com/darkstario/darkstar/issues";

    protected static final String UNSUPPORTED_MSG = "Method implementation is currently unsupported for access log " +
            "entries.  If you would like it supported, please open an issue at " + ISSUES_URL + ".  Issues created " +
            "with Pull Requests will always have higher priority, so please consider issuing a Pull Request!";
}
