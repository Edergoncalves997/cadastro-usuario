package com.EderProject.cadastro_usuario.infrastructure.repository;

import com.EderProject.cadastro_usuario.infrastructure.entitys.Emprestimo;
import com.EderProject.cadastro_usuario.infrastructure.entitys.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmprestimoRepository extends JpaRepository<Emprestimo, Integer> {
    
    List<Emprestimo> findByLivro(Livro livro);
    
    List<Emprestimo> findByEmailPessoa(String emailPessoa);
    
    List<Emprestimo> findByStatus(Emprestimo.StatusEmprestimo status);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.dataPrevistaDevolucao < :dataAtual AND e.status = 'ATIVO'")
    List<Emprestimo> findEmprestimosAtrasados(@Param("dataAtual") LocalDateTime dataAtual);
    
    @Query("SELECT e FROM Emprestimo e WHERE e.livro.id = :livroId AND e.status = 'ATIVO'")
    Optional<Emprestimo> findEmprestimoAtivoPorLivro(@Param("livroId") Integer livroId);
    
    @Query("SELECT COUNT(e) FROM Emprestimo e WHERE e.status = 'ATIVO'")
    Long countEmprestimosAtivos();
}
