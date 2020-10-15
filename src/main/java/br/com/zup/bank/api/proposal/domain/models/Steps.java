
package br.com.zup.bank.api.proposal.domain.models;

import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class Steps {
    @Nullable private boolean isStep1Complete;
    @Nullable private boolean isStep2Complete;
    @Nullable private boolean isStep3Complete;
    @Nullable private boolean isAcceptedByCustomer;
    @Nullable private boolean isAcceptedByBank;
}
