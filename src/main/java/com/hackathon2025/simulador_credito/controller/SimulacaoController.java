package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.service.SimulacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SimulacaoController {

    @Autowired
    private SimulacaoService simulacaoService;

    @PostMapping
    public ResponseEntity<Simulacao> criar(@RequestBody Simulacao simulacao) {
        Simulacao novaSimulacao = simulacaoService.registrarSimulacao(simulacao);
        return ResponseEntity.ok(novaSimulacao);
    }

    @GetMapping
    public ResponseEntity<List<Simulacao>> listarTodos() {
        return ResponseEntity.ok(simulacaoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Simulacao> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(simulacaoService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        simulacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
@GetMapping("/cadastrargeral")
public ResponseEntity<List<Simulacao>> cadastrarExemplos() {
    List<Simulacao> simulacoesSalvas = new ArrayList<>();

    // Exemplo 1
    Simulacao simulacao1 = new Simulacao();
    simulacao1.setValorDesejado(new BigDecimal("10000"));
    simulacao1.setPrazo(12);
    simulacao1.setCodigoProduto(1);
    simulacao1.setDescricaoProduto("Crédito Pessoal");
    simulacao1.setTaxaJuros(new BigDecimal("1.5"));
    simulacao1.setValorMedioPrestacao(new BigDecimal("900"));
    simulacao1.setValorTotalParcelasPRICE(new BigDecimal("10800"));
    simulacao1.setValorTotalParcelasSAC(new BigDecimal("11000"));
    simulacoesSalvas.add(simulacaoService.registrarSimulacao(simulacao1));

    // Exemplo 2
    Simulacao simulacao2 = new Simulacao();
    simulacao2.setValorDesejado(new BigDecimal("20000"));
    simulacao2.setPrazo(24);
    simulacao2.setCodigoProduto(2);
    simulacao2.setDescricaoProduto("Crédito Veicular");
    simulacao2.setTaxaJuros(new BigDecimal("1.8"));
    simulacao2.setValorMedioPrestacao(new BigDecimal("950"));
    simulacao2.setValorTotalParcelasPRICE(new BigDecimal("22800"));
    simulacao2.setValorTotalParcelasSAC(new BigDecimal("23200"));
    simulacoesSalvas.add(simulacaoService.registrarSimulacao(simulacao2));

    // Exemplo 3
    Simulacao simulacao3 = new Simulacao();
    simulacao3.setValorDesejado(new BigDecimal("30000"));
    simulacao3.setPrazo(36);
    simulacao3.setCodigoProduto(3);
    simulacao3.setDescricaoProduto("Crédito Imobiliário");
    simulacao3.setTaxaJuros(new BigDecimal("2.0"));
    simulacao3.setValorMedioPrestacao(new BigDecimal("1100"));
    simulacao3.setValorTotalParcelasPRICE(new BigDecimal("39600"));
    simulacao3.setValorTotalParcelasSAC(new BigDecimal("40500"));
    simulacoesSalvas.add(simulacaoService.registrarSimulacao(simulacao3));

    return ResponseEntity.ok(simulacoesSalvas);
}


}