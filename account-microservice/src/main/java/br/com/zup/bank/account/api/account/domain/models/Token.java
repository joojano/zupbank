
package br.com.zup.bank.account.api.account.domain.models;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class Token {
    private int minutesTokenDuration;
    @NonNull private Date createdAt;
    @NonNull private Date invalidAfter;
    @Nullable private int token;
}
