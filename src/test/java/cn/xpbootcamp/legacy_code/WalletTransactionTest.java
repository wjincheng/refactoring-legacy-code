package cn.xpbootcamp.legacy_code;

import javax.transaction.InvalidTransactionException;
import mockit.Mocked;
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

}
