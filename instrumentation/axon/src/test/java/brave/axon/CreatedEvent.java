package brave.axon;

public class CreatedEvent {
    private final String id;
    private final String traceId;
    private final String spanId;

    public CreatedEvent(String id, String traceId, String spanId) {
        this.id = id;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public String getId() {
        return id;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CreatedEvent that = (CreatedEvent) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (traceId != null ? !traceId.equals(that.traceId) : that.traceId != null) return false;
        return spanId != null ? spanId.equals(that.spanId) : that.spanId == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (traceId != null ? traceId.hashCode() : 0);
        result = 31 * result + (spanId != null ? spanId.hashCode() : 0);
        return result;
    }
}
