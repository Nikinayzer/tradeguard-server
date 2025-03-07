package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.model.entity.BybitAccount;
import korn03.tradeguardserver.model.entity.User;
import korn03.tradeguardserver.model.repository.BybitAccountRepository;
import korn03.tradeguardserver.service.core.EncryptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BybitAccountService {

    private final BybitAccountRepository accountRepository;
    private final EncryptionService encryptionService;

    public BybitAccountService(BybitAccountRepository accountRepository, EncryptionService encryptionService) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }

    public BybitAccount saveBybitAccount(Long userId, String accountName, String apiKey, String apiSecret) {
        BybitAccount account = new BybitAccount();
        account.setUserId(userId);
        account.setAccountName(accountName);
        account.setEncryptedApiKey(encryptionService.encrypt(apiKey));
        account.setEncryptedApiSecret(encryptionService.encrypt(apiSecret));
        return accountRepository.save(account);
    }
//    public User getUserByBybitAccountId(Long accountId) {
//        return accountRepository.findById(accountId).map(BybitAccount::getUserId).map(User::new).orElse(null);
//    }

    public List<BybitAccount> getUserBybitAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<BybitAccount> getBybitAccount(Long userId, String accountName) {
        return accountRepository.findByUserIdAndAccountName(userId, accountName);
    }
}
