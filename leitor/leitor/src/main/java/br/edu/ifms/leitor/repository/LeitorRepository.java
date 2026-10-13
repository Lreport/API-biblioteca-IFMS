package br.edu.ifms.leitor.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifms.leitor.entity.Leitor;


public interface LeitorRepository extends JpaRepository<Leitor, Long> {
    
}