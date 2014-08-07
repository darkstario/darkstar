package io.darkstar.config.http.connector;

import io.netty.channel.nio.NioEventLoopGroup;
import org.springframework.beans.factory.config.AbstractFactoryBean;

public class NioEventLoopGroupFactoryBean extends AbstractFactoryBean<NioEventLoopGroup> {

    private int numThreads = 0; //zero means let Netty choose based on num processors

    @SuppressWarnings("UnusedDeclaration") //can be called via reflection and bean property values
    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    @Override
    public Class<?> getObjectType() {
        return NioEventLoopGroup.class;
    }

    @Override
    protected NioEventLoopGroup createInstance() throws Exception {
        return new NioEventLoopGroup(numThreads);
    }

    @Override
    protected void destroyInstance(NioEventLoopGroup instance) throws Exception {
        instance.shutdownGracefully().awaitUninterruptibly();
    }
}
