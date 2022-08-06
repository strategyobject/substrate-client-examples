package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import com.strategyobject.substrateclient.crypto.KeyRing;
import com.strategyobject.substrateclient.rpc.types.Address;
import com.strategyobject.substrateclient.rpc.types.AddressId;
import com.strategyobject.substrateclient.rpc.types.Call;
import com.strategyobject.substrateclient.rpc.types.Extrinsic;
import com.strategyobject.substrateclient.rpc.types.ImmortalEra;
import com.strategyobject.substrateclient.rpc.types.Signature;
import com.strategyobject.substrateclient.rpc.types.SignedExtra;

public interface BalancePallet {
    CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> transfer(KeyRing signer, AddressId destination, BigInteger amount);
}
