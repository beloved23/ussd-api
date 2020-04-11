package com.ft.domain;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;

import io.swagger.annotations.ApiModel;

/**
 * Notification via SMS, WEB, WAP
 */
@ApiModel(description = "Notification via SMS, WEB, WAP")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @NotNull
    @Size(max = 20)
    private String channel;

    @NotNull
    @Size(max = 40)
    private String msisdn;

    @NotNull
    private String productId;

    private Integer state;

    private String requestPayload;

    private ZonedDateTime requestAt;

    private String responsePayload;

    private ZonedDateTime responseAt;

    private String syncPayload;

    private ZonedDateTime syncAt;

    private String notificationPayload;

    private Set<String> tags = new HashSet<String>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public Notification channel(String channel) {
        this.channel = channel;
        return this;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public Notification msisdn(String msisdn) {
        this.msisdn = msisdn;
        return this;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getProductId() {
        return productId;
    }

    public Notification productId(String productId) {
        this.productId = productId;
        return this;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getState() {
        return state;
    }

    public Notification state(Integer state) {
        this.state = state;
        return this;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public Notification requestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
        return this;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public Notification responsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
        return this;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getNotificationPayload() {
        return notificationPayload;
    }

    public Notification notificationPayload(String notificationPayload) {
        this.notificationPayload = notificationPayload;
        return this;
    }

    public void setNotificationPayload(String notificationPayload) {
        this.notificationPayload = notificationPayload;
    }

    public Notification requestAt(ZonedDateTime requestAt) {
        this.requestAt = requestAt;
        return this;
    }

    public void setRequestAt(ZonedDateTime requestAt) {
        this.requestAt = requestAt;
    }

    public Notification responseAt(ZonedDateTime responseAt) {
        this.responseAt = responseAt;
        return this;
    }

    public void setResponseAt(ZonedDateTime responseAt) {
        this.responseAt = responseAt;
    }

    public ZonedDateTime getNotificationAt() {
        return syncAt;
    }

    public Notification syncAt(ZonedDateTime syncAt) {
        this.syncAt = syncAt;
        return this;
    }

    public void setNotificationAt(ZonedDateTime syncAt) {
        this.syncAt = syncAt;
    }
    public Set<String> getTags() {
        return tags;
    }

    public Notification tags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public Notification addTag(String tag) {
        this.tags.add(tag);
        return this;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        return id != null && id.equals(((Notification) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Notification{" +
            "id=" + getId() +
            ", channel='" + getChannel() + "'" +
            ", msisdn='" + getMsisdn() + "'" +
            ", productId='" + getProductId() + "'" +
            ", state=" + getState() +
            ", requestAt='" + getRequestAt() + "'" +
            ", requestPayload='" + getRequestPayload() + "'" +
            ", responseAt='" + getResponseAt() + "'" +
            ", responsePayload='" + getResponsePayload() + "'" +
            ", notificationPayload='" + getNotificationPayload() + "'" +
            ", tags='" + getTags() + "'" +
            ", meta='" + getMeta() + "'" +
            "}";
    }

    public ZonedDateTime getRequestAt() {
        return requestAt;
    }

    public Notification requestPayload(ZonedDateTime requestAt) {
        this.requestAt = requestAt;
        return this;
    }

    public void setRequestPayload(ZonedDateTime requestAt) {
        this.requestAt = requestAt;
    }

    public ZonedDateTime getResponseAt() {
        return responseAt;
    }

    public Notification responsePayload(ZonedDateTime responseAt) {
        this.responseAt = responseAt;
        return this;
    }

    public void setResponsePayload(ZonedDateTime responseAt) {
        this.responseAt = responseAt;
    }

    private Map<String, Object> meta = new HashMap<String, Object>();

    public Map<String, Object> getMeta() {
        return meta;
    }

    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }

    public Notification meta(Map<String, Object> meta) {
        this.meta = meta;
        return this;
    }

    public Notification addMeta(String key, Object value) {
        this.meta.put(key, value);
        return this;
    }
}
