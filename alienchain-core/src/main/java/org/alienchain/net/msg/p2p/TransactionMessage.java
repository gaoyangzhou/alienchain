/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.p2p;

import org.alienchain.core.Transaction;
import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;

public class TransactionMessage extends Message {

    private final Transaction transaction;

    /**
     * Create a TRANSACTION message.
     * 
     */
    public TransactionMessage(Transaction transaction) {
        super(MessageCode.TRANSACTION, null);

        this.transaction = transaction;

        // FIXME: consider wrapping by simple codec
        this.body = transaction.toBytes();
    }

    /**
     * Parse a TRANSACTION message from byte array.
     * 
     * @param body
     */
    public TransactionMessage(byte[] body) {
        super(MessageCode.TRANSACTION, null);

        this.transaction = Transaction.fromBytes(body);

        this.body = body;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {
        return "TransactionMessage [tx=" + transaction + "]";
    }
}
