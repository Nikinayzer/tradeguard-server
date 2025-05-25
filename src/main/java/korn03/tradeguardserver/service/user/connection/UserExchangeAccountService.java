package korn03.tradeguardserver.service.user.connection;

import korn03.tradeguardserver.endpoints.dto.user.exchangeAccount.ExchangeAccountDTO;
import korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.model.repository.user.connections.UserBybitAccountRepository;
import korn03.tradeguardserver.service.core.EncryptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserExchangeAccountService {

    private final UserBybitAccountRepository accountRepository;
    private final EncryptionService encryptionService;

    public UserExchangeAccountService(UserBybitAccountRepository accountRepository, EncryptionService encryptionService) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }

    public UserExchangeAccount saveExchangeAccount(
            Long userId,
            String accountName,
            ExchangeProvider provider,
            String readWriteApiKey,
            String readWriteApiSecret
    ) {
        UserExchangeAccount account = new UserExchangeAccount();
        account.setUserId(userId);
        account.setAccountName(accountName);
        account.setProvider(provider);
        account.setEncryptedReadWriteApiKey(encryptionService.encrypt(readWriteApiKey));
        account.setEncryptedReadWriteApiSecret(encryptionService.encrypt(readWriteApiSecret));
        return accountRepository.save(account);
    }

    public UserExchangeAccount updateExchangeAccount(
            Long userId,
            Long id,
            String accountName,
            String readWriteApiKey,
            String readWriteApiSecret
    ) {
        UserExchangeAccount account = getExchangeAccount(userId, id);
        account.setAccountName(accountName);
        if (readWriteApiKey != null) {
            account.setEncryptedReadWriteApiKey(encryptionService.encrypt(readWriteApiKey));
        }
        if (readWriteApiSecret != null) {
            account.setEncryptedReadWriteApiSecret(encryptionService.encrypt(readWriteApiSecret));
        }
        accountRepository.save(account);
        return account;
    }

    public List<ExchangeAccountDTO> getUserExchangeAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream().map(account -> ExchangeAccountDTO.builder()
                .id(account.getId())
               // .userId(account.getUserId())
                .provider(String.valueOf(account.getProvider()))
                .name(account.getAccountName())
                .readWriteApiKey(getMaskedToken(getDecryptedReadWriteApiKey(account)))
                .readWriteApiSecret(getMaskedToken(getDecryptedReadWriteApiSecret(account)))
                .build()
        ).toList();
    }

    public List<UserExchangeAccount> getUserExchangeAccountsEntites(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public UserExchangeAccount getExchangeAccount(Long userId, Long id) {
        return accountRepository.findByUserIdAndId(userId, id).orElseThrow(() -> new RuntimeException("Exchange account not found"));
    }

    @Transactional
    public void deleteExchangeAccount(Long userId, Long id) {
        accountRepository.deleteByUserIdAndId(userId, id);
    }

    public String getDecryptedReadWriteApiKey(UserExchangeAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadWriteApiKey());
    }

    public String getDecryptedReadWriteApiSecret(UserExchangeAccount account) {
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
