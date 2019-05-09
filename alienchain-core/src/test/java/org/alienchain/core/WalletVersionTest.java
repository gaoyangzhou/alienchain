/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.alienchain.Network;
import org.alienchain.crypto.Aes;
import org.alienchain.crypto.Hash;
import org.alienchain.crypto.Key;
import org.alienchain.util.Bytes;
import org.alienchain.util.IOUtil;
import org.alienchain.util.SimpleEncoder;

/**
 * Tests ability to read old wallet versions
 */
public class WalletVersionTest {

  @Test
    public void testVersion1Wallet() throws IOException {
        File file = File.createTempFile("wallet", ".data");
        List<Key> accounts = Collections.singletonList(new Key());

        writeVersion1Wallet(accounts, file, "password!");

        // read it as current version
        Wallet wallet = new Wallet(file, Network.DEVNET);
        wallet.unlock("password!");
        List<Key> readAccounts = wallet.getAccounts();

        assertEquals(accounts, readAccounts);

        // verify that it has 'name' set to default
        Optional<String> name = wallet.getAddressAlias(accounts.get(0).getPublicKey());
        assertFalse(name.isPresent());
    }

    private void writeVersion1Wallet(List<Key> accounts, File file, String password) throws IOException {
        byte[] key = Hash.h256(Bytes.of(password));

        // write a version 1 wallet
        SimpleEncoder enc = new SimpleEncoder();
        enc.writeInt(1);
        enc.writeInt(accounts.size());

        for (Key a : accounts) {
            byte[] iv = Bytes.random(16);

            enc.writeBytes(iv, false);
            enc.writeBytes(a.getPublicKey(), false);
            enc.writeBytes(Aes.encrypt(a.getPrivateKey(), key, iv), false);
        }

        IOUtil.writeToFile(enc.toBytes(), file);
    }
}
