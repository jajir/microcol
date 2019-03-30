package org.microcol.model.campaign;

import java.util.ArrayList;
import java.util.List;

import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;

/**
 * Event show simple message box with given key. Keys will later replaced with
 * text from localized resource bundle.
 */
public class EventShowMessages {

    private List<MessageKeyResource> keys = new ArrayList<>();

    @SuppressWarnings("unchecked")
    <T extends Enum<T> & MessageKeyResource> EventShowMessages(final T... messageKeys) {
        Preconditions.checkNotNull(messageKeys);
        for (final MessageKeyResource messageKey : messageKeys) {
            keys.add(messageKey);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Enum<T> & MessageKeyResource> List<T> getMessages() {
        return (List) keys;
    }

}
