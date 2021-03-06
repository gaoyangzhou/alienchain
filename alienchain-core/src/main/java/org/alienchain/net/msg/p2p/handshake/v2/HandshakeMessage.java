/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.p2p.handshake.v2;

import java.util.ArrayList;
import java.util.List;

import org.alienchain.Network;
import org.alienchain.config.Config;
import org.alienchain.crypto.Hex;
import org.alienchain.crypto.Key;
import org.alienchain.net.CapabilitySet;
import org.alienchain.net.Peer;
import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;
import org.alienchain.util.SimpleDecoder;
import org.alienchain.util.SimpleEncoder;
import org.alienchain.util.TimeUtil;

public abstract class HandshakeMessage extends Message {

    protected final Network network;
    protected final short networkVersion;

    protected final String peerId;
    protected final int port;

    protected final String clientId;
    protected final CapabilitySet capabilities;

    protected final long latestBlockNumber;

    protected final byte[] secret;
    protected final long timestamp;
    protected final Key.Signature signature;

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass,
            Network network, short networkVersion, String peerId, int port,
            String clientId, CapabilitySet capabilities, long latestBlockNumber,
            byte[] secret, Key coinbase) {
        super(code, responseMessageClass);

        this.network = network;
        this.networkVersion = networkVersion;
        this.peerId = peerId;
        this.port = port;
        this.clientId = clientId;
        this.capabilities = capabilities;
        this.latestBlockNumber = latestBlockNumber;
        this.secret = secret;
        this.timestamp = TimeUtil.currentTimeMillis();

        SimpleEncoder enc = encodeBasicInfo();
        this.signature = coinbase.sign(enc.toBytes());
        enc.writeBytes(signature.toBytes());

        this.body = enc.toBytes();
    }

    public HandshakeMessage(MessageCode code, Class<?> responseMessageClass, byte[] body) {
        super(code, responseMessageClass);

        SimpleDecoder dec = new SimpleDecoder(body);
        this.network = Network.of(dec.readByte());
        this.networkVersion = dec.readShort();
        this.peerId = dec.readString();
        this.port = dec.readInt();
        this.clientId = dec.readString();
        List<String> capabilities = new ArrayList<>();
        for (int i = 0, size = dec.readInt(); i < size; i++) {
            capabilities.add(dec.readString());
        }
        this.capabilities = CapabilitySet.of(capabilities.toArray(new String[0]));
        this.latestBlockNumber = dec.readLong();
        this.secret = dec.readBytes();
        this.timestamp = dec.readLong();
        this.signature = Key.Signature.fromBytes(dec.readBytes());

        this.body = body;
    }

    protected SimpleEncoder encodeBasicInfo() {
        SimpleEncoder enc = new SimpleEncoder();

        enc.writeByte(network.id());
        enc.writeShort(networkVersion);
        enc.writeString(peerId);
        enc.writeInt(port);
        enc.writeString(clientId);
        enc.writeInt(capabilities.size());
        for (String capability : capabilities.toArray()) {
            enc.writeString(capability);
        }
        enc.writeLong(latestBlockNumber);
        enc.writeBytes(secret);
        enc.writeLong(timestamp);

        return enc;
    }

    /**
     * Validates this HELLO message.
     *
     * <p>
     * NOTE: only data format and signature is checked here.
     * </p>
     *
     * @param config
     * @return true if success, otherwise false
     */
    public boolean validate(Config config) {
        if (network == config.network()
                && networkVersion == config.networkVersion()
                && peerId != null && peerId.length() == 40
                && port > 0 && port <= 65535
                && clientId != null && clientId.length() < 128
                && latestBlockNumber >= 0
                && secret != null && secret.length == InitMessage.SECRET_LENGTH
                && Math.abs(TimeUtil.currentTimeMillis() - timestamp) <= config.netHandshakeExpiry()
                && signature != null
                && peerId.equals(Hex.encode(signature.getAddress()))) {

            SimpleEncoder enc = encodeBasicInfo();
            return Key.verify(enc.toBytes(), signature);
        } else {
            return false;
        }
    }

    /**
     * Constructs a Peer object from the handshake info.
     *
     * @param ip
     * @return
     */
    public Peer getPeer(String ip) {
        return new Peer(network, networkVersion, peerId, ip, port, clientId, capabilities.toArray(),
                latestBlockNumber);
    }

    /**
     * Returns the secret.
     *
     * @return
     */
    public byte[] getSecret() {
        return secret;
    }
}
