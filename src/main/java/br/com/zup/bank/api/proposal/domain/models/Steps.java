
package br.com.zup.bank.api.proposal.domain.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Steps {
    private boolean isStep1Complete;
    private boolean isStep2Complete;
    private boolean isStep3Complete;
    private boolean isAccepted;
}
