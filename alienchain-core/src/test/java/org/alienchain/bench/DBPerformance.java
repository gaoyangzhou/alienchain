/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.bench;

import java.io.File;

import org.alienchain.config.Constants;
import org.alienchain.db.LeveldbDatabase;
import org.alienchain.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBPerformance {
    private static final Logger logger = LoggerFactory.getLogger(DBPerformance.class);

    private static final int REPEAT = 100_000;

    private static LeveldbDatabase getTestDB() {
        return new LeveldbDatabase(new File(Constants.DEFAULT_DATA_DIR, "test"));
    }

    public static void testWrite() {
        LeveldbDatabase db = getTestDB();
        try {
            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                byte[] key = Bytes.random(256);
                byte[] value = Bytes.random(256);
                db.put(key, value);
            }
            long t2 = System.nanoTime();
            logger.info("Perf_db_write: " + (t2 - t1) / 1_000 / REPEAT + " μs/time");
        } finally {
            db.close();
        }
    }

    public static void testRead() {
        LeveldbDatabase db = getTestDB();
        try {
            long t1 = System.nanoTime();
            for (int i = 0; i < REPEAT; i++) {
                byte[] key = Bytes.random(256);
                db.get(key);
            }
            long t2 = System.nanoTime();
            logger.info("Perf_db_read: " + (t2 - t1) / 1_000 / REPEAT + " μs/time");
        } finally {
            db.close();
        }
    }

    public static void main(String[] args) {
        testWrite();
        testRead();

        LeveldbDatabase db = getTestDB();
        db.destroy();
    }
}
