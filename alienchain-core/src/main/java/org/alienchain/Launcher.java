/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain;

import static org.alienchain.Network.MAINNET;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alienchain.cli.AlienchainOption;
import org.alienchain.config.Config;
import org.alienchain.config.Constants;
import org.alienchain.config.DevnetConfig;
import org.alienchain.config.MainnetConfig;
import org.alienchain.config.TestnetConfig;
import org.alienchain.event.PubSubFactory;
import org.alienchain.exception.LauncherException;
import org.alienchain.log.LoggerConfigurator;
import org.alienchain.message.CliMessages;
import org.alienchain.util.SystemUtil;
import org.alienchain.util.exception.UnreachableException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Launcher {

    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);

    private static final String ENV_ALIENCHAIN_WALLET_PASSWORD = "ALIENCHAIN_WALLET_PASSWORD";

    /**
     * Here we make sure that all shutdown hooks will be executed in the order of
     * registration. This is necessary to be manually maintained because
     * ${@link Runtime#addShutdownHook(Thread)} starts shutdown hooks concurrently
     * in unspecified order.
     */
    private static final List<Pair<String, Runnable>> shutdownHooks = Collections.synchronizedList(new ArrayList<>());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Launcher::shutdownHook, "shutdown-hook"));
    }

    private final Options options = new Options();

    private String dataDir = Constants.DEFAULT_DATA_DIR;
    private Network network = MAINNET;

    private Integer coinbase = null;
    private String password = null;

    public Launcher() {
        Option dataDirOption = Option.builder()
                .longOpt(AlienchainOption.DATA_DIR.toString())
                .desc(CliMessages.get("SpecifyDataDir"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("path").type(String.class)
                .build();
        addOption(dataDirOption);

        Option networkOption = Option.builder()
                .longOpt(AlienchainOption.NETWORK.toString()).desc(CliMessages.get("SpecifyNetwork"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("name").type(String.class)
                .build();
        addOption(networkOption);

        Option coinbaseOption = Option.builder()
                .longOpt(AlienchainOption.COINBASE.toString()).desc(CliMessages.get("SpecifyCoinbase"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName("index").type(Number.class)
                .build();
        addOption(coinbaseOption);

        Option passwordOption = Option.builder()
                .longOpt(AlienchainOption.PASSWORD.toString()).desc(CliMessages.get("WalletPassword"))
                .hasArg(true).numberOfArgs(1).optionalArg(false).argName(AlienchainOption.PASSWORD.toString())
                .type(String.class)
                .build();
        addOption(passwordOption);
    }

    /**
     * Creates an instance of {@link Config} based on the given `--network` option.
     * <p>
     * Defaults to MainNet.
     *
     * @return the configuration
     */
    public Config getConfig() {
        switch (getNetwork()) {
        case MAINNET:
            return new MainnetConfig(getDataDir());
        case TESTNET:
            return new TestnetConfig(getDataDir());
        case DEVNET:
            return new DevnetConfig(getDataDir());
        default:
            throw new UnreachableException();
        }
    }

    /**
     * Returns the network.
     *
     * @return
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Returns the data directory.
     *
     * @return
     */
    public String getDataDir() {
        return dataDir;
    }

    /**
     * Returns the coinbase.
     *
     * @return The specified coinbase, or NULL
     */
    public Integer getCoinbase() {
        return coinbase;
    }

    /**
     * Returns the provided password if any.
     *
     * @return The specified password, or NULL
     */
    public String getPassword() {
        return password;
    }

    /**
     * Parses options from the given arguments.
     *
     * @param args
     * @return
     * @throws ParseException
     */
    protected CommandLine parseOptions(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(getOptions(), args);

        if (cmd.hasOption(AlienchainOption.DATA_DIR.toString())) {
            setDataDir(cmd.getOptionValue(AlienchainOption.DATA_DIR.toString()));
        }

        if (cmd.hasOption(AlienchainOption.NETWORK.toString())) {
            String option = cmd.getOptionValue(AlienchainOption.NETWORK.toString());
            Network net = Network.of(option);
            if (net == null) {
                logger.error("Invalid network label: {}", option);
                SystemUtil.exit(SystemUtil.Code.INVALID_NETWORK_LABEL);
            } else {
                setNetwork(net);
            }
        }

        if (cmd.hasOption(AlienchainOption.COINBASE.toString())) {
            setCoinbase(((Number) cmd.getParsedOptionValue(AlienchainOption.COINBASE.toString())).intValue());
        }

        // Priority: arguments => system property => console input
        if (cmd.hasOption(AlienchainOption.PASSWORD.toString())) {
            setPassword(cmd.getOptionValue(AlienchainOption.PASSWORD.toString()));
        } else if (System.getenv(ENV_ALIENCHAIN_WALLET_PASSWORD) != null) {
            setPassword(System.getenv(ENV_ALIENCHAIN_WALLET_PASSWORD));
        }

        return cmd;
    }

    /**
     * Set up customized logger configuration.
     *
     * @param args
     * @throws ParseException
     */
    protected void setupLogger(String[] args) throws ParseException {
        // parse options
        parseOptions(args);

        LoggerConfigurator.configure(new File(dataDir));
    }

    /**
     * Set up pubsub service.
     */
    protected void setupPubSub() {
        PubSubFactory.getDefault().start();
        registerShutdownHook("pubsub-default", () -> PubSubFactory.getDefault().stop());
    }

    /**
     * Returns all supported options.
     *
     * @return
     */
    protected Options getOptions() {
        return options;
    }

    /**
     * Adds a supported option.
     *
     * @param option
     */
    protected void addOption(Option option) {
        options.addOption(option);
    }

    /**
     * Sets the network.
     *
     * @param network
     */
    protected void setNetwork(Network network) {
        this.network = network;
    }

    /**
     * Sets the data directory.
     *
     * @param dataDir
     */
    protected void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    /**
     * Sets the coinbase.
     *
     * @param coinbase
     */
    protected void setCoinbase(int coinbase) {
        this.coinbase = coinbase;
    }

    /**
     * Sets the password.
     *
     * @param password
     */
    protected void setPassword(String password) {
        this.password = password;
    }

    /**
     * Check runtime prerequisite.
     */
    protected static void checkPrerequisite() {
        switch (SystemUtil.getOsName()) {
        case WINDOWS:
            if (!SystemUtil.isWindowsVCRedist2012Installed()) {
                throw new LauncherException(
                        "Microsoft Visual C++ 2012 Redistributable Package is not installed. Please visit: https://www.microsoft.com/en-us/download/details.aspx?id=30679");
            }
            break;
        default:
        }
    }

    /**
     * Registers a shutdown hook which will be executed in the order of
     * registration.
     *
     * @param name
     * @param runnable
     */
    public static synchronized void registerShutdownHook(String name, Runnable runnable) {
        shutdownHooks.add(Pair.of(name, runnable));
    }

    /**
     * Call registered shutdown hooks in the order of registration.
     */
    private static synchronized void shutdownHook() {
        // shutdown hooks
        for (Pair<String, Runnable> r : shutdownHooks) {
            try {
                logger.info("Shutting down {}", r.getLeft());
                r.getRight().run();
            } catch (Exception e) {
                logger.info("Failed to shutdown {}", r.getLeft(), e);
            }
        }

        // flush log4j async loggers
        LogManager.shutdown();
    }
}
