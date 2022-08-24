package com.strategyobject.substrateclient.examples.balancetransfer;

import static java.lang.System.out;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.strategyobject.substrateclient.api.Api;
import com.strategyobject.substrateclient.api.pallet.balances.AccountData;
import com.strategyobject.substrateclient.api.pallet.system.System;
import com.strategyobject.substrateclient.pallet.storage.Arg;
import com.strategyobject.substrateclient.rpc.api.Address;
import com.strategyobject.substrateclient.rpc.api.Call;
import com.strategyobject.substrateclient.rpc.api.Extrinsic;
import com.strategyobject.substrateclient.rpc.api.ExtrinsicStatus;
import com.strategyobject.substrateclient.rpc.api.ImmortalEra;
import com.strategyobject.substrateclient.rpc.api.Signature;
import com.strategyobject.substrateclient.rpc.api.SignedExtra;
import com.strategyobject.substrateclient.rpc.api.section.Author;
import com.strategyobject.substrateclient.rpc.api.section.Chain;
import com.strategyobject.substrateclient.scale.registries.ScaleWriterRegistry;
import com.strategyobject.substrateclient.transport.ws.WsProvider;

public class Main {
    private static final int TIMEOUT = 30;

    // Need to set up SCALE writer registry manually to be able to serialize extrinsics.
    // Won't be needed since v0.3.0
    private static final ScaleWriterRegistry scaleWriterRegistry = new ScaleWriterRegistry();

    static {
        scaleWriterRegistry.registerAnnotatedFrom("com.strategyobject.substrateclient");
    }

    public static void main(String[] args) throws Exception {
        try (SubstrateContainer substrate = new SubstrateContainer("v3.0.0")) {
            // Run substrate in docker
            substrate.start();

            // Connect to the node running in docker
            var wsProvider = WsProvider.builder().setEndpoint(substrate.getWsAddress());
            try (var api = Api.with(wsProvider).build().join()) {
                var unsubscribe = subscribeEvents(api);

                printBalances(api);
                doTransfer(api, Account.ALICE, Account.BOB, BigInteger.valueOf(1111));
                printBalances(api);
                doTransfer(api, Account.BOB, Account.ALICE, BigInteger.valueOf(9999));
                printBalances(api);

                unsubscribe.get().join();
            }
        }

        java.lang.System.exit(0);
    }

    private static Supplier<CompletableFuture<Boolean>> subscribeEvents(Api api) {
        return api.pallet(System.class)
            .events()
            .subscribe((exception, block, value, keys) -> {
                if (value == null || value.size() < 2) {
                    return;
                }

                out.println("Events:");
                value.forEach(x -> out.printf("%s::(phase={\"ApplyExtrinsic\":%s})%n",
                    x.getEvent().getEvent().getClass().getSimpleName(),
                    x.getPhase().getApplyExtrinsicIndex()));
            }, Arg.EMPTY)
            .join();
    }

    private static void printBalances(Api api) {
        var system = api.pallet(System.class);
        var aliceAccountInfo = system.account().get(Account.ALICE.getAddressId().getAddress()).join();
        var bobAccountInfo = system.account().get(Account.BOB.getAddressId().getAddress()).join();

        out.printf("Balances:%nAlice: %d, Bob: %d%n%n",
            aliceAccountInfo.getData().into(AccountData.class).getFree(),
            bobAccountInfo.getData().into(AccountData.class).getFree());
    }

    private static void waitUntilInBlock(Author author,
                                         CompletableFuture<Extrinsic<Call, Address, Signature, SignedExtra<ImmortalEra>>> xt) {
        var xtStatus = new AtomicReference<>(ExtrinsicStatus.Status.READY);

        var unsubscribe = xt.thenCompose(
                x -> author.submitAndWatchExtrinsic(
                    x,
                    (exception, extrinsicStatus) -> xtStatus.set(extrinsicStatus.getStatus())))
            .join();

        await()
            .atMost(TIMEOUT, TimeUnit.SECONDS)
            .untilAtomic(xtStatus, equalTo(ExtrinsicStatus.Status.IN_BLOCK));

        unsubscribe.get().join();
    }

    private static void doTransfer(Api api, Account from, Account to, BigInteger amount) {
        out.printf("Transferring %d from %s to %s...%n", amount, from, to);

        var balancePallet = new BalancePalletImpl(
            scaleWriterRegistry,
            api.rpc(Chain.class),
            api.rpc(com.strategyobject.substrateclient.rpc.api.section.System.class));

        waitUntilInBlock(
            api.rpc(Author.class),
            balancePallet.transfer(from.getKeyRing(), to.getAddressId(), amount));
    }
}