package brave.axon;

import brave.Span;
import brave.Tracing;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

public class SimpleAggregate {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleAggregate.class);

    @AggregateIdentifier
    private String id;

    private String traceId;
    private String spanId;

    public SimpleAggregate() {
    }

    @CommandHandler
    public SimpleAggregate(CreateCommand cmd) {
        LOGGER.info("Creating new aggregate {}.", cmd.getId());
        final Span span = Tracing.current().tracer().currentSpan();
        apply(new CreatedEvent(cmd.getId(), span.context().traceIdString(), span.context().spanIdString()));
    }

    @EventHandler
    public void onCreated(CreatedEvent event) {
        this.id = event.getId();
        this.traceId = event.getTraceId();
        this.spanId = event.getSpanId();
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public String getId() {
        return id;
    }
}
