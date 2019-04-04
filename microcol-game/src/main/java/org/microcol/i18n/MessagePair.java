package org.microcol.i18n;

public class MessagePair {

    private String key;

    private String message;

    public MessagePair() {
    }

    @Override
    public String toString() {
        return Class.class.getSimpleName() + "{key=" + key + ", message=" + message + "}";
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
