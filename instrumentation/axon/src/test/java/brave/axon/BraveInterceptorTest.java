package brave.axon;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BraveInterceptorTest {

    private Tracing tracing;
    private FixtureConfiguration<SimpleAggregate> fixture;

    @Before
    public void initializeFixture() {
        tracing = Tracing.newBuilder()
                .localServiceName(getClass().getSimpleName())
                .build();
        final BraveInterceptor<CommandMessage<?>> interceptor = new BraveInterceptor<>(tracing);
        fixture = new AggregateTestFixture<>(SimpleAggregate.class)
                .registerCommandDispatchInterceptor(interceptor)
                .registerCommandHandlerInterceptor(interceptor);
    }

    @Test
    public void propagationWhenTracing() {
        final Span span = tracing.tracer().newTrace();
        try (Tracer.SpanInScope sis = tracing.tracer().withSpanInScope(span)) {
            fixture.given()
                    .when(new CreateCommand("foo"))
                    .expectSuccessfulHandlerExecution()
                    .expectState(agg -> {
                        assertThat(agg.getId()).isEqualTo("foo");
                        assertThat(agg.getTraceId()).isEqualTo(span.context().traceIdString());
                        assertThat(agg.getSpanId()).isNotEqualTo(span.context().spanIdString());
                    });
        }

    }

    @Test
    public void propagationWhenNotTracing() {
        fixture.given()
                .when(new CreateCommand("foo"))
                .expectSuccessfulHandlerExecution()
                .expectState(agg -> {
                    assertThat(agg.getId()).isEqualTo("foo");
                    assertThat(agg.getTraceId()).isNotNull();
                    assertThat(agg.getSpanId()).isNotNull();
                });
    }
}