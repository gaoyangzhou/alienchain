/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.p2p;

import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;
import org.alienchain.net.msg.ReasonCode;
import org.alienchain.util.SimpleDecoder;
import org.alienchain.util.SimpleEncoder;

public class DisconnectMessage extends Message {

    private final ReasonCode reason;

    /**
     * Create a DISCONNECT message.
     * 
     * @param reason
     */
    public DisconnectMessage(ReasonCode reason) {
        super(MessageCode.DISCONNECT, null);

        this.reason = reason;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeByte(reason.toByte());
        this.body = enc.toBytes();
    }

    /**
     * Parse a DISCONNECT message from byte array.
     * 
     * @param body
     */
    public DisconnectMessage(byte[] body) {
        super(MessageCode.DISCONNECT, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.reason = ReasonCode.of(dec.readByte());

        this.body = body;
    }

    public ReasonCode getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "DisconnectMessage [reason=" + reason + "]";
    }
}