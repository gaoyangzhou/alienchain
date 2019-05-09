/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.consensus.exception;

public class AlienchainBftException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AlienchainBftException() {
    }

    public AlienchainBftException(String s) {
        super(s);
    }

    public AlienchainBftException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AlienchainBftException(Throwable throwable) {
        super(throwable);
    }

    public AlienchainBftException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
