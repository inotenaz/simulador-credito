package com.hackathon2025.simulador_credito.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hackathon2025.simulador_credito.model.Simulacao;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SimulacaoRepository extends JpaRepository<Simulacao, Long> {
    List<Simulacao> findByDataSimulacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}