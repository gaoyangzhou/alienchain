/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net;

import org.alienchain.net.msg.ReasonCode;

/**
 * This enum represents the available capabilities in current version of Alienchain
 * wallet. One peer should be disconnected by
 * ${@link ReasonCode#BAD_NETWORK_VERSION} if the peer doesn't support the
 * required set of capabilities.
 */
public enum Capability {

    /**
     * A mandatory capability for all clients.
     */
    ALIENCHAIN,

    /**
     * This client supports the CORE protocol.
     */
    CORE,

    /**
     * This client supports the LIGHT protocol.
     */
    LIGHT,

    /**
     * The client supports FAST_SYNC protocol.
     */
    FAST_SYNC;

    public static Capability of(String name) {
        try {
            return valueOf(name);
        } catch (IllegalArgumentException | NullPointerException ex) {
            return null;
        }
    }

}
