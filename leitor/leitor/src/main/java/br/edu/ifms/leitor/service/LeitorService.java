package br.edu.ifms.leitor.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.ifms.leitor.entity.Leitor;
import br.edu.ifms.leitor.repository.LeitorRepository;

@Service
public class LeitorService {

    private final LeitorRepository leitorRepository;

    public LeitorService(LeitorRepository leitorRepository) {
        this.leitorRepository = leitorRepository;
    }

    public Leitor cadastrar(Leitor leitor) {
        return leitorRepository.save(leitor);
    }

    public List<Leitor> buscarTodos() {
        return leitorRepository.findAll();
    }

    public Leitor buscarPorId(Long id) {
        return leitorRepository.findById(id).orElse(null);
    }

    public Leitor atualizar(Long id, Leitor leitor) {
        Leitor leitorExistente = buscarPorId(id);

        if (leitorExistente == null) {
            return null;
        }

        leitorExistente.setNome(leitor.getNome());
        leitorExistente.setEmail(leitor.getEmail());

        return leitorRepository.save(leitorExistente);
    }

    public boolean excluir(Long id) {
        if (!leitorRepository.existsById(id)) {
            return false;
        }

        leitorRepository.deleteById(id);
        return true;
    }
}