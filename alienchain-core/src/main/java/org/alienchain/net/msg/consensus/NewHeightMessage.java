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

public class NewHeightMessage extends Message {

    private final long height;

    public NewHeightMessage(long height) {
        super(MessageCode.BFT_NEW_HEIGHT, null);
        this.height = height;

        SimpleEncoder enc = new SimpleEncoder();
        enc.writeLong(height);
        this.body = enc.toBytes();
    }

    public NewHeightMessage(byte[] body) {
        super(MessageCode.BFT_NEW_HEIGHT, null);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.height = dec.readLong();

        this.body = body;
    }

    public long getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "BFTNewHeightMessage [height=" + height + "]";
    }
}
