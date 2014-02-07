package com.playtech.wallet.domain.messages;

/**
 * Errors for WalletChangeResult
 */
public enum WalletChangeResultStatus {
    OK,
    NO_SUCH_PLAYER,
    PLAYER_BALANCE_LESS_THAN_ZERO,
    REPEATING_TRANSACTION,
    OPTIMISTIC_LOCKING_EXCEPTION
}
