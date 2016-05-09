package wigzo.sdk.model;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import wigzo.sdk.WigzoSDK;

/**
 * <p>An instance of this class represents an event(or activity).</p>
 * Event information includes -
 * <ol>
 *     <li>{@link EventInfo#eventName} - Name of underlying event.</li>
 *     <li>{@link EventInfo#eventValue} - Value of underlying event. e.g. rating info in case of <i>Rate</i> event</li>
 *     <li>{@link EventInfo#metadata}. For more info see - {@link Metadata}</li>
 * </ol>
 * @author Minaz Ali
 */

public class EventInfo {

    private String eventName;
    private String eventValue;
    private Metadata metadata;

    private String timestamp;

    /**
     * Constructor to create {@link EventInfo} object whenever an event( or activity ) takes place
     * @param eventName : Name of Event ( or activity), must not be null
     * @param eventValue : Value of Event ( or Activity), must not be null
     */
    public EventInfo(String eventName, String eventValue) {
        this.eventName = eventName;
        this.eventValue = eventValue;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }


    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * <code>Metadata</code> class is used to provide additional information about activity.
     * <b>Example</b> : <p>If it is a product page, metadata can be used to provide product details like
     * <code> {@link Metadata#id}, {@link Metadata#title}, {@link Metadata#description}, {@link Metadata#url}, {@link Metadata#price} </code>
     */
    public static class Metadata{
        private String id;
        private String title;
        private String description;
        private String url;
        private String tags;
        private BigDecimal price;

        public Metadata (){}

        /**
         * Constructor to obtain <code>Metadata</code> object with id, title, description,url.
         * This object used to provide additional information about activity.
         * <b>Example</b> : If it is a product page, metadata can be used to provide product details like id, title, description, url, price
         * @param id id of event ( or activity)
         * @param title title of event ( or activity)
         * @param description description of event ( or activity)
         * @param url url of application's  market place or web url
         */
        public Metadata(String id, String title, String description, String url) {
            this.id = id;
            this.title = title;
            this.description = description;
            if(StringUtils.isEmpty(url)){
                String packageName = WigzoSDK.getInstance().getContext().getPackageName();
                this.url ="http://play.google.com/store/apps/details?id="+packageName;
            }else {
                this.url = url;
            }
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

        public void setUrl(String url) {
            this.url = url;
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
