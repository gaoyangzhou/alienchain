/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;
import org.alienchain.util.SimpleDecoder;
import org.alienchain.util.SimpleEncoder;

public class GetBlockMessage extends Message {

    private final long number;

    public GetBlockMessage(long number) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);
        this.number = number;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(number);
        this.body = enc.toBytes();
    }

    public GetBlockMessage(byte[] body) {
        super(MessageCode.GET_BLOCK, BlockMessage.class);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.number = dec.readLong();

        this.body = body;
    }

    public long getNumber() {
        return number;
    }

    @Override
    public String toString() {
        return "GetBlockMessage [number=" + number + "]";
    }
}
