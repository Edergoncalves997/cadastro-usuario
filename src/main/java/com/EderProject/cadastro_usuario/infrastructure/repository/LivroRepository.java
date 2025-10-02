package com.EderProject.cadastro_usuario.infrastructure.repository;

import com.EderProject.cadastro_usuario.infrastructure.entitys.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LivroRepository extends JpaRepository<Livro, Integer> {
    
    Optional<Livro> findByIsbn(String isbn);
    
    List<Livro> findByTituloContainingIgnoreCase(String titulo);
    
    List<Livro> findByAutorContainingIgnoreCase(String autor);
    
    List<Livro> findByDisponivel(Boolean disponivel);
    
    @Query("SELECT l FROM Livro l WHERE l.titulo LIKE %:termo% OR l.autor LIKE %:termo% OR l.isbn LIKE %:termo%")
    List<Livro> buscarPorTermo(@Param("termo") String termo);
    
    @Query("SELECT COUNT(l) FROM Livro l WHERE l.disponivel = true")
    Long countLivrosDisponiveis();
}
