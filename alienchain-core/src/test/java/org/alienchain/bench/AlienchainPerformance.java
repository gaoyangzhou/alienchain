/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.bench;

import static org.alienchain.core.Amount.Unit.MILLI_ALX;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.alienchain.config.Constants;
import org.alienchain.config.DevnetConfig;
import org.alienchain.util.Bytes;
import org.alienchain.util.ConsoleUtil;
import org.alienchain.util.SimpleApiClient;
import org.alienchain.util.TimeUtil;

public class AlienchainPerformance {
    private static String host = "127.0.0.1";
    private static int port = 9246;
    private static String username = "";
    private static String password = "";

    private static String address = "";
    private static int tps = 500;

    public static void testTransfer(int n) throws IOException, InterruptedException {
        DevnetConfig config = new DevnetConfig(Constants.DEFAULT_DATA_DIR);

        long t1 = TimeUtil.currentTimeMillis();
        for (int i = 1; i <= n; i++) {
            Map<String, Object> params = new HashMap<>();
            params.put("from", address);
            params.put("to", address);
            params.put("value", MILLI_ALX.of(1).getNano());
            params.put("fee", config.minTransactionFee().getNano());
            params.put("data", Bytes.EMPTY_BYTES);
            params.put("password", password);

            SimpleApiClient api = new SimpleApiClient(host, port, username, password);
            String response = api.post("/transaction/transfer", params);
            if (!response.contains("\"success\":true")) {
                System.out.println(response);
                return;
            }

            if (i % tps == 0) {
                System.out.println(new SimpleDateFormat("[HH:mm:ss]").format(new Date()) + " " + i);
                long t2 = TimeUtil.currentTimeMillis();
                Thread.sleep(Math.max(0, 1000 - (t2 - t1)));
                t1 = t2;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        address = ConsoleUtil.readPassword("Please enter your wallet address: ");
        username = ConsoleUtil.readPassword("Please enter your API username: ");
        password = ConsoleUtil.readPassword("Please enter your API password: ");

        while (true) {
            int n = Integer.parseInt(ConsoleUtil.readLine("# transactions to send: ").replaceAll("[^\\d]", ""));
            if (n > 0) {
                testTransfer(n);
            } else {
                break;
            }
        }
    }
}
