package com.hackathon2025.simulador_credito.repository;

import com.hackathon2025.simulador_credito.model.Acesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AcessoRepository extends JpaRepository<Acesso, Long> {
    List<Acesso> findByUsuario(String usuario);
    List<Acesso> findByDataAcessoBetween(LocalDateTime inicio, LocalDateTime fim);
}