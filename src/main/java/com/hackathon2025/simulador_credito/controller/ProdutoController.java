package com.hackathon2025.simulador_credito.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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
}
