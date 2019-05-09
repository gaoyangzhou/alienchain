/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.vm.client;

import java.math.BigInteger;

import org.alienchain.core.TransactionType;
import org.ethereum.vm.client.Transaction;

/**
 * Facade for Transaction -> Transaction
 */
public class AlienchainTransaction implements Transaction {

    private final org.alienchain.core.Transaction transaction;

    public AlienchainTransaction(org.alienchain.core.Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public boolean isCreate() {
        return transaction.getType().equals(TransactionType.CREATE);
    }

    @Override
    public byte[] getFrom() {
        return transaction.getFrom();
    }

    @Override
    public byte[] getTo() {
        return transaction.getTo();
    }

    @Override
    public long getNonce() {
        return transaction.getNonce();
    }

    @Override
    public BigInteger getValue() {
        return transaction.getValue().getBigInteger();
    }

    @Override
    public byte[] getData() {
        return transaction.getData();
    }

    @Override
    public long getGas() {
        return transaction.getGas();
    }

    @Override
    public BigInteger getGasPrice() {
        return BigInteger.valueOf(transaction.getGasPrice());
    }
}
