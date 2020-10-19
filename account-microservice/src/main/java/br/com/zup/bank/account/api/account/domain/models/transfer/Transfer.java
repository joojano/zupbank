
package br.com.zup.bank.account.api.account.domain.models.transfer;

import java.math.BigDecimal;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.lang.NonNull;

@Getter
@Builder
public class Transfer {
    @Id private String id;
    @NonNull private BigDecimal ammount;
    @NonNull private Date realizationDate;
    @NonNull private String idDocumentOrigin;
    @NonNull private String bankOrigin;
    @NonNull private String accountOrigin;
    @NonNull private String agencyOrigin;
    @NonNull private String uniqueCodeOrigin;
    @NonNull private String accountDestiny;
    @NonNull private String agencyDestiny;
}
