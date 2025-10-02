package com.EderProject.cadastro_usuario.controller;

import com.EderProject.cadastro_usuario.business.LivroService;
import com.EderProject.cadastro_usuario.infrastructure.entitys.Livro;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/livros")
@RequiredArgsConstructor
public class LivroController {

    private final LivroService livroService;

    @PostMapping
    public ResponseEntity<Livro> salvarLivro(@RequestBody Livro livro) {
        Livro livroSalvo = livroService.salvarLivro(livro);
        return ResponseEntity.ok(livroSalvo);
    }

    @GetMapping
    public ResponseEntity<List<Livro>> buscarTodosLivros() {
        List<Livro> livros = livroService.buscarTodosLivros();
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livro> buscarLivroPorId(@PathVariable Integer id) {
        Optional<Livro> livro = livroService.buscarLivroPorId(id);
        return livro.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<Livro> buscarLivroPorIsbn(@PathVariable String isbn) {
        Optional<Livro> livro = livroService.buscarLivroPorIsbn(isbn);
        return livro.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/titulo")
    public ResponseEntity<List<Livro>> buscarLivrosPorTitulo(@RequestParam String titulo) {
        List<Livro> livros = livroService.buscarLivrosPorTitulo(titulo);
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/autor")
    public ResponseEntity<List<Livro>> buscarLivrosPorAutor(@RequestParam String autor) {
        List<Livro> livros = livroService.buscarLivrosPorAutor(autor);
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/disponiveis")
    public ResponseEntity<List<Livro>> buscarLivrosDisponiveis() {
        List<Livro> livros = livroService.buscarLivrosDisponiveis();
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/indisponiveis")
    public ResponseEntity<List<Livro>> buscarLivrosIndisponiveis() {
        List<Livro> livros = livroService.buscarLivrosIndisponiveis();
        return ResponseEntity.ok(livros);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Livro>> buscarLivrosPorTermo(@RequestParam String termo) {
        List<Livro> livros = livroService.buscarLivrosPorTermo(termo);
        return ResponseEntity.ok(livros);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livro> atualizarLivro(@PathVariable Integer id, @RequestBody Livro livro) {
        Livro livroAtualizado = livroService.atualizarLivro(id, livro);
        return ResponseEntity.ok(livroAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarLivro(@PathVariable Integer id) {
        livroService.deletarLivro(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/marcar-indisponivel")
    public ResponseEntity<Void> marcarComoIndisponivel(@PathVariable Integer id) {
        livroService.marcarComoIndisponivel(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/marcar-disponivel")
    public ResponseEntity<Void> marcarComoDisponivel(@PathVariable Integer id) {
        livroService.marcarComoDisponivel(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contar/disponiveis")
    public ResponseEntity<Long> contarLivrosDisponiveis() {
        Long count = livroService.contarLivrosDisponiveis();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/buscar-por-isbn/{isbn}")
    public ResponseEntity<Livro> buscarInformacoesLivroPorIsbn(@PathVariable String isbn) {
        Livro livro = livroService.buscarInformacoesLivroPorIsbn(isbn);
        return ResponseEntity.ok(livro);
    }
    
    @PutMapping("/{id}/buscar-informacoes")
    public ResponseEntity<Livro> atualizarLivroComInformacoesApi(@PathVariable Integer id) {
        Livro livro = livroService.atualizarLivroComInformacoesApi(id);
        return ResponseEntity.ok(livro);
    }
}
