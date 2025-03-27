package korn03.tradeguardserver.service.user.connection;

import korn03.tradeguardserver.endpoints.dto.user.ExchangeAccountDTO;
import korn03.tradeguardserver.model.entity.user.connections.ExchangeProvider;
import korn03.tradeguardserver.model.entity.user.connections.UserExchangeAccount;
import korn03.tradeguardserver.model.repository.user.connections.UserBybitAccountRepository;
import korn03.tradeguardserver.service.core.EncryptionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserExchangeAccountService {

    private final UserBybitAccountRepository accountRepository;
    private final EncryptionService encryptionService;

    public UserExchangeAccountService(UserBybitAccountRepository accountRepository, EncryptionService encryptionService) {
        this.accountRepository = accountRepository;
        this.encryptionService = encryptionService;
    }
    public UserExchangeAccount saveBinanceAccount(
            Long userId,
            String accountName,
            String apiKey,
            String apiSecret
    ) {
        UserExchangeAccount account = new UserExchangeAccount();
        account.setUserId(userId);
        account.setAccountName(accountName);
        account.setProvider(ExchangeProvider.BINANCE);
        account.setEncryptedReadOnlyApiKey(encryptionService.encrypt(apiKey));
        account.setEncryptedReadOnlyApiSecret(encryptionService.encrypt(apiSecret));
        return accountRepository.save(account);
    }

    public UserExchangeAccount saveBybitAccount(
            Long userId,
            String accountName,
            String readOnlyApiKey,
            String readOnlyApiSecret,
            String readWriteApiKey,
            String readWriteApiSecret
    ) {
        UserExchangeAccount account = new UserExchangeAccount();
        account.setUserId(userId);
        account.setAccountName(accountName);
        account.setProvider(ExchangeProvider.BYBIT);
        account.setEncryptedReadOnlyApiKey(encryptionService.encrypt(readOnlyApiKey));
        account.setEncryptedReadOnlyApiSecret(encryptionService.encrypt(readOnlyApiSecret));
        account.setEncryptedReadWriteApiKey(encryptionService.encrypt(readWriteApiKey));
        account.setEncryptedReadWriteApiSecret(encryptionService.encrypt(readWriteApiSecret));
        return accountRepository.save(account);
    }

    public List<ExchangeAccountDTO> getUserExchangeAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream().map(account -> ExchangeAccountDTO.builder()
                .id(account.getId())
                .userId(account.getUserId())
                .provider(String.valueOf(account.getProvider()))
                .name(account.getAccountName())
                .readOnlyApiKey(getMaskedToken(getDecryptedReadOnlyApiKey(account)))
                .readOnlyApiSecret(getMaskedToken(getDecryptedReadOnlyApiSecret(account)))
                .readWriteApiKey(getMaskedToken(getDecryptedReadWriteApiKey(account)))
                .readWriteApiSecret(getMaskedToken(getDecryptedReadWriteApiSecret(account)))
                .build()
        ).toList();
    }
    public List<UserExchangeAccount> getUserExchangeAccountsEntites(Long userId) {
        return accountRepository.findByUserId(userId);
    }

    public Optional<UserExchangeAccount> getExchangeAccount(Long userId, Long id) {
        return accountRepository.findByUserIdAndId(userId, id);
    }
    public void deleteExchangeAccount(Long userId, Long id) {
        accountRepository.deleteByUserIdAndId(userId, id);
    }

    public String getDecryptedReadOnlyApiKey(UserExchangeAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadOnlyApiKey());
    }

    public String getDecryptedReadOnlyApiSecret(UserExchangeAccount account) {
        return encryptionService.decrypt(account.getEncryptedReadOnlyApiSecret());
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
