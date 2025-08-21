package com.hackathon2025.simulador_credito.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hackathon2025.simulador_credito.model.Telemetria;
import com.hackathon2025.simulador_credito.repository.TelemetriaRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@RestController
@RequestMapping("/telemetria")
public class TelemetriaController {

    @Autowired
    private TelemetriaRepository telemetriaRepository;

    @Operation(summary = "Resumo de telemetria por data", description = "Retorna estatísticas de execução por endpoint para uma data específica (ex.: número de requisições, tempos e percentual de sucesso).")
    @GetMapping("/resumo")
    public ResponseEntity<Map<String, Object>> resumoTelemetria(
            @Parameter(description = "Data no formato yyyy-MM-dd", required = true, example = "2025-08-21") @RequestParam String data) {

        LocalDate dataReferencia = LocalDate.parse(data);

        List<Telemetria> registros = telemetriaRepository.findByDataReferencia(dataReferencia);

        // Agrupa por endpoint
        Map<String, List<Telemetria>> agrupado = registros.stream()
                .collect(Collectors.groupingBy(Telemetria::getNomeApi));

        // Monta a lista com métricas
        List<Map<String, Object>> listaEndpoints = new ArrayList<>();
        for (String endpoint : agrupado.keySet()) {
            List<Telemetria> lista = agrupado.get(endpoint);

            long tempoMin = lista.stream().mapToLong(Telemetria::getTempoExecucao).min().orElse(0);
            long tempoMax = lista.stream().mapToLong(Telemetria::getTempoExecucao).max().orElse(0);
            double tempoMedio = lista.stream().mapToLong(Telemetria::getTempoExecucao).average().orElse(0);

            long qtdRequisicoes = lista.size();
            long sucesso = lista.stream().filter(t -> t.getStatusHttp() == 200).count();
            double percentualSucesso = qtdRequisicoes > 0 ? (double) sucesso / qtdRequisicoes * 100 : 0.0;

            percentualSucesso = Math.round(percentualSucesso) / 100.0;

            Map<String, Object> endpointResumo = new LinkedHashMap<>();
            endpointResumo.put("nomeApi", endpoint);
            endpointResumo.put("qtdRequisicoes", qtdRequisicoes);
            endpointResumo.put("tempoMedio", (long) tempoMedio);
            endpointResumo.put("tempoMinimo", tempoMin);
            endpointResumo.put("tempoMaximo", tempoMax);
            endpointResumo.put("percentualSucesso", percentualSucesso);

            listaEndpoints.add(endpointResumo);
        }

        // Resposta final
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("dataReferencia", data);
        resposta.put("listaEndpoints", listaEndpoints);

        return ResponseEntity.ok(resposta);
    }

}
