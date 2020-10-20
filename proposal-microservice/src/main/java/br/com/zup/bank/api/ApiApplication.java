package br.com.zup.bank.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableMongoRepositories
@EnableRetry
@OpenAPIDefinition(
        tags = {
            @Tag(name = "proposal", description = "Proposal endpoints")
        },
        info = @Info(title = "ZupBank API",
                version = "1.0.0"
        )
)
public class ApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiApplication.class, args);
	}

}
