package br.edu.ifms.emprestimo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.ifms.emprestimo.entity.Emprestimo;
import br.edu.ifms.emprestimo.service.EmprestimoService;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService service;

    public EmprestimoController(EmprestimoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Emprestimo> cadastrar(@RequestBody Emprestimo emprestimo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(emprestimo));
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> buscarTodos() {
        return ResponseEntity.ok(service.buscarTodos());
    }
}
