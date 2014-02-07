package com.playtech.gamesimulator.service;

import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.domain.messages.WalletChangeResult;

/**
 * Created by svenzik on 2/7/14.
 */
public interface WalletServiceClient {
    WalletChangeResult sendMessage(WalletChangeMessage message) throws WalletServiceException;
}
