package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import com.strategyobject.substrateclient.crypto.KeyRing;
import com.strategyobject.substrateclient.rpc.api.Address;
import com.strategyobject.substrateclient.rpc.api.AddressId;
import com.strategyobject.substrateclient.rpc.api.Call;
import com.strategyobject.substrateclient.rpc.api.Extrinsic;
import com.strategyobject.substrateclient.rpc.api.ImmortalEra;
import com.strategyobject.substrateclient.rpc.api.Signature;
import com.strategyobject.substrateclient.rpc.api.SignedExtra;

public interface BalancePallet {
    CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> transfer(KeyRing signer, AddressId destination, BigInteger amount);
}
