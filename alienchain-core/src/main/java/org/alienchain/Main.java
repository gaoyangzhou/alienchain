/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 * <p>
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain;

import org.alienchain.cli.AlienchainCli;
import org.alienchain.gui.AlienchainGui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    private static final String CLI = "--cli";
    private static final String GUI = "--gui";


    public static void main(String[] args) {

        List<String> startArgs = new ArrayList<>();
        boolean startGui = true;
        for (String arg : args) {
            if (CLI.equals(arg)) {
                startGui = false;
            } else if (GUI.equals(arg)) {
                startGui = true;
            } else {
                startArgs.add(arg);
            }
        }

        try {
            org.alienchain.ui.lnf.beautyeye.BeautyEyeLNFHelper.launchBeautyEyeLNF();
        } catch (Exception e) {

        }

        if (startGui) {
            AlienchainGui.main(startArgs.toArray(new String[0]));
        } else {
            AlienchainCli.main(startArgs.toArray(new String[0]));
        }

    }

}
