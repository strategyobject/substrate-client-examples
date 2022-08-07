package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;

import com.strategyobject.substrateclient.rpc.api.AddressId;
import com.strategyobject.substrateclient.rpc.api.Call;
import com.strategyobject.substrateclient.scale.ScaleType;
import com.strategyobject.substrateclient.scale.annotation.Scale;
import com.strategyobject.substrateclient.scale.annotation.ScaleWriter;

@ScaleWriter // Generate SCALE writer for this type automatically
public class BalanceTransfer implements Call {
    private final byte moduleIndex;

    private final byte callIndex;

    private final AddressId destination;

    @Scale(ScaleType.CompactBigInteger.class)
    private final BigInteger amount;

    public BalanceTransfer(byte moduleIndex, byte callIndex, AddressId destination, BigInteger amount) {
        this.moduleIndex = moduleIndex;
        this.callIndex = callIndex;
        this.destination = destination;
        this.amount = amount;
    }

    public byte getModuleIndex() {
        return moduleIndex;
    }

    public byte getCallIndex() {
        return callIndex;
    }

    public AddressId getDestination() {
        return destination;
    }

    public BigInteger getAmount() {
        return amount;
    }
}
