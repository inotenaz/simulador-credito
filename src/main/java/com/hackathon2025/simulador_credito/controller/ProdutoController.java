package com.hackathon2025.simulador_credito.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon2025.simulador_credito.service.ProdutoService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/produtos-credito")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Operation(summary = "Lista todos os produtos de crédito disponíveis", description = "Retorna uma lista de produtos de crédito com suas faixas de valores e número de prestações.")
    @GetMapping
    public List<Map<String, Object>> listar() {
        return produtoService.listarTodos();
    }
}
