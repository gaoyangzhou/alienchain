/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.db;

import org.alienchain.config.Config;

public interface Migration {

    void migrate(Config config, DatabaseFactory dbFactory);
}
