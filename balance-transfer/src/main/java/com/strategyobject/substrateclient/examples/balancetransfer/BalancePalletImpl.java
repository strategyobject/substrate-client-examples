package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import com.strategyobject.substrateclient.crypto.KeyRing;
import com.strategyobject.substrateclient.rpc.api.AccountId;
import com.strategyobject.substrateclient.rpc.api.Address;
import com.strategyobject.substrateclient.rpc.api.AddressId;
import com.strategyobject.substrateclient.rpc.api.Call;
import com.strategyobject.substrateclient.rpc.api.Extrinsic;
import com.strategyobject.substrateclient.rpc.api.ImmortalEra;
import com.strategyobject.substrateclient.rpc.api.Signature;
import com.strategyobject.substrateclient.rpc.api.SignaturePayload;
import com.strategyobject.substrateclient.rpc.api.SignedExtra;
import com.strategyobject.substrateclient.rpc.api.SignedPayload;
import com.strategyobject.substrateclient.rpc.api.Sr25519Signature;
import com.strategyobject.substrateclient.rpc.api.primitives.BlockHash;
import com.strategyobject.substrateclient.rpc.api.primitives.BlockNumber;
import com.strategyobject.substrateclient.rpc.api.primitives.Index;
import com.strategyobject.substrateclient.rpc.api.section.Chain;
import com.strategyobject.substrateclient.rpc.api.section.System;
import com.strategyobject.substrateclient.scale.ScaleUtils;
import com.strategyobject.substrateclient.scale.ScaleWriter;
import com.strategyobject.substrateclient.scale.registries.ScaleWriterRegistry;
import org.bouncycastle.crypto.digests.Blake2bDigest;

/*
 * Extrinsic currently are not supported, so you have to implement it manually.
 */

public class BalancePalletImpl implements BalancePallet {
    private static final byte MODULE_INDEX = 6;
    private static final byte TRANSFER_INDEX = 0;
    private static final long SPEC_VERSION = 264;
    private static final long TX_VERSION = 2;
    private static final BigInteger TIP = BigInteger.valueOf(0);

    private final ScaleWriterRegistry scaleWriterRegistry;
    private final Chain chain;
    private final System system;

    public BalancePalletImpl(ScaleWriterRegistry scaleWriterRegistry, Chain chain, System system) {
        this.scaleWriterRegistry = scaleWriterRegistry;
        this.chain = chain;
        this.system = system;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> transfer(KeyRing signer,
                                                                                                     AddressId destination,
                                                                                                     BigInteger amount) {
        var call = new BalanceTransfer(MODULE_INDEX, TRANSFER_INDEX, destination, amount);
        var signerAddressId = AddressId.fromBytes(signer.getPublicKey().getBytes());

        return getGenesis()
            .thenCombineAsync(
                getNonce(signerAddressId.getAddress()),
                (genesis, nonce) ->
                    new SignedExtra<>(
                        SPEC_VERSION,
                        TX_VERSION,
                        genesis,
                        genesis,
                        new ImmortalEra(),
                        nonce,
                        TIP))
            .thenApplyAsync(extra -> {
                var writer = (ScaleWriter<? super SignedPayload<? super BalanceTransfer, ? super SignedExtra<ImmortalEra>>>) scaleWriterRegistry.resolve(SignedPayload.class);
                var signedPayload = ScaleUtils.toBytes(new SignedPayload<>(call, extra), writer);
                Signature signature = sign(signer, signedPayload);

                return Extrinsic.createSigned(
                    new SignaturePayload<>(
                        signerAddressId,
                        signature,
                        extra
                    ), call);
            });
    }

    private CompletableFuture<BlockHash> getGenesis() {
        return chain.getBlockHash(BlockNumber.GENESIS);
    }

    private CompletableFuture<Index> getNonce(AccountId accountId) {
        return system.accountNextIndex(accountId);
    }

    private static byte[] blake2(byte[] value) {
        Blake2bDigest digest = new Blake2bDigest(256);
        digest.update(value, 0, value.length);

        byte[] result = new byte[32];
        digest.doFinal(result, 0);
        return result;
    }

    private Signature sign(KeyRing keyRing, byte[] payload) {
        byte[] signature = payload.length > 256 ? blake2(payload) : payload;

        return Sr25519Signature.from(keyRing.sign(() -> signature));
    }
}
