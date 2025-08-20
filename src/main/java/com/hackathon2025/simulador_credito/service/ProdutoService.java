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

import com.hackathon2025.simulador_credito.repository.ProdutoRepository;

@Service
public class ProdutoService {
 
    @Autowired
    private ProdutoRepository produtoRepository;

    public List<Map<String, Object>> listarTodos() {
        return produtoRepository.findAll();
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

                // Monta o objeto de resposta final com todos os detalhes da simulação
                return Map.of(
                    "status", "Cálculo realizado com sucesso",
                    "valorSolicitado", valorDesejado,
                    "prazo", prazo,
                    "produtoSelecionado", Map.of(
                        "codigo", codigoProduto,
                        "nome", nomeProduto,
                        "taxaJuros", taxaJuros
                    ),
                    "resultadoSimulacao", List.of(
                        Map.of(
                            "tipo", "SAC",
                            "parcelas", prestacoes
                        ),
                        Map.of(
                            "tipo", "PRICE",
                            "parcelas", prestacoesPRICE
                        )
                    )
                );
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
