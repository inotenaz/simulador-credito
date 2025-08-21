package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.SimulacaoRepository;
import com.hackathon2025.simulador_credito.service.SimulacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {

    @Autowired
    private SimulacaoService simulacaoService;

    @Autowired
    private SimulacaoRepository simulacaoRepository;

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

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> listarSimulacoes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "200") int tamanho_pagina) {

        Pageable pageable = PageRequest.of(pagina - 1, tamanho_pagina, Sort.by("idSimulacao").ascending());

        Page<Simulacao> page = simulacaoRepository.findAll(pageable);

        // Montar lista de registros com os campos desejados
        List<Map<String, Object>> registros = page.getContent().stream().map(sim -> {
            Map<String, Object> registro = new LinkedHashMap<>();
            registro.put("idSimulacao", sim.getIdSimulacao());
            registro.put("valorDesejado", sim.getValorDesejado());
            registro.put("prazo", sim.getPrazo());
            registro.put("valorTotalParcelas", sim.getValorTotalParcelas());
            return registro;
        }).toList();

        // Montar resposta
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("pagina", pagina);
        resposta.put("qtdRegistros", page.getTotalElements());
        resposta.put("qtdRegistrosPagina", tamanho_pagina);
        resposta.put("registros", registros);

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumoSimulacoes(
            @RequestParam String data, // formato "yyyy-MM-dd"
            @RequestParam Integer codigoProduto) {

        try {
            LocalDate dataReferencia = LocalDate.parse(data);

            // Buscar simulações pelo código do produto e data da simulação (mesmo dia)
            List<Simulacao> simulacoes = simulacaoRepository.findAll().stream()
                    .filter(s -> s.getCodigoProduto().equals(codigoProduto)
                            && s.getDataSimulacao().toLocalDate().equals(dataReferencia))
                    .toList();

            if (simulacoes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "dataReferencia", data,
                        "simulacoes", List.of()
                ));
            }

            // Pega a descricaoProduto de qualquer registro
            String descricaoProduto = simulacoes.get(0).getDescricaoProduto();

            // Calcula valores agregados
            BigDecimal taxaMediaJuro = simulacoes.stream()
                    .map(Simulacao::getTaxaJuros)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(simulacoes.size()), 4, RoundingMode.HALF_UP);

            BigDecimal valorMedioPrestacao = simulacoes.stream()
                    .map(Simulacao::getValorMedioPrestacao)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(simulacoes.size()), 2, RoundingMode.HALF_UP);

            BigDecimal valorTotalDesejado = simulacoes.stream()
                    .map(Simulacao::getValorDesejado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valorTotalCredito = simulacoes.stream()
                    .map(Simulacao::getValorTotalParcelas)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Monta objeto do JSON
            Map<String, Object> resumo = new LinkedHashMap<>();
            resumo.put("codigoProduto", codigoProduto);
            resumo.put("descricaoProduto", descricaoProduto);
            resumo.put("taxaMediaJuro", taxaMediaJuro);
            resumo.put("valorMedioPrestacao", valorMedioPrestacao);
            resumo.put("valorTotalDesejado", valorTotalDesejado);
            resumo.put("valorTotalCredito", valorTotalCredito);

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("dataReferencia", data);
            resposta.put("simulacoes", List.of(resumo));

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        simulacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}