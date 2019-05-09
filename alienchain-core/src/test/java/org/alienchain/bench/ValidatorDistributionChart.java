/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.bench;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.alienchain.config.Constants;
import org.alienchain.config.MainnetConfig;
import org.alienchain.util.BigIntegerUtil;

/**
 * This program generates a chart that shows the distribution of selected
 * primary validator from block 0 to block 1,000,000. Credits to:
 * https://github.com/r1d1-btct
 */
public class ValidatorDistributionChart {

    public static void main(String[] args) {
        Random random = new Random();

        MainnetConfig mainnetConfig = new MainnetConfig(Constants.DEFAULT_DATA_DIR);
        HashMap<Integer, AtomicInteger> mapPRNG = new HashMap<>();
        HashMap<Integer, AtomicInteger> mapPRNG_fast = new HashMap<>();

        for (int i = 0; i < 100; i++) {
            mapPRNG.put(i, new AtomicInteger(0));
            mapPRNG_fast.put(i, new AtomicInteger(0));
        }

        int view, vPRNG, vPRNG_fast;
        BigInteger seed;
        final List<String> validators = IntStream.range(0, 100).boxed().map(i -> Integer.toString(i))
                .collect(Collectors.toList());
        for (long height = 0; height < 1_000_000; height++) {
            view = random.nextDouble() < 0.05 ? 1 : 0; // about 5%
            seed = BigIntegerUtil
                    .random(BigInteger.valueOf(height))
                    .xor(BigIntegerUtil.random(BigInteger.valueOf(view)));
            vPRNG = new Random(seed.longValue()).nextInt(100);
            vPRNG_fast = Integer.valueOf(mainnetConfig.getPrimaryValidator(validators, height, view, true));

            mapPRNG.get(vPRNG).incrementAndGet();
            mapPRNG_fast.get(vPRNG_fast).incrementAndGet();
        }

        long sumPRNG = mapPRNG.entrySet().stream().filter(e -> e.getKey() < 100)
                .mapToLong(e -> e.getValue().longValue()).sum();
        long sumPRNG_fast = mapPRNG_fast.entrySet().stream().filter(e -> e.getKey() < 100)
                .mapToLong(e -> e.getValue().longValue()).sum();

        double[] percentagePRNG = mapPRNG.entrySet().stream().filter(e -> e.getKey() < 100)
                .mapToDouble(e -> e.getValue().doubleValue() / (double) sumPRNG * 100).toArray();
        double[] percentagePRNG_fast = mapPRNG_fast.entrySet().stream().filter(e -> e.getKey() < 100)
                .mapToDouble(e -> e.getValue().doubleValue() / (double) sumPRNG_fast * 100).toArray();

        System.out.println("Number\tPRNG\tPRNG_fast");
        for (int i = 0; i < 100; i++) {
            System.out.format("%d\t%.2f\t%.2f\n", i, percentagePRNG[i] * 100, percentagePRNG_fast[i] * 100);
        }

        // Create Chart
        XYChart chart = new XYChartBuilder().width(800).height(600).title("Validator Distribution")
                .xAxisTitle("Primary Validator").yAxisTitle("%").build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideSW);

        // Series
        chart.addSeries("Java Random", IntStream.rangeClosed(0, 99).boxed().mapToDouble(i -> (double) i).toArray(),
                percentagePRNG);
        chart.addSeries("Intel fastrand", IntStream.rangeClosed(0, 99).boxed().mapToDouble(i -> (double) i).toArray(),
                percentagePRNG_fast);

        // Display
        new SwingWrapper<>(chart).displayChart();
    }
}