
package br.com.zup.bank.api.proposal.models;

import lombok.Builder;

@Builder
public class Steps {
    private boolean isStep1Complete;
    private boolean isStep2Complete;
    private boolean isStep3Complete;
    private boolean isAccepted;
}
