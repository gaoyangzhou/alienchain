/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.util;

import java.util.Iterator;

public interface ClosableIterator<T> extends Iterator<T> {

    /**
     * Closes the underlying resources for this iterator.
     */
    void close();
}
