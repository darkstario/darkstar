package io.darkstar.log;

import org.slf4j.Logger;

public interface LogFactory {

    Logger getLogger(String name);
}
