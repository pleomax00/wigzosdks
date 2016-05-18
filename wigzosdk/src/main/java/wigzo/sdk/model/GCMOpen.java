package wigzo.sdk.model;

/**
 * Created by wigzo on 18/5/16.
 */
public class GCMOpen {

    private long id;
    private String timestamp;

    public GCMOpen(){};

    public GCMOpen(long id, String timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
