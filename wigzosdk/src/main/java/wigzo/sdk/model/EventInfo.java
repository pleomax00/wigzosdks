package wigzo.sdk.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

/**
 * Created by wigzo on 2/5/16.
 */
public class EventInfo {

    private String eventName;
    private String eventValue;

    private Metadata metadata;

    private String timestamp;

    public EventInfo(String eventName, String eventValue) {
        this.eventName = eventName;
        this.eventValue = eventValue;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }


    public static class Metadata{
        private String title;
        private String description;
        private String tags;
        private BigDecimal price;

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

        public void setPrice(BigDecimal price) {
            this.price = price;
        }


    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof EventInfo){
            EventInfo eventInfo = (EventInfo)o;
            return this.timestamp.equalsIgnoreCase(eventInfo.timestamp);
        }
        return false;
    }


}
