package com.hackathon2025.simulador_credito.service;

import com.hackathon2025.simulador_credito.model.Acesso;
import com.hackathon2025.simulador_credito.repository.AcessoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AcessoService {

    @Autowired
    private AcessoRepository acessoRepository;

    public Acesso registrarAcesso(Acesso acesso) {
        acesso.setDataAcesso(LocalDateTime.now());
        return acessoRepository.save(acesso);
    }

    public List<Acesso> buscarTodos() {
        return acessoRepository.findAll();
    }

    public List<Acesso> buscarPorUsuario(String usuario) {
        return acessoRepository.findByUsuario(usuario);
    }

    public Acesso buscarPorId(Long id) {
        return acessoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acesso não encontrado"));
    }

    public void deletar(Long id) {
        acessoRepository.deleteById(id);
    }
}