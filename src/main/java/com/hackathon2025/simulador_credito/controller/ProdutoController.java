package com.hackathon2025.simulador_credito.controller;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon2025.simulador_credito.model.Produto;
import com.hackathon2025.simulador_credito.repository.ProdutoRepository;


@RestController
@RequestMapping("/produtos-credito")
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping
    public List<Produto> listar() {
        return produtoRepository.findAll();
    }
}

