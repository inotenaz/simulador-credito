package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.service.SimulacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/simulacoes")
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

    @GetMapping("/total")
    public ResponseEntity<Map<String, Object>> listarSimulacoes(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "200") int tamanho_pagina) {

        return ResponseEntity.ok(simulacaoService.listarSimulacoes(pagina, tamanho_pagina));
    }

    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumoSimulacoes(
            @RequestParam String data, // formato "yyyy-MM-dd"
            @RequestParam Integer codigoProduto) {

        return ResponseEntity.ok(simulacaoService.resumoSimulacoes(data, codigoProduto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        simulacaoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

}