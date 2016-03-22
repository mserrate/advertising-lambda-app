package net.serrate.advertising.shared;

/**
 * Created by mserrate on 06/03/16.
 */
public class ImpressionEvent {
    private String cookie;
    private String campaign;
    private String product;
    private Boolean click_thru;
    private Long timestamp;

    public ImpressionEvent() {}

    public ImpressionEvent(String cookie, String campaign, String product, Boolean click_thru, Long timestamp) {
        this.setCookie(cookie);
        this.setCampaign(campaign);
        this.setProduct(product);
        this.setClick_Thru(click_thru);
        this.setTimestamp(timestamp);
    }

    public String getCookie() {
        return cookie;
    }

    public String getCampaign() {
        return campaign;
    }

    public String getProduct() {
        return product;
    }

    public Boolean getClick_Thru() {
        return click_thru;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public void setCampaign(String campaign) {
        this.campaign = campaign;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public void setClick_Thru(Boolean click_thru) {
        this.click_thru = click_thru;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
