package io.darkstar.log;

import io.darkstar.http.HttpFilter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Promise;
import reactor.core.composable.spec.Promises;
import reactor.tuple.Tuple;
import reactor.tuple.Tuple2;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AccessLogFilter extends HttpFilter {

    @Autowired
    private Environment environment;

    protected HttpResponse httpResponse;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

        if (msg instanceof HttpResponse) {
            httpResponse = (HttpResponse) msg;

            Deferred<Tuple2<HttpRequest, HttpResponse>, Promise<Tuple2<HttpRequest, HttpResponse>>> deferred =
                    Promises.defer(environment, Environment.RING_BUFFER);

            deferred.compose().map(tuple ->

                    Tuple.of(new AccessLogHttpServletRequest(tuple.getT1()),
                            new AccessLogHttpServletResponse(tuple.getT2()))

            ).consume(tuple -> {

                AccessLogHttpServletRequest request = tuple.getT1();
                AccessLogHttpServletResponse response = tuple.getT2();

            });

            deferred.accept(Tuple.of(currentRequest, httpResponse));
        }

        super.write(ctx, msg, promise);
    }
}
