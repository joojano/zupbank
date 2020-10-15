
package br.com.zup.bank.api.proposal.domain.models;

import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@Builder
@Getter
public class CPFImage {
    @NotNull private String pathImage;
    @Nullable private Date timestampAvaliation;
    @Nullable private StatusApprovalEnum approvalStatus;
}
