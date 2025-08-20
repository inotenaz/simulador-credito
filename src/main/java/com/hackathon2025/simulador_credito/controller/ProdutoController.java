package com.hackathon2025.simulador_credito.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon2025.simulador_credito.service.ProdutoService;

@RestController
@RequestMapping("/produtos-credito")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;
    
    /**
     * Retorna uma lista de todos os produtos de crédito disponíveis, com as faixas de valores e numero de prestações.
     * @return Uma lista de mapas, onde cada mapa representa um produto de crédito.
     */
    @GetMapping
    public List<Map<String, Object>> listar() {
        return produtoService.listarTodos();
    }

    /**
     * Realiza uma simulação de crédito com base no valor desejado e no prazo.
     * @param payload O corpo da requisição contendo o valor desejado e o prazo.
     * @return O resultado da simulação.
     */
    @PostMapping("/simular")
    public ResponseEntity<Object> simularCredito(@RequestBody Map<String, Object> payload) {
        try {
            Object valorDesejadoObj = payload.get("valorDesejado");
            Object prazoObj = payload.get("prazo");

            BigDecimal valorDesejado = new BigDecimal(valorDesejadoObj.toString());
            Integer prazo = ((Number) prazoObj).intValue();

            Object resultado = produtoService.realizaCalculo(valorDesejado, prazo);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Formato inválido para Valor Desejado ou Prazo. Verifique se são números."));
        }
    }
}
