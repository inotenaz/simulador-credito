package com.hackathon2025.simulador_credito.service;

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.SimulacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SimulacaoService {

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    public Map<String, Object> resumoSimulacoes(String data, Integer codigoProduto) {

        try {
            LocalDate dataReferencia = LocalDate.parse(data);

            // Buscar simulações pelo código do produto e data da simulação (mesmo dia)
            List<Simulacao> simulacoes = simulacaoRepository.findAll().stream()
                    .filter(s -> s.getCodigoProduto().equals(codigoProduto)
                            && s.getDataSimulacao().toLocalDate().equals(dataReferencia))
                    .toList();

            if (simulacoes.isEmpty()) {
                return Map.of(
                        "dataReferencia", data,
                        "simulacoes", List.of());
            }

            // Pega a descricaoProduto de qualquer registro
            String descricaoProduto = simulacoes.get(0).getDescricaoProduto();

            // Calcula valores agregados
            BigDecimal taxaMediaJuro = simulacoes.stream()
                    .map(Simulacao::getTaxaJuros)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(simulacoes.size()), 4, RoundingMode.HALF_UP);

            BigDecimal valorMedioPrestacao = simulacoes.stream()
                    .map(Simulacao::getValorMedioPrestacao)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(simulacoes.size()), 2, RoundingMode.HALF_UP);

            BigDecimal valorTotalDesejado = simulacoes.stream()
                    .map(Simulacao::getValorDesejado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal valorTotalCredito = simulacoes.stream()
                    .map(Simulacao::getValorTotalParcelas)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Monta objeto do JSON
            Map<String, Object> resumo = new LinkedHashMap<>();
            resumo.put("codigoProduto", codigoProduto);
            resumo.put("descricaoProduto", descricaoProduto);
            resumo.put("taxaMediaJuro", taxaMediaJuro);
            resumo.put("valorMedioPrestacao", valorMedioPrestacao);
            resumo.put("valorTotalDesejado", valorTotalDesejado);
            resumo.put("valorTotalCredito", valorTotalCredito);

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("dataReferencia", data);
            resposta.put("simulacoes", List.of(resumo));

            return resposta;

        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> listarSimulacoes(int pagina, int tamanhoPagina) {

        Pageable pageable = PageRequest.of(pagina - 1, tamanhoPagina, Sort.by("idSimulacao").ascending());

        Page<Simulacao> page = simulacaoRepository.findAll(pageable);

        // Montar lista de registros com os campos desejados
        List<Map<String, Object>> registros = page.getContent().stream().map(sim -> {
            Map<String, Object> registro = new LinkedHashMap<>();
            registro.put("idSimulacao", sim.getIdSimulacao());
            registro.put("valorDesejado", sim.getValorDesejado());
            registro.put("prazo", sim.getPrazo());
            registro.put("valorTotalParcelas", sim.getValorTotalParcelas());
            return registro;
        }).toList();

        // Montar resposta
        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("pagina", pagina);
        resposta.put("qtdRegistros", page.getTotalElements());
        resposta.put("qtdRegistrosPagina", tamanhoPagina);
        resposta.put("registros", registros);

        return resposta;
    }

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