package br.edu.ifms.leitor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifms.leitor.entity.Leitor;
import br.edu.ifms.leitor.service.LeitorService;

@RestController
@RequestMapping("/leitores")
public class LeitorController {

    private final LeitorService leitorService;

    public LeitorController(LeitorService leitorService) {
        this.leitorService = leitorService;
    }

    @PostMapping
    public ResponseEntity<Leitor> cadastrar(@RequestBody Leitor leitor) {
        Leitor leitorSalvo = leitorService.cadastrar(leitor);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(leitorSalvo);
    }

    @GetMapping
    public List<Leitor> buscarTodos() {
        return leitorService.buscarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Leitor> buscarPorId(@PathVariable Long id) {
        Leitor leitor = leitorService.buscarPorId(id);

        if (leitor == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(leitor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Leitor> atualizar(
            @PathVariable Long id,
            @RequestBody Leitor leitor) {

        Leitor leitorAtualizado = leitorService.atualizar(id, leitor);

        if (leitorAtualizado == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(leitorAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        boolean excluido = leitorService.excluir(id);

        if (!excluido) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}