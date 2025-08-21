package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.service.SimulacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/simulacoes")
public class SimulacaoController {

    @Autowired
    private SimulacaoService simulacaoService;

    @PostMapping("/simular")
    public ResponseEntity<Object> simularCredito(@RequestBody Map<String, Object> payload) {
        try {
            Object valorDesejadoObj = payload.get("valorDesejado");
            Object prazoObj = payload.get("prazo");

            BigDecimal valorDesejado = new BigDecimal(valorDesejadoObj.toString());
            Integer prazo = ((Number) prazoObj).intValue();

            Object resultado = simulacaoService.realizaCalculo(valorDesejado, prazo);

            if (resultado instanceof Map) {
                Map<String, Object> resultMap = (Map<String, Object>) resultado;
                if ("Não elegível".equals(resultMap.get("status"))) {
                    return ResponseEntity.status(422).body(resultMap); // 422 Unprocessable Entity
                } else if ("Erro".equals(resultMap.get("status"))) {
                    return ResponseEntity.status(500).body(resultMap); // Internal Server Error
                }
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Formato inválido para Valor Desejado ou Prazo. Verifique se são números."));
        }
    }

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> listarSimulacoes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "200") int tamanho_pagina) {

        Map<String, Object> resultado = simulacaoService.listarSimulacoes(pagina, tamanho_pagina);

        if ("Erro".equals(resultado.get("status"))) {
            // Retorna 500 Internal Server Error se houve problema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
        } else {
            // Retorna 200 OK normalmente
            return ResponseEntity.ok(resultado);
        }
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumoSimulacoes(
            @RequestParam String data, // formato "yyyy-MM-dd"
            @RequestParam Integer codigoProduto) {

        Map<String, Object> resultado = simulacaoService.resumoSimulacoes(data, codigoProduto);

        if (resultado == null) {
            // Retorna 500 Internal Server Error se houve problema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("status", "Erro", "mensagem", "Ocorreu um erro ao obter o resumo das simulações."));
        }
        return ResponseEntity.ok(resultado);
    }

}