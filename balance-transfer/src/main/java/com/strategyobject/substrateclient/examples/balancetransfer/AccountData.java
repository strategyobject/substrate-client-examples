package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;

import com.strategyobject.substrateclient.scale.ScaleType;
import com.strategyobject.substrateclient.scale.annotations.Scale;
import com.strategyobject.substrateclient.scale.annotations.ScaleReader;

@ScaleReader
public class AccountData {
    @Scale(ScaleType.U128.class)
    private BigInteger free;

    @Scale(ScaleType.U128.class)
    private BigInteger reserved;

    @Scale(ScaleType.U128.class)
    private BigInteger miscFrozen;

    @Scale(ScaleType.U128.class)
    private BigInteger feeFrozen;

    public BigInteger getFree() {
        return free;
    }

    public void setFree(BigInteger free) {
        this.free = free;
    }

    public BigInteger getReserved() {
        return reserved;
    }

    public void setReserved(BigInteger reserved) {
        this.reserved = reserved;
    }

    public BigInteger getMiscFrozen() {
        return miscFrozen;
    }

    public void setMiscFrozen(BigInteger miscFrozen) {
        this.miscFrozen = miscFrozen;
    }

    public BigInteger getFeeFrozen() {
        return feeFrozen;
    }

    public void setFeeFrozen(BigInteger feeFrozen) {
        this.feeFrozen = feeFrozen;
    }
}
