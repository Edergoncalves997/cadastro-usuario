package com.EderProject.cadastro_usuario.controller;

import com.EderProject.cadastro_usuario.business.EmprestimoService;
import com.EderProject.cadastro_usuario.business.LivroService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/biblioteca")
@RequiredArgsConstructor
public class BibliotecaController {

    private final LivroService livroService;
    private final EmprestimoService emprestimoService;

    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> estatisticas = new HashMap<>();
        
        estatisticas.put("totalLivros", livroService.buscarTodosLivros().size());
        estatisticas.put("livrosDisponiveis", livroService.contarLivrosDisponiveis());
        estatisticas.put("livrosIndisponiveis", 
                livroService.buscarTodosLivros().size() - livroService.contarLivrosDisponiveis());
        estatisticas.put("emprestimosAtivos", emprestimoService.contarEmprestimosAtivos());
        estatisticas.put("emprestimosAtrasados", emprestimoService.buscarEmprestimosAtrasados().size());
        
        return ResponseEntity.ok(estatisticas);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> obterStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "Biblioteca Digital Online");
        status.put("versao", "1.0.0");
        status.put("descricao", "Sistema de gerenciamento de biblioteca digital");
        
        return ResponseEntity.ok(status);
    }
}
