package com.hackathon2025.simulador_credito.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "acessos")
public class Acesso {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;
    
    @Column(name = "usuario", length = 100, nullable = false)
    private String usuario;
    
    @Column(name = "tipo_acesso", length = 50, nullable = false)
    private String tipoAcesso;
    
    @Column(name = "data_acesso")
    private LocalDateTime dataAcesso;
    
    @Column(name = "descricao", length = 500)
    private String descricao;
    
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        dataAcesso = LocalDateTime.now();
    }
}