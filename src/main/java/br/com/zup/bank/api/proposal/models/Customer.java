
package br.com.zup.bank.api.proposal.models;

import java.util.Date;
import lombok.Builder;

@Builder
public class Customer {
    private String name;
    private String surname;
    private String email;
    private Date birthDate;
    private Date cpf;
}
