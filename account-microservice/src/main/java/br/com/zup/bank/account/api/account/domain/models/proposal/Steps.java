
package br.com.zup.bank.account.api.account.domain.models.proposal;

import br.com.zup.bank.api.proposal.domain.enums.StatusApprovalEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

@Getter
@Setter
public class Steps {
    @Nullable private boolean isStep1Complete;
    @Nullable private boolean isStep2Complete;
    @Nullable private boolean isStep3Complete;
    @Nullable private StatusApprovalEnum isAcceptedByCustomer;
    @Nullable private StatusApprovalEnum isAcceptedByBank;
}
