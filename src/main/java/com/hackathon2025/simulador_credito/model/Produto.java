package com.hackathon2025.simulador_credito.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PRODUTO")
public class Produto {

    @Id
    @Column(name = "CO_PRODUTO", nullable = false)
    private Integer coProduto;

    @Column(name = "NO_PRODUTO", nullable = false, length = 200)
    private String noProduto;

    @Column(name = "PC_TAXA_JUROS", nullable = false, precision = 10, scale = 9)
    private BigDecimal pcTaxaJuros;

    @Column(name = "NU_MINIMO_MESES", nullable = false)
    private Short nuMinimoMeses;

    @Column(name = "NU_MAXIMO_MESES")
    private Short nuMaximoMeses;

    @Column(name = "VR_MINIMO", nullable = false, precision = 18, scale = 2)
    private BigDecimal vrMinimo;

    @Column(name = "VR_MAXIMO", precision = 18, scale = 2)
    private BigDecimal vrMaximo;

    // Construtor padrão
    public Produto() {}

    public Produto(Integer coProduto, String noProduto, BigDecimal pcTaxaJuros,
               Short nuMinimoMeses, Short nuMaximoMeses,
               BigDecimal vrMinimo, BigDecimal vrMaximo) {
    this.coProduto = coProduto;
    this.noProduto = noProduto;
    this.pcTaxaJuros = pcTaxaJuros;
    this.nuMinimoMeses = nuMinimoMeses;
    this.nuMaximoMeses = nuMaximoMeses;
    this.vrMinimo = vrMinimo;
    this.vrMaximo = vrMaximo;
}

    // Getters e Setters
    public Integer getCoProduto() {
        return coProduto;
    }

    public void setCoProduto(Integer coProduto) {
        this.coProduto = coProduto;
    }

    public String getNoProduto() {
        return noProduto;
    }

    public void setNoProduto(String noProduto) {
        this.noProduto = noProduto;
    }

    public BigDecimal getPcTaxaJuros() {
        return pcTaxaJuros;
    }

    public void setPcTaxaJuros(BigDecimal pcTaxaJuros) {
        this.pcTaxaJuros = pcTaxaJuros;
    }

    public Short getNuMinimoMeses() {
        return nuMinimoMeses;
    }

    public void setNuMinimoMeses(Short nuMinimoMeses) {
        this.nuMinimoMeses = nuMinimoMeses;
    }

    public Short getNuMaximoMeses() {
        return nuMaximoMeses;
    }

    public void setNuMaximoMeses(Short nuMaximoMeses) {
        this.nuMaximoMeses = nuMaximoMeses;
    }

    public BigDecimal getVrMinimo() {
        return vrMinimo;
    }

    public void setVrMinimo(BigDecimal vrMinimo) {
        this.vrMinimo = vrMinimo;
    }

    public BigDecimal getVrMaximo() {
        return vrMaximo;
    }

    public void setVrMaximo(BigDecimal vrMaximo) {
        this.vrMaximo = vrMaximo;
    }
}



