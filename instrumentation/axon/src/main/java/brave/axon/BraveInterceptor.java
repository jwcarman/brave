package brave.axon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContextOrSamplingFlags;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.MetaData;
import org.axonframework.messaging.unitofwork.UnitOfWork;

public class BraveInterceptor<M extends Message<?>> implements MessageDispatchInterceptor<M>, MessageHandlerInterceptor<M> {
    private static final Propagation.Setter<Map<String,String>,String> METADATA_SETTER = Map::put;
    private static final Propagation.Getter<MetaData, String> METADATA_GETTER = (carrier, key) -> Optional.ofNullable(carrier.get(key))
            .map(Object::toString)
            .orElse(null);


    private final Tracing tracing;

    public BraveInterceptor(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override
    @SuppressWarnings("unchecked")
    public BiFunction<Integer, M, M> handle(List<? extends M> messages) {
        return (index,message) -> {
            final Span span = tracing.tracer().nextSpan();
            final Map<String,String> metadata = new HashMap<>();
            tracing.propagation().injector(METADATA_SETTER).inject(span.context(), metadata);
            return (M)message.andMetaData(metadata);
        };
    }

    @Override
    public Object handle(UnitOfWork<? extends M> unitOfWork, InterceptorChain interceptorChain) throws Exception {
        final TraceContextOrSamplingFlags extracted = tracing.propagation()
                .extractor(METADATA_GETTER)
                .extract(unitOfWork.getMessage().getMetaData());
        final Span span = tracing.tracer().nextSpan(extracted);
        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
            return interceptorChain.proceed();
        } finally {
            span.finish();
        }
    }
}
