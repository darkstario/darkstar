package com.stormpath.monban.config.json;

import java.util.List;

public class StormpathConfig {

    private String apiKeyFile;
    private String applicationHref;
    private List<String> authenticate;

    public StormpathConfig(){}

    public String getApiKeyFile() {
        return apiKeyFile;
    }

    public String getApplicationHref() {
        return applicationHref;
    }

    public List<String> getAuthenticate() {
        return authenticate;
    }
}
