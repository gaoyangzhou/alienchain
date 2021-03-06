/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.api.util;

import static org.alienchain.core.Amount.Unit.NANO_ALX;

import org.alienchain.Kernel;
import org.alienchain.Network;
import org.alienchain.core.Amount;
import org.alienchain.core.Transaction;
import org.alienchain.core.TransactionType;
import org.alienchain.crypto.CryptoException;
import org.alienchain.crypto.Hex;
import org.alienchain.crypto.Key;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;

/**
 * This is a builder class for building transactions required by Alienchain API with
 * provided inputs. The builder expects raw inputs from a HTTP request.
 */
public class TransactionBuilder {

    private final Kernel kernel;

    /**
     * Network id
     */
    private Network network;

    /**
     * Transaction type
     */
    private TransactionType type;

    /**
     * Transaction sender account
     */
    private Key account;

    /**
     * Transaction recipient address
     */
    private byte[] to;

    /**
     * Transaction value
     */
    private Amount value;

    /**
     * Transaction fee
     */
    private Amount fee;

    /**
     * Transaction nonce.
     */
    private Long nonce;

    /**
     * Transaction timestamp.
     */
    private Long timestamp;

    /**
     * Transaction data
     */
    private byte[] data;

    private long gasPrice = 0;
    private long gas = 0;

    public TransactionBuilder(Kernel kernel) {
        this.kernel = kernel;
    }

    public TransactionBuilder withType(TransactionType type) {
        if (type == null) {
            throw new IllegalArgumentException("Parameter `type` is required");
        }

        this.type = type;
        return this;
    }

    public TransactionBuilder withType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Parameter `type` is required");
        }

        this.type = TransactionType.valueOf(type);
        return this;
    }

    public TransactionBuilder withNetwork(String network) {
        if (network == null) {
            throw new IllegalArgumentException("Parameter `network` is required");
        }

        this.network = Network.valueOf(network);
        return this;
    }

    public TransactionBuilder withFrom(String from) {
        if (from == null) {
            throw new IllegalArgumentException("Parameter `from` is required");
        }

        try {
            account = kernel.getWallet().getAccount(Hex.decode0x(from));
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `from` is not a valid hexadecimal string");
        }

        if (account == null) {
            throw new IllegalArgumentException(
                    String.format("The provided address %s doesn't belong to the wallet", from));
        }

        return this;
    }

    public TransactionBuilder withTo(String to) {
        if (type == TransactionType.DELEGATE) {
            if (to != null && !to.isEmpty()) {
                throw new IllegalArgumentException("Parameter `to` is not needed for DELEGATE transaction");
            }
            return this; // ignore the provided parameter
        }
        if (type == TransactionType.CREATE) {
            if (to != null && !to.isEmpty()) {
                throw new IllegalArgumentException("Parameter `to` is not needed for CREATE transaction");
            }
            return this; // ignore the provided parameter
        }

        if (to == null) {
            throw new IllegalArgumentException("Parameter `to` is required");
        }

        try {
            this.to = Hex.decode0x(to);
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `to` is not a valid hexadecimal string");
        }

        if (this.to.length != Key.ADDRESS_LEN) {
            throw new IllegalArgumentException("Parameter `to` is not a valid address");
        }

        return this;
    }

    public TransactionBuilder withValue(String value) {
        if (type == TransactionType.DELEGATE) {
            if (value != null && !value.isEmpty()) {
                throw new IllegalArgumentException("Parameter `value` is not needed for DELEGATE transaction");
            }
            return this; // ignore the provided parameter
        }

        if (type == TransactionType.CREATE) {
            if (value != null && !value.isEmpty()) {
                throw new IllegalArgumentException("Parameter `value` is not needed for CREATE transaction");
            }
            return this; // ignore the provided parameter
        }

        if (value == null) {
            throw new IllegalArgumentException("Parameter `value` is required");
        }

        try {
            this.value = NANO_ALX.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter `value` is not a valid number");
        }

        return this;
    }

    public TransactionBuilder withFee(String fee, boolean optional) {
        if (optional && (fee == null || fee.isEmpty())) {
            this.fee = kernel.getConfig().minTransactionFee();
            return this;
        }

        try {
            this.fee = NANO_ALX.of(Long.parseLong(fee));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter `fee` is not a valid number");
        }

        return this;
    }

    public TransactionBuilder withNonce(String nonce) {
        try {
            this.nonce = Long.parseLong(nonce);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter 'nonce' is not a valid number");
        }
        return this;
    }

    public TransactionBuilder withTimestamp(String timestamp) {
        try {
            this.timestamp = timestamp != null && !timestamp.isEmpty() ? Long.parseLong(timestamp) : null;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter 'timestamp' is not a valid number");
        }
        return this;
    }

    public TransactionBuilder withData(String data) {
        try {
            this.data = (data == null) ? Bytes.EMPTY_BYTES : Hex.decode0x(data);
        } catch (CryptoException e) {
            throw new IllegalArgumentException("Parameter `data` is not a valid hexadecimal string");
        }

        return this;
    }

    public TransactionBuilder withGasPrice(String gasPrice) {
        if (gasPrice == null) {
            throw new IllegalArgumentException("Parameter `gasPrice` is required");
        }

        try {
            this.gasPrice = Long.parseLong(gasPrice);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter `gasPrice` is not a valid number");
        }

        return this;
    }

    public TransactionBuilder withGas(String gasLimit) {
        if (gasLimit == null) {
            throw new IllegalArgumentException("Parameter `gas` is required");
        }

        try {
            this.gas = Long.parseLong(gasLimit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parameter `gas` is not a valid number");
        }

        return this;
    }

    public Transaction buildUnsigned() {
        // DELEGATE transaction has fixed receiver and value
        if (type == TransactionType.DELEGATE) {
            to = Bytes.EMPTY_ADDRESS;
            value = kernel.getConfig().minDelegateBurnAmount();
        }
        if (type == TransactionType.CREATE) {
            to = Bytes.EMPTY_ADDRESS;
            value = Amount.ZERO;
        }

        return new Transaction(
                network != null ? network : kernel.getConfig().network(),
                type,
                to,
                value,
                fee,
                nonce != null ? nonce : kernel.getPendingManager().getNonce(account.toAddress()),
                timestamp != null ? timestamp : TimeUtil.currentTimeMillis(),
                data, gas, gasPrice);
    }

    public Transaction buildSigned() {
        if (account == null) {
            throw new IllegalArgumentException("TransactionBuilder#withFrom must be called");
        }

        return buildUnsigned().sign(account);
    }
}