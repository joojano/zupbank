
package br.com.zup.bank.account.api.email.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EmailInfo {
    private String destinatary;
    private String subject;
    private String message;
}
