
package br.com.zup.bank.account.api.account.domain.models;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class Token {
    @NonNull private int minutesTokenDuration;
    @NonNull private long createdAt;
    @NonNull private long invalidAfter;
    @NonNull private boolean isTokenUsed;
    @Nullable private String token;
    
}
