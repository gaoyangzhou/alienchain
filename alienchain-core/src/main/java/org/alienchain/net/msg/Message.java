/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg;

import org.alienchain.util.Bytes;

/**
 * Abstract message class for all messages on the Alienchain network
 * 
 */
public abstract class Message {
    /**
     * Message code.
     */
    protected final MessageCode code;

    /**
     * Response message class.
     */
    protected final Class<?> responseMessageClass;

    /**
     * Message body.
     */
    protected byte[] body;

    /**
     * Create a message instance.
     * 
     * @param code
     * @param responseMessageClass
     */
    public Message(MessageCode code, Class<?> responseMessageClass) {
        this.code = code;
        this.responseMessageClass = responseMessageClass;
        this.body = Bytes.EMPTY_BYTES;
    }

    /**
     * Get the body of this message
     * 
     * @return
     */
    public byte[] getBody() {
        return body;
    }

    /**
     * Get the message code
     * 
     * @return
     */
    public MessageCode getCode() {
        return code;
    }

    /**
     * Get the response message class of this message.
     * 
     * @return the response message, or null if this message requires no response.
     */
    public Class<?> getResponseMessageClass() {
        return responseMessageClass;
    }

    /**
     * Return the message name.
     */
    public String toString() {
        return getClass().getName();
    }
}
