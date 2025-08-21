package com.hackathon2025.simulador_credito.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "simulacoes")
public class Simulacao {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "simulacoes_seq")
    @SequenceGenerator(
        name = "simulacoes_seq",
        sequenceName = "simulacoes_id_seq",
        allocationSize = 1,
        initialValue = 10000000
    )
    @Column(name = "id_simulacao", nullable = false, updatable = false)
    private Long idSimulacao;

    @Column(name = "valor_desejado", precision = 15, scale = 2)
    private BigDecimal valorDesejado;

    @Column(name = "prazo")
    private Integer prazo;

    @Column(name = "codigo_produto")
    private Integer codigoProduto;

    @Column(name = "descricao_produto", length = 200)
    private String descricaoProduto;

    @Column(name = "taxa_juros", precision = 8, scale = 4)
    private BigDecimal taxaJuros;

    @Column(name = "valor_medio_prestacao", precision = 15, scale = 2)
    private BigDecimal valorMedioPrestacao;

    @Column(name = "valor_total_parcelas", precision = 15, scale = 2)
    private BigDecimal valorTotalParcelas;

    @Column(name = "data_simulacao")
    private LocalDateTime dataSimulacao;

    @PrePersist
    protected void onCreate() {
        if (dataSimulacao == null) {
            dataSimulacao = LocalDateTime.now();
        }
    }
}
