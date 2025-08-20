package com.hackathon2025.simulador_credito.service;

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.SimulacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SimulacaoService {

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    public Simulacao registrarSimulacao(Simulacao simulacao) {
        simulacao.setDataSimulacao(LocalDateTime.now());
        return simulacaoRepository.save(simulacao);
    }

    public List<Simulacao> buscarTodos() {
        return simulacaoRepository.findAll();
    }

    public Simulacao buscarPorId(Long id) {
        return simulacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Simulação não encontrada"));
    }

    public void deletar(Long id) {
        simulacaoRepository.deleteById(id);
    }
}