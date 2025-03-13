package korn03.tradeguardserver.service.user;

import korn03.tradeguardserver.model.entity.user.UserBybitAccount;
import korn03.tradeguardserver.model.repository.user.UserBybitAccountRepository;
import korn03.tradeguardserver.service.core.EncryptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserBybitAccountService {

    private final UserBybitAccountRepository accountRepository;
    private final EncryptionService encryptionService;

    public UserBybitAccountService(UserBybitAccountRepository accountRepository, EncryptionService encryptionService) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }

    public UserBybitAccount saveBybitAccount(
            Long userId,
            String accountName,
            String readOnlyApiKey,
            String readOnlyApiSecret,
            String readWriteApiKey,
            String readWriteApiSecret
    ) {
        UserBybitAccount account = new UserBybitAccount();
        account.setUserId(userId);
        account.setAccountName(accountName);
        account.setEncryptedReadOnlyApiKey(encryptionService.encrypt(readOnlyApiKey));
        account.setEncryptedReadOnlyApiSecret(encryptionService.encrypt(readOnlyApiSecret));
        account.setEncryptedReadWriteApiKey(encryptionService.encrypt(readWriteApiKey));
        account.setEncryptedReadWriteApiSecret(encryptionService.encrypt(readWriteApiSecret));
        return accountRepository.save(account);
    }

    public List<UserBybitAccount> getUserBybitAccounts(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<UserBybitAccount> getBybitAccount(Long userId, Long id) {
        return accountRepository.findByUserIdAndId(userId, id);
    }
    public void deleteBybitAccount(Long userId, Long id) {
        accountRepository.deleteByUserIdAndId(userId, id);
    }

    public String getDecryptedReadOnlyApiKey(UserBybitAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadOnlyApiKey());
    }

    public String getDecryptedReadOnlyApiSecret(UserBybitAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadOnlyApiSecret());
    }

    public String getDecryptedReadWriteApiKey(UserBybitAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadWriteApiKey());
    }

    public String getDecryptedReadWriteApiSecret(UserBybitAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadWriteApiSecret());
    }

    public String getMaskedToken(String token) {
        if (token == null || token.length() < 6) {
            return "****"; // Fallback for shorties
        }

        int maskLength = token.length() - 6;
        return "*".repeat(maskLength) + token.substring(maskLength);
    }

}
