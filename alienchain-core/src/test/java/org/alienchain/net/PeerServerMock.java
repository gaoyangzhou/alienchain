/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net;

import java.util.concurrent.atomic.AtomicBoolean;

import org.alienchain.Kernel;
import org.alienchain.KernelMock;
import org.alienchain.config.Config;
import org.alienchain.consensus.AlienchainBft;
import org.alienchain.consensus.AlienchainSync;
import org.alienchain.core.BlockchainImpl;
import org.alienchain.core.PendingManager;
import org.alienchain.db.Database;
import org.alienchain.db.DatabaseFactory;
import org.alienchain.db.DatabaseName;
import org.alienchain.db.LeveldbDatabase.LeveldbFactory;

public class PeerServerMock {

    private KernelMock kernel;
    private PeerServer server;

    private DatabaseFactory dbFactory;
    private PeerClient client;

    private AtomicBoolean isRunning = new AtomicBoolean(false);

    public PeerServerMock(KernelMock kernel) {
        this.kernel = kernel;
    }

    public synchronized void start() {
        if (isRunning.compareAndSet(false, true)) {
            Config config = kernel.getConfig();

            dbFactory = new LeveldbFactory(config.databaseDir());
            client = new PeerClient(config.p2pListenIp(), config.p2pListenPort(), kernel.getCoinbase());

            kernel.setBlockchain(new BlockchainImpl(config, dbFactory));
            kernel.setClient(client);
            kernel.setChannelManager(new ChannelManager(kernel));
            kernel.setPendingManager(new PendingManager(kernel));
            kernel.setNodeManager(new NodeManager(kernel));

            kernel.setBftManager(new AlienchainBft(kernel));
            kernel.setSyncManager(new AlienchainSync(kernel));

            // start peer server
            server = new PeerServer(kernel);
            server.start(config.p2pListenIp(), config.p2pListenPort());
        }
    }

    public synchronized void stop() {
        if (isRunning.compareAndSet(true, false)) {
            server.stop();

            client.close();

            for (DatabaseName name : DatabaseName.values()) {
                Database db = dbFactory.getDB(name);
                db.close();
            }
        }
    }

    public Kernel getKernel() {
        return kernel;
    }

    public PeerServer getServer() {
        return server;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
