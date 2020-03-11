package cn.xpbootcamp.legacy_code;

import static org.junit.jupiter.api.Assertions.assertFalse;

import cn.xpbootcamp.legacy_code.utils.RedisDistributedLock;
import javax.transaction.InvalidTransactionException;
import mockit.Mock;
import mockit.MockUp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WalletTransactionTest {

    @Test
    public void should_throw_except_when_buyerId_is_null() {
        WalletTransaction walletTransaction = new WalletTransaction("1", null, 1l, 1l, "1");
        Assertions.assertThrows(InvalidTransactionException.class, ()->{
            walletTransaction.execute();
        });
    }

    @Test
    public void should_throw_except_when_sellerId_is_null() {
        WalletTransaction walletTransaction = new WalletTransaction("1", 1l, null, 1l, "1");
        Assertions.assertThrows(InvalidTransactionException.class, ()->{
            walletTransaction.execute();
        });
    }

    @Test
    public void should_return_false_when_lock_error() throws InvalidTransactionException{
        String preAssignedId = "1";
        Long buyerId = 1l;
        Long sellerId = 1l;
        Long productId = 1l;
        String orderId = "1";

        new MockUp<RedisDistributedLock>(RedisDistributedLock.class) {
            @Mock
            public boolean lock(String unit) {
                return false;
            }
        };

        WalletTransaction walletTransaction = new WalletTransaction(preAssignedId, buyerId, sellerId, productId, orderId);
        walletTransaction.setAmount((double)1);
        assertFalse(walletTransaction.execute());
    }

}
