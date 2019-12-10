package org.microcol.gui.util;

import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.microcol.gui.MicroColException;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

/**
 * Class for reading stream from class path. Class handle java module security.
 */
@Singleton
public class StreamReader {

    public InputStream openStream(final String fileName) {
        Preconditions.checkNotNull(fileName, "File name can't be null.");
        final Module module = this.getClass().getModule();
        final PrivilegedAction<InputStream> pa = () -> {
            try {
                return module.getResourceAsStream(fileName);
            } catch (IOException e) {
                throw new MicroColException(e.getMessage(), e);
            }
        };
        final InputStream stream = AccessController.doPrivileged(pa);
        if (stream == null) {
            throw new MicroColException(
                    String.format("Unabble to load file %s from class path in module %s", fileName,
                            module.getName()));
        }
        return stream;
    }

    public String toResourceName(final String bundleName, final String suffix) {
        if (bundleName.contains("://")) {
            return null;
        }
        StringBuilder sb = new StringBuilder(bundleName.length() + 1 + suffix.length());
        sb.append(bundleName.replace('.', '/')).append('.').append(suffix);
        return sb.toString();
    }

}
