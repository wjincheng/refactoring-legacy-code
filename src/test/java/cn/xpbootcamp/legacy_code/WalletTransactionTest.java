package cn.xpbootcamp.legacy_code;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cn.xpbootcamp.legacy_code.service.WalletServiceImpl;
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
    public void should_throw_except_when_amount_less_than_zero() throws InvalidTransactionException{
        WalletTransaction walletTransaction = new WalletTransaction("1", 1l, 1l, 1l, "1");
        walletTransaction.setAmount((double) -1);
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
//        assertEquals(walletTransaction.execute(), is(false));
    }

    @Test
    public void should_return_false_when_timeout_20_days() throws InvalidTransactionException{
        String preAssignedId = "1";
        Long buyerId = 1l;
        Long sellerId = 1l;
        Long productId = 1l;
        String orderId = "1";

        new MockUp<RedisDistributedLock>(RedisDistributedLock.class) {
            @Mock
            public boolean lock(String unit) {
                return true;
            }

            @Mock
            public void unlock(String unit) {
            }
        };

        new MockUp<System>(System.class) {
            @Mock
            @SuppressWarnings("unused")
            long currentTimeMillis() {
                return 0L;
            }
        };

        WalletTransaction walletTransaction = new WalletTransaction(preAssignedId, buyerId, sellerId, productId, orderId);

        new MockUp<System>(System.class) {
            @Mock
            @SuppressWarnings("unused")
            long currentTimeMillis() {
                return 17280000000l;
            }
        };

        walletTransaction.setAmount((double)1);
        assertFalse(walletTransaction.execute());
    }

    @Test
    public void should_return_true() throws InvalidTransactionException{
        String preAssignedId = "1";
        Long buyerId = 1l;
        Long sellerId = 1l;
        Long productId = 1l;
        String orderId = "1";

        new MockUp<RedisDistributedLock>(RedisDistributedLock.class) {
            @Mock
            public boolean lock(String unit) {
                return true;
            }

            @Mock
            public void unlock(String unit) {
            }
        };

        new MockUp<WalletServiceImpl>(WalletServiceImpl.class) {

            @Mock
            public String moveMoney(String id, long buyerId, long sellerId, double amount) {
                return "1";
            }
        };

        WalletTransaction walletTransaction = new WalletTransaction(preAssignedId, buyerId, sellerId, productId, orderId);
        walletTransaction.setAmount((double)1);
        assertTrue(walletTransaction.execute());
        assertTrue(walletTransaction.execute());
    }

    @Test
    public void should_return_false_when_move_money_error() throws InvalidTransactionException{
        String preAssignedId = "1";
        Long buyerId = 1l;
        Long sellerId = 1l;
        Long productId = 1l;
        String orderId = "1";

        new MockUp<RedisDistributedLock>(RedisDistributedLock.class) {
            @Mock
            public boolean lock(String unit) {
                return true;
            }

            @Mock
            public void unlock(String unit) {
            }
        };

        new MockUp<WalletServiceImpl>(WalletServiceImpl.class) {

            @Mock
            public String moveMoney(String id, long buyerId, long sellerId, double amount) {
                return null;
            }
        };

        WalletTransaction walletTransaction = new WalletTransaction(preAssignedId, buyerId, sellerId, productId, orderId);
        walletTransaction.setAmount((double)1);
        assertFalse(walletTransaction.execute());
    }

}
