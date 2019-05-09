/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.gui.model;

import static org.alienchain.core.Amount.ZERO;

import org.alienchain.core.Amount;
import org.alienchain.core.state.Delegate;

public class WalletDelegate extends Delegate {

    private Amount votesFromMe = ZERO;

    private long numberOfBlocksForged;
    private long numberOfTurnsHit;
    private long numberOfTurnsMissed;

    private Boolean isValidator;
    private Integer validatorPosition;

    public WalletDelegate(Delegate d) {
        this(d, false, null);
    }

    public WalletDelegate(Delegate d, Boolean isValidator, Integer validatorPosition) {
        super(d.getAddress(), d.getName(), d.getRegisteredAt(), d.getVotes());
        this.isValidator = isValidator;
        this.validatorPosition = validatorPosition;
    }

    public Amount getVotesFromMe() {
        return votesFromMe;
    }

    public void setVotesFromMe(Amount votesFromMe) {
        this.votesFromMe = votesFromMe;
    }

    public long getNumberOfBlocksForged() {
        return numberOfBlocksForged;
    }

    public void setNumberOfBlocksForged(long numberOfBlocksForged) {
        this.numberOfBlocksForged = numberOfBlocksForged;
    }

    public long getNumberOfTurnsHit() {
        return numberOfTurnsHit;
    }

    public void setNumberOfTurnsHit(long numberOfTurnsHit) {
        this.numberOfTurnsHit = numberOfTurnsHit;
    }

    public long getNumberOfTurnsMissed() {
        return numberOfTurnsMissed;
    }

    public void setNumberOfTurnsMissed(long numberOfTurnsMissed) {
        this.numberOfTurnsMissed = numberOfTurnsMissed;
    }

    public double getRate() {
        long total = numberOfTurnsHit + numberOfTurnsMissed;
        return total == 0 ? 0 : numberOfTurnsHit * 100.0 / total;
    }

    public boolean isValidator() {
        return isValidator;
    }

    public int getValidatorPosition() {
        return validatorPosition;
    }
}
