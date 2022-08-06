package com.strategyobject.substrateclient.examples.balancetransfer;

import com.strategyobject.substrateclient.common.utils.HexConverter;
import com.strategyobject.substrateclient.crypto.KeyRing;
import com.strategyobject.substrateclient.rpc.types.AddressId;
import com.strategyobject.substrateclient.types.KeyPair;

public enum Account {

    ALICE("0x98319d4ff8a9508c4bb0cf0b5a78d760a0b2082c02"
        + "775e6e82370816fedfff48925a225d97aa00682d6a59b95b18780c10d7032336e88f3442b42361f4a66011d43593c7"
        + "15fdd31c61141abd04a99fd6822c8558854ccde39a5684e7a56da27d"),

    BOB("0x081ff694633e255136bdb456c20a5fc8fed21f8b964c"
        + "11bb17ff534ce80ebd5941ae88f85d0c1bfc37be41c904e1dfc01de8c8067b0d6d5df25dd1ac0894a3258eaf041516"
        + "87736326c9fea17e25fc5287613693c912909cb226aa4794f26a48");

    private final AddressId addressId;
    private final KeyRing keyRing;

    Account(String hex) {
        KeyPair keyPair = KeyPair.fromBytes(HexConverter.toBytes(hex));
        this.addressId = AddressId.fromBytes(keyPair.asPublicKey().getData());
        this.keyRing = KeyRing.fromKeyPair(keyPair);
    }

    public AddressId getAddressId() {
        return addressId;
    }

    public KeyRing getKeyRing() {
        return keyRing;
    }

}
