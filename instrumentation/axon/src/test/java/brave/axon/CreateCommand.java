package brave.axon;

public class CreateCommand {
    private final String id;

    public CreateCommand(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
