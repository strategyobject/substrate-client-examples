package com.strategyobject.substrateclient.examples.balancetransfer;

import com.strategyobject.substrateclient.pallet.annotations.Pallet;
import com.strategyobject.substrateclient.pallet.annotations.Storage;
import com.strategyobject.substrateclient.pallet.annotations.StorageHasher;
import com.strategyobject.substrateclient.pallet.annotations.StorageKey;
import com.strategyobject.substrateclient.rpc.types.AccountId;
import com.strategyobject.substrateclient.scale.annotations.Scale;
import com.strategyobject.substrateclient.storage.StorageNMap;

/*
 * Describe the pallet you want to interact with.
 * The source code is generated automatically.
 */

@Pallet("System")
public interface SystemPallet {

    @Storage(
        value = "Account",
        keys = {
            @StorageKey(
                type = @Scale(AccountId.class),
                hasher = StorageHasher.Blake2B128Concat
            )
        })
    StorageNMap<AccountInfo> account();

}
