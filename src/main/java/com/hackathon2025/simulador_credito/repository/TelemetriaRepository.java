package com.hackathon2025.simulador_credito.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.hackathon2025.simulador_credito.model.Telemetria;

public interface TelemetriaRepository extends JpaRepository<Telemetria, Long> {
    List<Telemetria> findByDataReferencia(LocalDate dataReferencia);
}

