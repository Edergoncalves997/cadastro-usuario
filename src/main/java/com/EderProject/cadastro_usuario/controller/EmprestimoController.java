package com.EderProject.cadastro_usuario.controller;

import com.EderProject.cadastro_usuario.business.EmprestimoService;
import com.EderProject.cadastro_usuario.infrastructure.entitys.Emprestimo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
public class EmprestimoController {

    private final EmprestimoService emprestimoService;

    @PostMapping("/emprestar")
    public ResponseEntity<Emprestimo> realizarEmprestimo(
            @RequestParam Integer livroId,
            @RequestParam String nomePessoa,
            @RequestParam String emailPessoa,
            @RequestParam(required = false) String telefonePessoa,
            @RequestParam(required = false, defaultValue = "7") Integer diasEmprestimo,
            @RequestParam(required = false) String observacoes) {
        
        Emprestimo emprestimo = emprestimoService.realizarEmprestimo(
                livroId, nomePessoa, emailPessoa, telefonePessoa, diasEmprestimo, observacoes);
        return ResponseEntity.ok(emprestimo);
    }

    @PutMapping("/{id}/devolver")
    public ResponseEntity<Emprestimo> devolverLivro(@PathVariable Integer id) {
        Emprestimo emprestimo = emprestimoService.devolverLivro(id);
        return ResponseEntity.ok(emprestimo);
    }

    @PutMapping("/devolver-por-livro/{livroId}")
    public ResponseEntity<Emprestimo> devolverLivroPorIdLivro(@PathVariable Integer livroId) {
        Emprestimo emprestimo = emprestimoService.devolverLivroPorIdLivro(livroId);
        return ResponseEntity.ok(emprestimo);
    }

    @GetMapping
    public ResponseEntity<List<Emprestimo>> buscarTodosEmprestimos() {
        List<Emprestimo> emprestimos = emprestimoService.buscarTodosEmprestimos();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Emprestimo> buscarEmprestimoPorId(@PathVariable Integer id) {
        Optional<Emprestimo> emprestimo = emprestimoService.buscarEmprestimoPorId(id);
        return emprestimo.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/por-email")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosPorEmail(@RequestParam String email) {
        List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosPorEmail(email);
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosAtivos() {
        List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosAtivos();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/devolvidos")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosDevolvidos() {
        List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosDevolvidos();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/atrasados")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosAtrasados() {
        List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosAtrasados();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/por-livro/{livroId}")
    public ResponseEntity<List<Emprestimo>> buscarEmprestimosPorLivro(@PathVariable Integer livroId) {
        List<Emprestimo> emprestimos = emprestimoService.buscarEmprestimosPorLivro(livroId);
        return ResponseEntity.ok(emprestimos);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Emprestimo> atualizarEmprestimo(@PathVariable Integer id, @RequestBody Emprestimo emprestimo) {
        Emprestimo emprestimoAtualizado = emprestimoService.atualizarEmprestimo(id, emprestimo);
        return ResponseEntity.ok(emprestimoAtualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarEmprestimo(@PathVariable Integer id) {
        emprestimoService.deletarEmprestimo(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/contar/ativos")
    public ResponseEntity<Long> contarEmprestimosAtivos() {
        Long count = emprestimoService.contarEmprestimosAtivos();
        return ResponseEntity.ok(count);
    }
}
