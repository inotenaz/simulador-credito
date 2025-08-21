package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.service.SimulacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

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

    @Operation(summary = "Simula crédito", description = "Recebe valor desejado e prazo e retorna o resultado da simulação.", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Payload contendo valorDesejado e prazo", required = true, content = @Content(mediaType = "application/json", schema = @Schema(example = "{ \"valorDesejado\": 1000.00, \"prazo\": 12 }"))))
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

    @Operation(summary = "Listar simulações", description = "Retorna uma lista paginada de simulações realizadas.")
    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> listarSimulacoes(
            @Parameter(description = "Número da página") @RequestParam(defaultValue = "1") int pagina,
            @Parameter(description = "Quantidade de registros por página") @RequestParam(defaultValue = "200") int tamanho_pagina) {

        Map<String, Object> resultado = simulacaoService.listarSimulacoes(pagina, tamanho_pagina);

        if ("Erro".equals(resultado.get("status"))) {
            // Retorna 500 Internal Server Error se houve problema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultado);
        } else {
            // Retorna 200 OK normalmente
            return ResponseEntity.ok(resultado);
        }
    }

    @Operation(summary = "Resumo das simulações", description = "Retorna o resumo das simulações realizadas para uma data e código de produto específicos.")
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumoSimulacoes(
            @Parameter(description = "Data no formato yyyy-MM-dd", example = "2025-08-21") @RequestParam String data,
            @Parameter(description = "Código do produto", example = "123") @RequestParam Integer codigoProduto) {

        Map<String, Object> resultado = simulacaoService.resumoSimulacoes(data, codigoProduto);

        if (resultado == null) {
            // Retorna 500 Internal Server Error se houve problema
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "Erro", "mensagem", "Ocorreu um erro ao obter o resumo das simulações."));
        }
        return ResponseEntity.ok(resultado);
    }

}