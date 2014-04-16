package io.darkstar.config.json;

public class LogConfig {

    protected String path;
    protected String format;

    public LogConfig() {
        this.format = "%h %l %u [%t] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\"";
    }

    public LogConfig(LogConfig src) {
        this.path = src.path;
        this.format = src.format;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}


