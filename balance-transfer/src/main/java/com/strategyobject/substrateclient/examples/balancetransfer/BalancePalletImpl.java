package com.strategyobject.substrateclient.examples.balancetransfer;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

import com.strategyobject.substrateclient.crypto.KeyRing;
import com.strategyobject.substrateclient.rpc.Rpc;
import com.strategyobject.substrateclient.rpc.types.AccountId;
import com.strategyobject.substrateclient.rpc.types.Address;
import com.strategyobject.substrateclient.rpc.types.AddressId;
import com.strategyobject.substrateclient.rpc.types.BlockHash;
import com.strategyobject.substrateclient.rpc.types.Call;
import com.strategyobject.substrateclient.rpc.types.Extrinsic;
import com.strategyobject.substrateclient.rpc.types.ImmortalEra;
import com.strategyobject.substrateclient.rpc.types.Signature;
import com.strategyobject.substrateclient.rpc.types.SignaturePayload;
import com.strategyobject.substrateclient.rpc.types.SignedExtra;
import com.strategyobject.substrateclient.rpc.types.SignedPayload;
import com.strategyobject.substrateclient.rpc.types.Sr25519Signature;
import com.strategyobject.substrateclient.types.Signable;
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

    private final Rpc rpc;

    public BalancePalletImpl(Rpc rpc) {
        this.rpc = rpc;
    }

    @Override
    public CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> transfer(KeyRing signer,
                                                                                                     AddressId destination,
                                                                                                     BigInteger amount) {
        Call call = new BalanceTransfer(MODULE_INDEX, TRANSFER_INDEX, destination, amount);
        AddressId signerAddressId = AddressId.fromBytes(signer.getPublicKey().getData());

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
                        BigInteger.valueOf(nonce),
                        TIP))
            .thenApplyAsync(extra -> {
                SignedPayload<Call, SignedExtra<ImmortalEra>> signedPayload = new SignedPayload<>(call, extra);
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
        return rpc.chain().getBlockHash(0);
    }

    private CompletableFuture<Integer> getNonce(AccountId accountId) {
        return rpc.system().accountNextIndex(accountId);
    }

    private static byte[] blake2(byte[] value) {
        Blake2bDigest digest = new Blake2bDigest(256);
        digest.update(value, 0, value.length);

        byte[] result = new byte[32];
        digest.doFinal(result, 0);
        return result;
    }

    private Signature sign(KeyRing keyRing, Signable payload) {
        byte[] signed = payload.getBytes();
        byte[] signature = signed.length > 256 ? blake2(signed) : signed;

        return Sr25519Signature.from(keyRing.sign(() -> signature));
    }
}
