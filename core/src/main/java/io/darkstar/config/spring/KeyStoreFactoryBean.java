package io.darkstar.config.spring;

import org.springframework.beans.factory.config.AbstractFactoryBean;

import java.security.KeyStore;

public class KeyStoreFactoryBean extends AbstractFactoryBean {

    @Override
    public Class<?> getObjectType() {
        return KeyStore.class;
    }

    @Override
    protected Object createInstance() throws Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
