package br.edu.ifms.emprestimo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifms.emprestimo.entity.Emprestimo;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {
}
