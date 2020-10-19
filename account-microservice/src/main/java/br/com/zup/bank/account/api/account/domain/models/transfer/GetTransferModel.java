
package br.com.zup.bank.account.api.account.domain.models.transfer;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GetTransferModel {
    private String cpf;
    private String bankNumber;
    private String agencyNumber;
    private String accountNumber;
}
