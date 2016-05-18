package wigzo.sdk.model;

/**
 * Created by wigzo on 18/5/16.
 */
public class GCMRead {

    private long id;
    private String timestamp;

    public GCMRead(){};

    public GCMRead(long id, String timestamp) {
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
