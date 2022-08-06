package com.strategyobject.substrateclient.examples.balancetransfer;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.strategyobject.substrateclient.api.Api;
import com.strategyobject.substrateclient.api.DefaultApi;
import com.strategyobject.substrateclient.rpc.Rpc;
import com.strategyobject.substrateclient.rpc.types.Address;
import com.strategyobject.substrateclient.rpc.types.Call;
import com.strategyobject.substrateclient.rpc.types.Extrinsic;
import com.strategyobject.substrateclient.rpc.types.ExtrinsicStatus;
import com.strategyobject.substrateclient.rpc.types.ImmortalEra;
import com.strategyobject.substrateclient.rpc.types.Signature;
import com.strategyobject.substrateclient.rpc.types.SignedExtra;
import com.strategyobject.substrateclient.transport.ws.WsProvider;

public class Main {
    private static final int TIMEOUT = 30;

    public static void main(String[] args) throws Exception {
        try (SubstrateContainer substrate = new SubstrateContainer("v3.0.0")) {
            // Run substrate in docker
            substrate.start();

            // Connect to the node running in docker
            var wsProvider = WsProvider.builder().setEndpoint(substrate.getWsAddress()).build();
            wsProvider.connect().join();

            try (DefaultApi api = Api.with(wsProvider)) {
                printBalances(api);

                doTransfer(api, Account.ALICE, Account.BOB, BigInteger.valueOf(1111));

                printBalances(api);

                doTransfer(api, Account.BOB, Account.ALICE, BigInteger.valueOf(9999));

                printBalances(api);
            }
        }
    }

    private static void printBalances(Api api) {
        SystemPallet systemPallet = api.pallet(SystemPallet.class);
        AccountInfo aliceAccountInfo = systemPallet.account().get(Account.ALICE.getAddressId().getAddress()).join();
        AccountInfo bobAccountInfo = systemPallet.account().get(Account.BOB.getAddressId().getAddress()).join();

        System.out.printf("Balances: Alice: %d, Bob: %d%n", aliceAccountInfo.getData().getFree(), bobAccountInfo.getData().getFree());
    }

    private static void waitUntilInBlock(Rpc rpc,
                                         CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> xt) {
        AtomicReference<ExtrinsicStatus.Status> xtStatus = new AtomicReference<>(ExtrinsicStatus.Status.Ready);

        Supplier<CompletableFuture<Boolean>> unsubscribe = xt.thenCompose(
                x -> rpc.author().submitAndWatchExtrinsic(
                    x,
                    (exception, extrinsicStatus) -> xtStatus.set(extrinsicStatus.getStatus())))
            .join();

        await()
            .atMost(TIMEOUT, TimeUnit.SECONDS)
            .untilAtomic(xtStatus, equalTo(ExtrinsicStatus.Status.InBlock));

        unsubscribe.get().join();
    }

    private static void doTransfer(Api api, Account from, Account to, BigInteger amount) {
        System.out.printf("Transferring %d from %s to %s...%n", amount, from, to);

        BalancePallet balancePallet = new BalancePalletImpl(api.rpc());
        waitUntilInBlock(api.rpc(), balancePallet.transfer(from.getKeyRing(), to.getAddressId(), amount));
    }
}