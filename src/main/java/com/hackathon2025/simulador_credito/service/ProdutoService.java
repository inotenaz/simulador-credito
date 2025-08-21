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

import com.hackathon2025.simulador_credito.model.Simulacao;
import com.hackathon2025.simulador_credito.repository.ProdutoRepository;
import com.hackathon2025.simulador_credito.repository.SimulacaoRepository;

@Service
public class ProdutoService {
 
    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private SimulacaoRepository simulacaoRepository;


    public List<Map<String, Object>> listarTodos() {
        return produtoRepository.findAll();
    }

    public Long salvarSimulacao(Map<String, Object> dados) {
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

            // Monta a entidade Simulacao
            Simulacao simulacao = new Simulacao();
            simulacao.setCodigoProduto(codigoProduto);
            simulacao.setDescricaoProduto(descricaoProduto);
            simulacao.setTaxaJuros(taxaJuros);
            simulacao.setValorMedioPrestacaoPrice(mediaPRICE);
            simulacao.setValorMedioPrestacaoSAC(mediaSAC);

            // Se quiser salvar as médias no banco, adicione colunas na tabela e no model
            // simulacao.setMediaSac(mediaSAC);
            // simulacao.setMediaPrice(mediaPRICE);

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
                    BigDecimal vrMaximo = p.get("VR_MAXIMO") != null ? new BigDecimal(p.get("VR_MAXIMO").toString()) : null;
                    Integer nuMinimoMeses = ((Number) p.get("NU_MINIMO_MESES")).intValue();
                    Integer nuMaximoMeses = p.get("NU_MAXIMO_MESES") != null ? ((Number) p.get("NU_MAXIMO_MESES")).intValue() : null;

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
                    // Na última parcela, a amortização é o saldo devedor restante para garantir que zere.
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
                BigDecimal valorPrestacaoPRICE = valorDesejado.multiply(BigDecimal.valueOf((jurosDouble * fator) / (fator - 1)))
                                                            .setScale(2, RoundingMode.HALF_UP);

                BigDecimal saldoDevedorPrice = valorDesejado;

                for (int i = 1; i <= prazo; i++) {
                    BigDecimal jurosDaParcela = saldoDevedorPrice.multiply(taxaJuros).setScale(2, RoundingMode.HALF_UP);
                    BigDecimal amortizacaoDaParcela = valorPrestacaoPRICE.subtract(jurosDaParcela).setScale(2, RoundingMode.HALF_UP);

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

                Long idSimulacao = salvarSimulacao(resposta);

                Map<String, Object> novaResposta = new LinkedHashMap<>();
                novaResposta.put("idSimulacao", idSimulacao);
                novaResposta.putAll(resposta); 

                resposta = novaResposta;

                return resposta;

            } else {
                // Nenhum produto compatível foi encontrado
                return Map.of("status", "Não elegível",
                              "mensagem", "Nenhum produto de crédito encontrado para o valor e prazo solicitados.");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Idealmente, usar um logger como SLF4J
            return Map.of("status", "Erro", "mensagem", "Ocorreu um erro inesperado ao processar sua solicitação.");
        }
    }
}
