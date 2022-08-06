package com.strategyobject.substrateclient.examples.balancetransfer;

import com.strategyobject.substrateclient.scale.ScaleType;
import com.strategyobject.substrateclient.scale.annotations.Scale;
import com.strategyobject.substrateclient.scale.annotations.ScaleReader;

@ScaleReader // Generate SCALE reader for this type automatically
public class AccountInfo {
    @Scale(ScaleType.U32.class) // Instructs Api to use `U32Reader` for this field (`I32Reader` is used by default for `Long`)
    private Long nonce;

    @Scale(ScaleType.U32.class)
    private Long consumers;

    @Scale(ScaleType.U32.class)
    private Long providers;

    private AccountData data;

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public Long getConsumers() {
        return consumers;
    }

    public void setConsumers(Long consumers) {
        this.consumers = consumers;
    }

    public Long getProviders() {
        return providers;
    }

    public void setProviders(Long providers) {
        this.providers = providers;
    }

    public AccountData getData() {
        return data;
    }

    public void setData(AccountData data) {
        this.data = data;
    }
}
