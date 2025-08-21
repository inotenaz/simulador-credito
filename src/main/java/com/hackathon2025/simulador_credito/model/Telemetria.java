package com.hackathon2025.simulador_credito.model;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name = "telemetria")
@Data
public class Telemetria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeApi;          // Nome do endpoint, ex: "/simulacoes"
    private Long tempoExecucao;      // em ms
    private LocalDate dataReferencia; 
    private int statusHttp;      

}

