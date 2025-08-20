package com.hackathon2025.simulador_credito.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Repository
public class ProdutoRepository {

    private final JdbcTemplate jdbcTemplate;

    // Construtor que injeta o DataSource configurado
    public ProdutoRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    // Método para buscar todos os produtos
    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("SELECT * FROM PRODUTO"); 
    }

}


