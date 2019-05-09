/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.config;

import java.util.HashMap;
import java.util.Map;

import org.alienchain.Network;
import org.alienchain.core.Fork;
import org.apache.commons.collections4.MapUtils;

public class MainnetConfig extends AbstractConfig {

    private static final Map<Long, byte[]> checkpoints;
    static {
        HashMap<Long, byte[]> initCheckpoints = new HashMap<>();

        checkpoints = MapUtils.unmodifiableMap(initCheckpoints);
    }

    private static final Map<Fork, Long> forkActivationCheckpoints;
    static {
        HashMap<Fork, Long> initForkActivationCheckpoints = new HashMap<>();

        forkActivationCheckpoints = MapUtils.unmodifiableMap(initForkActivationCheckpoints);
    }

    public MainnetConfig(String dataDir) {
        super(dataDir, Network.MAINNET, Constants.MAINNET_VERSION);

        this.forkUniformDistributionEnabled = true;
        this.forkVirtualMachineEnabled = false;
    }

    @Override
    public Map<Long, byte[]> checkpoints() {
        return checkpoints;
    }

    @Override
    public Map<Fork, Long> forkActivationCheckpoints() {
        return forkActivationCheckpoints;
    }
}
