package com.playtech.wallet.domain.messages;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.UUID;


/**
 * Outgoing message from controller
 */
@XmlRootElement
public class WalletChangeResult {

    /**
     * Client settable transaction identifier,
     * will be used to check if message has allready persisted
     */
    private UUID transactionId;

    /**
     * Error codes @see WalletChangeResultStatus
     */
    private WalletChangeResultStatus errorCode = WalletChangeResultStatus.OK;

    /**
     * JPA version field for optimistic locking
     * Can be null, if @see Player does not exist
     */
    private Long balanceVersion;

    /**
     * The amount of change in balance. Sent by @see WalletChangeMessage
     */
    private BigDecimal balanceChange;


    /**
     * @see com.playtech.wallet.domain.Player total balance, can be null if user does not exist
     */
    private BigDecimal totalBalance;

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public WalletChangeResultStatus getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(WalletChangeResultStatus errorCode) {
        this.errorCode = errorCode;
    }

    public Long getBalanceVersion() {
        return balanceVersion;
    }

    public void setBalanceVersion(Long balanceVersion) {
        this.balanceVersion = balanceVersion;
    }

    public BigDecimal getBalanceChange() {
        return balanceChange;
    }

    public void setBalanceChange(BigDecimal balanceChange) {
        this.balanceChange = balanceChange;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    @Override
    public String toString() {
        return "WalletChangeResult{" +
                "transactionId=" + transactionId +
                ", errorCode='" + errorCode + '\'' +
                ", balanceVersion=" + balanceVersion +
                ", balanceChange=" + balanceChange +
                ", totalBalance=" + totalBalance +
                '}';
    }
}
