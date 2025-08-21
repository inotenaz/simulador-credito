package com.hackathon2025.simulador_credito.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.ProdutoRepository;
import com.hackathon2025.simulador_credito.repository.SimulacaoRepository;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Map<String, Object>> listarTodos() {
        return produtoRepository.findAll();
    }

}
