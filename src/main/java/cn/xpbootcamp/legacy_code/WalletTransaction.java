package cn.xpbootcamp.legacy_code;

import cn.xpbootcamp.legacy_code.enums.STATUS;
import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
import cn.xpbootcamp.legacy_code.utils.IdGenerator;
import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;

import javax.transaction.InvalidTransactionException;

public class WalletTransaction {

    private String preAssignedId;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String orderId;
    private Long createdTimestamp;
    private Double amount;
    private STATUS status;
    private String walletTransactionId;

    private static long timeout = 1728000000;
    private static String header = "t_";

    public WalletTransaction(String preAssignedId, Long buyerId, Long sellerId, Long productId,
        String orderId){
        if (preAssignedId != null && !preAssignedId.isEmpty()) {
            this.preAssignedId = preAssignedId;
        } else {
            this.preAssignedId = IdGenerator.generateTransactionId();
        }
        if (!this.preAssignedId.startsWith(header)) {
            this.preAssignedId = header + preAssignedId;
        }
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.productId = productId;
        this.orderId = orderId;
        this.status = STATUS.TO_BE_EXECUTED;
        this.createdTimestamp = System.currentTimeMillis();
    }

    public boolean execute() throws InvalidTransactionException{
        if (isErrorWalletTransaction()) {
            throw new InvalidTransactionException("This is an invalid transaction");
        }
        if (isEndWalletTransaction()) {
            return true;
        }
        if (isNotGetLock()) {
            return false;
        }

        try {
            if (isTimeout()) {
                this.status = STATUS.EXPIRED;
                return false;
            }
            if (isMoveMoneySuccess()) {
                this.status = STATUS.EXECUTED;
                return true;
            } else {
                this.status = STATUS.FAILED;
                return false;
            }
        } finally {
            RedisDistributedLock.getSingletonInstance().unlock(preAssignedId);
        }
    }

    private boolean isMoveMoneySuccess() {
        this.walletTransactionId = new WalletServiceImpl()
            .moveMoney(preAssignedId, buyerId, sellerId, amount);
        return walletTransactionId != null;
    }

    private boolean isNotGetLock(){
        return !RedisDistributedLock.getSingletonInstance().lock(preAssignedId);
    }

    private boolean isTimeout(){
        return System.currentTimeMillis() - createdTimestamp > timeout;
    }

    private boolean isEndWalletTransaction(){
        return status == STATUS.EXECUTED;
    }

    private boolean isErrorWalletTransaction(){
        return buyerId == null || (sellerId == null || amount < 0.0);
    }

    public void setAmount(Double amount){
        this.amount = amount;
    }
}