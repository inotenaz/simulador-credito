package com.hackathon2025.simulador_credito.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.ProdutoRepository;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SimulacaoService {

    @Autowired
    private SimulacaoRepository simulacaoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EventHubService eventHubService;

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

        try {

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

        } catch (Exception e) {
            return Map.of("status", "Erro", "mensagem", "Ocorreu um erro ao listar as simulações.");
        }
    }

    public Long salvarSimulacao(Map<String, Object> dados, BigDecimal valorDesejado, Integer prazo) {
        try {
            // Extrair campos principais do Map
            Integer codigoProduto = (Integer) dados.get("codigoProduto");
            String descricaoProduto = (String) dados.get("descricaoProduto");
            BigDecimal taxaJuros = new BigDecimal(dados.get("taxaJuros").toString());

            // Extrair lista resultadoSimulacao
            List<Map<String, Object>> resultadoSimulacao = (List<Map<String, Object>>) dados.get("resultadoSimulacao");
            BigDecimal mediaSAC = BigDecimal.ZERO;
            BigDecimal mediaPRICE = BigDecimal.ZERO;

            for (Map<String, Object> simulacaoTipo : resultadoSimulacao) {
                String tipo = (String) simulacaoTipo.get("tipo");
                List<Map<String, Object>> parcelas = (List<Map<String, Object>>) simulacaoTipo.get("parcelas");

                BigDecimal soma = BigDecimal.ZERO;
                for (Map<String, Object> parcela : parcelas) {
                    BigDecimal valorPrestacao = new BigDecimal(parcela.get("valorPrestacao").toString());
                    soma = soma.add(valorPrestacao);
                }

                BigDecimal media = soma.divide(new BigDecimal(parcelas.size()), 2, RoundingMode.HALF_UP);

                if ("SAC".equalsIgnoreCase(tipo)) {
                    mediaSAC = media;
                } else if ("PRICE".equalsIgnoreCase(tipo)) {
                    mediaPRICE = media;
                }
            }
            // Calcula a média total
            BigDecimal mediaTotal = mediaPRICE.add(mediaSAC).divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
            // Calcula o valor total das parcelas
            BigDecimal valorTotalParcelas = mediaTotal.multiply(new BigDecimal(prazo)).setScale(2,
                    RoundingMode.HALF_UP);
            // Monta a entidade Simulacao
            Simulacao simulacao = new Simulacao();
            simulacao.setCodigoProduto(codigoProduto);
            simulacao.setDescricaoProduto(descricaoProduto);
            simulacao.setTaxaJuros(taxaJuros);
            simulacao.setValorMedioPrestacao(mediaTotal);
            simulacao.setValorDesejado(valorDesejado);
            simulacao.setPrazo(prazo);
            simulacao.setValorTotalParcelas(valorTotalParcelas);

            simulacaoRepository.save(simulacao);
            return simulacao.getIdSimulacao();

        } catch (Exception e) {
            return null;
        }
    }

    public Object realizaCalculo(BigDecimal valorDesejado, Integer prazo) {
        try {
            List<Map<String, Object>> listaProdutos = produtoRepository.findAll();

            // Procura por um produto que se encaixe nos critérios de valor e prazo
            Optional<Map<String, Object>> produtoCompativelOpt = listaProdutos.stream()
                    .filter(p -> {
                        // Extrai e converte os valores do mapa, tratando possíveis nulos
                        BigDecimal vrMinimo = new BigDecimal(p.get("VR_MINIMO").toString());
                        BigDecimal vrMaximo = p.get("VR_MAXIMO") != null ? new BigDecimal(p.get("VR_MAXIMO").toString())
                                : null;
                        Integer nuMinimoMeses = ((Number) p.get("NU_MINIMO_MESES")).intValue();
                        Integer nuMaximoMeses = p.get("NU_MAXIMO_MESES") != null
                                ? ((Number) p.get("NU_MAXIMO_MESES")).intValue()
                                : null;

                        // Verifica se o valor desejado está na faixa do produto
                        boolean valorOk = valorDesejado.compareTo(vrMinimo) >= 0 &&
                                (vrMaximo == null || valorDesejado.compareTo(vrMaximo) <= 0);

                        // Verifica se o prazo está na faixa do produto
                        boolean prazoOk = prazo >= nuMinimoMeses &&
                                (nuMaximoMeses == null || prazo <= nuMaximoMeses);

                        return valorOk && prazoOk;
                    })
                    .findFirst();

            if (produtoCompativelOpt.isPresent()) {
                Map<String, Object> produto = produtoCompativelOpt.get();

                // Extrai os valores do produto encontrado para variáveis locais
                Integer codigoProduto = ((Number) produto.get("CO_PRODUTO")).intValue();
                String nomeProduto = (String) produto.get("NO_PRODUTO");
                BigDecimal taxaJuros = new BigDecimal(produto.get("PC_TAXA_JUROS").toString());

                // --- Início do Cálculo SAC ---
                List<Map<String, Object>> prestacoes = new ArrayList<>();

                // Calcula a amortização constante, base para as parcelas
                BigDecimal valorAmortizacao = valorDesejado.divide(new BigDecimal(prazo), 2, RoundingMode.HALF_UP);
                BigDecimal saldoDevedor = valorDesejado;

                for (int i = 1; i <= prazo; i++) {
                    // Juros são calculados sobre o saldo devedor do período anterior
                    BigDecimal jurosDaParcela = saldoDevedor.multiply(taxaJuros).setScale(2, RoundingMode.HALF_UP);

                    BigDecimal amortizacaoDaParcela;
                    // Na última parcela, a amortização é o saldo devedor restante para garantir que
                    // zere.
                    // Isso corrige qualquer diferença de arredondamento.
                    if (i == prazo) {
                        amortizacaoDaParcela = saldoDevedor;
                    } else {
                        amortizacaoDaParcela = valorAmortizacao;
                    }

                    BigDecimal valorDaParcela = amortizacaoDaParcela.add(jurosDaParcela);

                    Map<String, Object> parcela = new LinkedHashMap<>();
                    parcela.put("numero", i);
                    parcela.put("valorAmortizacao", amortizacaoDaParcela.setScale(2, RoundingMode.HALF_UP));
                    parcela.put("valorJuros", jurosDaParcela); // Já está com 2 casas decimais
                    parcela.put("valorPrestacao", valorDaParcela.setScale(2, RoundingMode.HALF_UP));
                    prestacoes.add(parcela);

                    // Atualiza o saldo devedor para o próximo cálculo
                    saldoDevedor = saldoDevedor.subtract(amortizacaoDaParcela);
                }

                // --- Início do Cálculo PRICE ---
                double jurosDouble = taxaJuros.doubleValue();
                List<Map<String, Object>> prestacoesPRICE = new ArrayList<>();

                // Fórmula PRICE: P = PV * (i * (1+i)^n) / ((1+i)^n - 1)
                double fator = Math.pow(1 + jurosDouble, prazo);
                BigDecimal valorPrestacaoPRICE = valorDesejado
                        .multiply(BigDecimal.valueOf((jurosDouble * fator) / (fator - 1)))
                        .setScale(2, RoundingMode.HALF_UP);

                BigDecimal saldoDevedorPrice = valorDesejado;

                for (int i = 1; i <= prazo; i++) {
                    BigDecimal jurosDaParcela = saldoDevedorPrice.multiply(taxaJuros).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal amortizacaoDaParcela = valorPrestacaoPRICE.subtract(jurosDaParcela).setScale(2,
                            RoundingMode.HALF_UP);

                    Map<String, Object> parcela = new LinkedHashMap<>();
                    parcela.put("numero", i);
                    parcela.put("valorAmortizacao", amortizacaoDaParcela);
                    parcela.put("valorJuros", jurosDaParcela);
                    parcela.put("valorPrestacao", valorPrestacaoPRICE);
                    prestacoesPRICE.add(parcela);

                    saldoDevedorPrice = saldoDevedorPrice.subtract(amortizacaoDaParcela);
                }

                Map<String, Object> resposta = new LinkedHashMap<>();
                resposta.put("codigoProduto", codigoProduto);
                resposta.put("descricaoProduto", nomeProduto);
                resposta.put("taxaJuros", taxaJuros.setScale(4, RoundingMode.HALF_UP));

                // Criando os mapas internos também com LinkedHashMap
                Map<String, Object> simulacaoSAC = new LinkedHashMap<>();
                simulacaoSAC.put("tipo", "SAC");
                simulacaoSAC.put("parcelas", prestacoes);

                Map<String, Object> simulacaoPRICE = new LinkedHashMap<>();
                simulacaoPRICE.put("tipo", "PRICE");
                simulacaoPRICE.put("parcelas", prestacoesPRICE);

                // Lista com os dois tipos de simulação
                List<Map<String, Object>> resultadoSimulacao = new ArrayList<>();
                resultadoSimulacao.add(simulacaoSAC);
                resultadoSimulacao.add(simulacaoPRICE);

                resposta.put("resultadoSimulacao", resultadoSimulacao);

                Long idSimulacao = salvarSimulacao(resposta, valorDesejado, prazo);

                Map<String, Object> novaResposta = new LinkedHashMap<>();
                novaResposta.put("idSimulacao", idSimulacao);
                novaResposta.putAll(resposta);
                resposta = novaResposta;

                // Converte o Map para JSON
                ObjectMapper mapper = new ObjectMapper();
                String jsonResposta = mapper.writeValueAsString(resposta);

                // Envia para Event Hub
                eventHubService.sendMessage(jsonResposta);

                return resposta;

            } else {
                // Nenhum produto compatível foi encontrado
                return Map.of("status", "Não elegível",
                        "mensagem", "Nenhum produto de crédito encontrado para o valor e prazo solicitados.");
            }
        } catch (Exception e) {
            return Map.of("status", "Erro", "mensagem", "Ocorreu um erro inesperado ao processar sua solicitação.");
        }
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