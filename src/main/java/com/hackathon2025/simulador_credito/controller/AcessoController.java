package com.hackathon2025.simulador_credito.controller;

import com.hackathon2025.simulador_credito.model.Acesso;
import com.hackathon2025.simulador_credito.service.AcessoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AcessoController {

    @Autowired
    private AcessoService acessoService;

    @PostMapping
    public ResponseEntity<Acesso> criar(@RequestBody Acesso acesso) {
        Acesso novoAcesso = acessoService.registrarAcesso(acesso);
        return ResponseEntity.ok(novoAcesso);
    }

    @GetMapping
    public ResponseEntity<List<Acesso>> listarTodos() {
        return ResponseEntity.ok(acessoService.buscarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Acesso> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(acessoService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuario}")
    public ResponseEntity<List<Acesso>> buscarPorUsuario(@PathVariable String usuario) {
        return ResponseEntity.ok(acessoService.buscarPorUsuario(usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        acessoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/cadastrargeral")
    public ResponseEntity<List<Acesso>> cadastrarExemplos() {
        List<Acesso> acessos = new ArrayList<>();

        // Exemplo 1
        Acesso acesso1 = new Acesso();
        acesso1.setUsuario("admin");
        acesso1.setTipoAcesso("ADMINISTRADOR");
        acesso1.setDescricao("Acesso inicial do administrador");

        // Exemplo 2
        Acesso acesso2 = new Acesso();
        acesso2.setUsuario("operator");
        acesso2.setTipoAcesso("OPERADOR");
        acesso2.setDescricao("Acesso padrão de operador do sistema");

        // Exemplo 3
        Acesso acesso3 = new Acesso();
        acesso3.setUsuario("guest");
        acesso3.setTipoAcesso("VISITANTE");
        acesso3.setDescricao("Acesso limitado para visitantes");

        // Salvando cada acesso e adicionando à lista
        acessos.add(acessoService.registrarAcesso(acesso1));
        acessos.add(acessoService.registrarAcesso(acesso2));
        acessos.add(acessoService.registrarAcesso(acesso3));

        return ResponseEntity.ok(acessos);
    }

}