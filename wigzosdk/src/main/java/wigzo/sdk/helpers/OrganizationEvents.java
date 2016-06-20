package wigzo.sdk.helpers;


/**
 * Utility for standard events. It's advisable to use standard events so that Wigzo's recommendation engine can function properly.
 * Although you are free to use custom event names.
 * Standard events can be used as follows -
 *
 *     EventInfo eventInfo = new EventInfo( OrganizationEvents.Events.SEARCH.key,"iphone" );
 *
 */
public class OrganizationEvents {

    /**
     * Enum containing all events for all types of organizations.
     */
    public enum Events {
        OTHER( "other", "Other"),
        ITEM( "item", "Item"),JOIN( "join", "Join"),
        RATE("rate", "Rate"), VIEW("view", "View"), BUY("buy", "Buy"), SEARCH("search", "Search"),
        ADDTOCART("addtocart", "Add To Cart"), BOOK( "book", "Book"),CHECKOUT( "checkout", "Checkout"),
        REGISTERED("registered", "Registered"), LIKE( "like", "Like"), SHARE( "share", "Share"), ADDTOWISHLIST( "addtowishlist", "Add To Wishlist"),
        LISTEN( "listen", "Listen"), ADDTOPLAYLIST( "addtoplaylist", "Add To Playlist"), WATCH( "watch", "Watch"), WATCHLATER( "watchlater", "Watch Later" ),
        LOGGEDIN("loggedin","LoggedIn"),LOGGEDOUT("loggedout","LoggedOut");

        public String key;
        public String label;

        Events(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

}
