/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.parsers.ParserConfigurationException;

import org.alienchain.api.AlienchainApiService;
import org.alienchain.config.Config;
import org.alienchain.config.Constants;
import org.alienchain.consensus.AlienchainBft;
import org.alienchain.consensus.AlienchainSync;
import org.alienchain.core.BftManager;
import org.alienchain.core.Blockchain;
import org.alienchain.core.BlockchainImpl;
import org.alienchain.core.Genesis;
import org.alienchain.core.PendingManager;
import org.alienchain.core.SyncManager;
import org.alienchain.core.Wallet;
import org.alienchain.crypto.Hex;
import org.alienchain.crypto.Key;
import org.alienchain.db.DatabaseFactory;
import org.alienchain.db.DatabaseName;
import org.alienchain.db.LeveldbDatabase;
import org.alienchain.db.LeveldbDatabase.LeveldbFactory;
import org.alienchain.event.KernelBootingEvent;
import org.alienchain.event.PubSub;
import org.alienchain.event.PubSubFactory;
import org.alienchain.net.ChannelManager;
import org.alienchain.net.NodeManager;
import org.alienchain.net.PeerClient;
import org.alienchain.net.PeerServer;
import org.alienchain.util.Bytes;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.ComputerSystem;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.OperatingSystem;

/**
 * Kernel holds references to each individual components.
 */
public class Kernel {

    // Fix JNA issue: There is an incompatible JNA native library installed
    static {
        System.setProperty("jna.nosys", "true");
    }

    private static final Logger logger = LoggerFactory.getLogger(Kernel.class);

    private static final PubSub pubSub = PubSubFactory.getDefault();

    public enum State {
        STOPPED, BOOTING, RUNNING, STOPPING
    }

    protected State state = State.STOPPED;

    protected final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
    protected Config config;
    protected Genesis genesis;

    protected Wallet wallet;
    protected Key coinbase;

    protected DatabaseFactory dbFactory;
    protected Blockchain chain;
    protected PeerClient client;

    protected ChannelManager channelMgr;
    protected PendingManager pendingMgr;
    protected NodeManager nodeMgr;

    protected PeerServer p2p;
    protected AlienchainApiService api;

    protected Thread consThread;
    protected AlienchainSync sync;
    protected AlienchainBft bft;

    /**
     * Creates a kernel instance and initializes it.
     * 
     * @param config
     *            the config instance
     * @prarm genesis the genesis instance
     * @param wallet
     *            the wallet instance
     * @param coinbase
     *            the coinbase key
     */
    public Kernel(Config config, Genesis genesis, Wallet wallet, Key coinbase) {
        this.config = config;
        this.genesis = genesis;
        this.wallet = wallet;
        this.coinbase = coinbase;
    }

    /**
     * Start the kernel.
     */
    public synchronized void start() {
        if (state != State.STOPPED) {
            return;
        } else {
            state = State.BOOTING;
            pubSub.publish(new KernelBootingEvent());
        }

        // ====================================
        // print system info
        // ====================================
        logger.info(config.getClientId());
        logger.info("System booting up: network = {}, networkVersion = {}, coinbase = {}", config.network(),
                config.networkVersion(),
                coinbase);
        printSystemInfo();

        // ====================================
        // initialize blockchain database
        // ====================================
        relocateDatabaseIfNeeded();
        dbFactory = new LeveldbFactory(config.databaseDir());
        chain = new BlockchainImpl(config, genesis, dbFactory);
        long number = chain.getLatestBlockNumber();
        logger.info("Latest block number = {}", number);

        // ====================================
        // set up client
        // ====================================
        client = new PeerClient(config, coinbase);

        // ====================================
        // start channel/pending/node manager
        // ====================================
        channelMgr = new ChannelManager(this);
        pendingMgr = new PendingManager(this);
        nodeMgr = new NodeManager(this);

        pendingMgr.start();
        nodeMgr.start();

        // ====================================
        // start p2p module
        // ====================================
        p2p = new PeerServer(this);
        p2p.start();

        // ====================================
        // start API module
        // ====================================
        api = new AlienchainApiService(this);
        if (config.apiEnabled()) {
            api.start();
        }

        // ====================================
        // start sync/consensus
        // ====================================
        sync = new AlienchainSync(this);
        bft = new AlienchainBft(this);

        consThread = new Thread(bft::start, "consensus");
        consThread.start();

        // ====================================
        // add port forwarding
        // ====================================
        new Thread(this::setupUpnp, "upnp").start();

        // ====================================
        // register shutdown hook
        // ====================================
        Launcher.registerShutdownHook("kernel", this::stop);

        state = State.RUNNING;
    }

    /**
     * Relocates database to the new location.
     * <p>
     * Old file structure:
     * <ul>
     * <li><code>./config</code></li>
     * <li><code>./database</code></li>
     * </ul>
     *
     * New file structure:
     * <ul>
     * <li><code>./config</code></li>
     * <li><code>./database/mainnet</code></li>
     * <li><code>./database/testnet</code></li>
     * </ul>
     *
     */
    protected void relocateDatabaseIfNeeded() {
        File databaseDir = new File(config.dataDir(), Constants.DATABASE_DIR);
        File blocksDir = new File(databaseDir, "block");

        if (blocksDir.exists()) {
            LeveldbDatabase db = new LeveldbDatabase(blocksDir);
            byte[] header = db.get(Bytes.merge((byte) 0x00, Bytes.of(0L)));
            db.close();

            if (header == null || header.length < 33) {
                logger.info("Unable to decode genesis header. Quit relocating");
            } else {
                String hash = Hex.encode(Arrays.copyOfRange(header, 1, 33));
                switch (hash) {
                case "1d4fb49444a5a14dbe68f5f6109808c68e517b893c1e9bbffce9d199b5037c8e":
                    moveDatabase(databaseDir, config.databaseDir(Network.MAINNET));
                    break;
                case "abfe38563bed10ec431a4a9ad344a212ef62f6244c15795324cc06c2e8fa0f8d":
                    moveDatabase(databaseDir, config.databaseDir(Network.TESTNET));
                    break;
                default:
                    logger.info("Unable to recognize genesis hash. Quit relocating");
                }
            }
        }
    }

    /**
     * Moves database to another directory.
     *
     * @param srcDir
     * @param dstDir
     */
    private void moveDatabase(File srcDir, File dstDir) {
        // store the sub-folders
        File[] files = srcDir.listFiles();

        // create the destination folder
        dstDir.mkdirs();

        // move to destination
        for (File f : files) {
            f.renameTo(new File(dstDir, f.getName()));
        }
    }

    /**
     * Prints system info.
     */
    protected void printSystemInfo() {
        try {
            SystemInfo si = new SystemInfo();
            HardwareAbstractionLayer hal = si.getHardware();

            // computer system
            ComputerSystem cs = hal.getComputerSystem();
            logger.info("Computer: manufacturer = {}, model = {}", cs.getManufacturer(), cs.getModel());

            // operating system
            OperatingSystem os = si.getOperatingSystem();
            logger.info("OS: name = {}", os);

            // cpu
            CentralProcessor cp = hal.getProcessor();
            logger.info("CPU: processor = {}, cores = {} / {}", cp, cp.getPhysicalProcessorCount(),
                    cp.getLogicalProcessorCount());

            // memory
            GlobalMemory m = hal.getMemory();
            long mb = 1024L * 1024L;
            logger.info("Memory: total = {} MB, available = {} MB, swap total = {} MB, swap available = {} MB",
                    m.getTotal() / mb,
                    m.getAvailable() / mb,
                    m.getSwapTotal() / mb,
                    (m.getSwapTotal() - m.getSwapUsed()) / mb);

            // disk
            for (HWDiskStore disk : hal.getDiskStores()) {
                logger.info("Disk: name = {}, size = {} MB", disk.getName(), disk.getSize() / mb);
            }

            // network
            for (NetworkIF net : hal.getNetworkIFs()) {
                logger.info("Network: name = {}, ip = [{}]", net.getDisplayName(), String.join(",", net.getIPv4addr()));
            }

            // java version
            logger.info("Java: version = {}, xmx = {} MB", System.getProperty("java.version"),
                    Runtime.getRuntime().maxMemory() / mb);
        } catch (RuntimeException e) {
            logger.error("Unable to retrieve System information.", e);
        }
    }

    /**
     * Sets up uPnP port mapping.
     */
    protected void setupUpnp() {
        try {
            GatewayDiscover discover = new GatewayDiscover();
            Map<InetAddress, GatewayDevice> devices = discover.discover();
            for (Map.Entry<InetAddress, GatewayDevice> entry : devices.entrySet()) {
                GatewayDevice gw = entry.getValue();
                logger.info("Found a gateway device: local address = {}, external address = {}",
                        gw.getLocalAddress().getHostAddress(), gw.getExternalIPAddress());

                gw.deletePortMapping(config.p2pListenPort(), "TCP");
                gw.addPortMapping(config.p2pListenPort(), config.p2pListenPort(), gw.getLocalAddress().getHostAddress(),
                        "TCP", "Alienchain P2P network");
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            logger.info("Failed to add port mapping", e);
        }
    }

    /**
     * Stops the kernel.
     */
    public synchronized void stop() {
        if (state != State.RUNNING) {
            return;
        } else {
            state = State.STOPPING;
        }

        // stop consensus
        try {
            sync.stop();
            bft.stop();

            // make sure consensus thread is fully stopped
            consThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Failed to stop sync/bft manager properly");
        }

        // stop API and p2p
        api.stop();
        p2p.stop();

        // stop pending manager and node manager
        pendingMgr.stop();
        nodeMgr.stop();

        // close client
        client.close();

        // make sure no thread is reading/writing the state
        ReentrantReadWriteLock.WriteLock lock = stateLock.writeLock();
        lock.lock();
        try {
            for (DatabaseName name : DatabaseName.values()) {
                dbFactory.getDB(name).close();
            }
        } finally {
            lock.unlock();
        }

        state = State.STOPPED;
    }

    /**
     * Returns the kernel state.
     *
     * @return
     */
    public State state() {
        return state;
    }

    /**
     * Returns the wallet.
     * 
     * @return
     */
    public Wallet getWallet() {
        return wallet;
    }

    /**
     * Returns the coinbase.
     * 
     * @return
     */
    public Key getCoinbase() {
        return coinbase;
    }

    /**
     * Returns the blockchain.
     * 
     * @return
     */
    public Blockchain getBlockchain() {
        return chain;
    }

    /**
     * Returns the peer client.
     * 
     * @return
     */
    public PeerClient getClient() {
        return client;
    }

    /**
     * Returns the pending manager.
     * 
     * @return
     */
    public PendingManager getPendingManager() {
        return pendingMgr;
    }

    /**
     * Returns the channel manager.
     * 
     * @return
     */
    public ChannelManager getChannelManager() {
        return channelMgr;
    }

    /**
     * Returns the node manager.
     * 
     * @return
     */
    public NodeManager getNodeManager() {
        return nodeMgr;
    }

    /**
     * Returns the config.
     * 
     * @return
     */
    public Config getConfig() {
        return config;
    }

    /**
     * Returns the state lock.
     * 
     * @return
     */
    public ReentrantReadWriteLock getStateLock() {
        return stateLock;
    }

    /**
     * Returns the syncing manager.
     * 
     * @return
     */
    public SyncManager getSyncManager() {
        return sync;
    }

    /**
     * Returns the BFT manager.
     * 
     * @return
     */
    public BftManager getBftManager() {
        return bft;
    }

    /**
     * Get instance of Alienchain API server
     *
     * @return API server
     */
    public AlienchainApiService getApi() {
        return api;
    }

    /**
     * Returns the p2p server instance.
     *
     * @return a {@link PeerServer} instance or null
     */
    public PeerServer getP2p() {
        return p2p;
    }

    public DatabaseFactory getDbFactory() {
        return dbFactory;
    }
}
