package org.microcol.i18n;

import java.util.List;

public class ResourceBundleModel {

    private List<MessagePair> messages;

    @Override
    public String toString() {
        if (messages == null) {
            return Class.class.getSimpleName() + "{messages=null}";
        } else {
            return Class.class.getSimpleName() + "{messages.size()=" + messages.size() + "}";
        }
    }

    public List<MessagePair> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagePair> pairs) {
        this.messages = pairs;
    }

}
