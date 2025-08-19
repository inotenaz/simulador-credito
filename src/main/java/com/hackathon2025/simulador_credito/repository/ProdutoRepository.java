package com.hackathon2025.simulador_credito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.hackathon2025.simulador_credito.model.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Integer> {
    // Aqui você pode adicionar métodos customizados, se necessário
}

