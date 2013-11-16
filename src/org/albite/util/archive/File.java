
package org.albite.util.archive;

import java.io.IOException;

/**
 *
 * @author albus
 */
public interface File {
    public int fileSize() throws IOException;
    public String getURL();
}
