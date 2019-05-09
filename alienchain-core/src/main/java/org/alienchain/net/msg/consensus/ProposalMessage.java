/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import org.alienchain.consensus.Proposal;
import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;

public class ProposalMessage extends Message {

    private final Proposal proposal;

    public ProposalMessage(Proposal proposal) {
        super(MessageCode.BFT_PROPOSAL, null);

        this.proposal = proposal;

        // FIXME: consider wrapping by simple codec
        this.body = proposal.toBytes();
    }

    public ProposalMessage(byte[] body) {
        super(MessageCode.BFT_PROPOSAL, null);

        this.proposal = Proposal.fromBytes(body);

        this.body = body;
    }

    public Proposal getProposal() {
        return proposal;
    }

    @Override
    public String toString() {
        return "BFTProposalMessage: " + proposal;
    }
}
