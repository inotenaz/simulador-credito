package com.hackathon2025.simulador_credito;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.hackathon2025.simulador_credito.model.Produto;
import com.hackathon2025.simulador_credito.repository.ProdutoRepository;

@SpringBootApplication
public class SimuladorCreditoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimuladorCreditoApplication.class, args);
	}
	
   @Bean
    CommandLineRunner initDatabase(ProdutoRepository repository) {
        return args -> {
            repository.save(new Produto(
                1,
                "Produto 1",
                new BigDecimal("0.017900000"),
                (short) 0,
                (short) 24,
                new BigDecimal("200.00"),
                new BigDecimal("10000.00")
            ));
    
            repository.save(new Produto(
                2,
                "Produto 2",
                new BigDecimal("0.017500000"),
                (short) 25,
                (short) 48,
                new BigDecimal("10001.00"),
                new BigDecimal("100000.00")
            ));
    
            repository.save(new Produto(
                3,
                "Produto 3",
                new BigDecimal("0.018200000"),
                (short) 49,
                (short) 96,
                new BigDecimal("100000.01"),
                new BigDecimal("1000000.00")
            ));
    
            repository.save(new Produto(
                4,
                "Produto 4",
                new BigDecimal("0.015100000"),
                (short) 96,
                null,
                new BigDecimal("1000000.01"),
                null
            ));
        };
    }

}


