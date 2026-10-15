package br.edu.ifms.emprestimo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifms.emprestimo.entity.Emprestimo;
import br.edu.ifms.emprestimo.repository.EmprestimoRepository;

@Service
public class EmprestimoService {

    private final EmprestimoRepository repository;

    public EmprestimoService(EmprestimoRepository repository) {
        this.repository = repository;
    }

    public Emprestimo cadastrar(Emprestimo emprestimo) {
        // O cadastro sempre gera um novo ID, mesmo se o cliente enviar um.
        emprestimo.setId(null);
        return repository.save(emprestimo);
    }

    public List<Emprestimo> buscarTodos() {
        return repository.findAll();
    }
}
