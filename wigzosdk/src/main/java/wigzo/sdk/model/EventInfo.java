package wigzo.sdk.model;

import java.util.Date;
import java.util.Map;

/**
 * Created by wigzo on 2/5/16.
 */
public class EventInfo {

    private String eventName;
    private String eventValue;

    private Metadata metadata;

    private Date timestamp;

    public EventInfo(String eventName, String eventValue) {
        this.eventName = eventName;
        this.eventValue = eventValue;
        this.timestamp = new Date();
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }


    public static class Metadata{
        private String title;
        private String description;
        private String tags;
        private double price;

        public Metadata (){}

        public Metadata(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setTags(String tags) {
            this.tags = tags;
        }

        public void setPrice(double price) {
            this.price = price;
        }


    }


}
