package com.EderProject.cadastro_usuario.business;

import com.EderProject.cadastro_usuario.infrastructure.entitys.Livro;
import com.EderProject.cadastro_usuario.infrastructure.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class LivroService {
    
    private final LivroRepository livroRepository;
    private final LivroApiService livroApiService;
    
    public Livro salvarLivro(Livro livro) {
        // Verificar se já existe um livro com o mesmo ISBN
        if (livro.getId() == null && livroRepository.findByIsbn(livro.getIsbn()).isPresent()) {
            throw new RuntimeException("Já existe um livro cadastrado com este ISBN: " + livro.getIsbn());
        }
        return livroRepository.saveAndFlush(livro);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarTodosLivros() {
        return livroRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Livro> buscarLivroPorId(Integer id) {
        return livroRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public Optional<Livro> buscarLivroPorIsbn(String isbn) {
        return livroRepository.findByIsbn(isbn);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarLivrosPorTitulo(String titulo) {
        return livroRepository.findByTituloContainingIgnoreCase(titulo);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarLivrosPorAutor(String autor) {
        return livroRepository.findByAutorContainingIgnoreCase(autor);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarLivrosDisponiveis() {
        return livroRepository.findByDisponivel(true);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarLivrosIndisponiveis() {
        return livroRepository.findByDisponivel(false);
    }
    
    @Transactional(readOnly = true)
    public List<Livro> buscarLivrosPorTermo(String termo) {
        return livroRepository.buscarPorTermo(termo);
    }
    
    public Livro atualizarLivro(Integer id, Livro livroAtualizado) {
        Livro livroExistente = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + id));
        
        // Verificar se o ISBN está sendo alterado e se já existe
        if (!livroExistente.getIsbn().equals(livroAtualizado.getIsbn()) && 
            livroRepository.findByIsbn(livroAtualizado.getIsbn()).isPresent()) {
            throw new RuntimeException("Já existe um livro cadastrado com este ISBN: " + livroAtualizado.getIsbn());
        }
        
        // Atualizar campos
        livroExistente.setTitulo(livroAtualizado.getTitulo() != null ? 
                livroAtualizado.getTitulo() : livroExistente.getTitulo());
        livroExistente.setAutor(livroAtualizado.getAutor() != null ? 
                livroAtualizado.getAutor() : livroExistente.getAutor());
        livroExistente.setIsbn(livroAtualizado.getIsbn() != null ? 
                livroAtualizado.getIsbn() : livroExistente.getIsbn());
        livroExistente.setAnoPublicacao(livroAtualizado.getAnoPublicacao() != null ? 
                livroAtualizado.getAnoPublicacao() : livroExistente.getAnoPublicacao());
        livroExistente.setEditora(livroAtualizado.getEditora() != null ? 
                livroAtualizado.getEditora() : livroExistente.getEditora());
        livroExistente.setDescricao(livroAtualizado.getDescricao() != null ? 
                livroAtualizado.getDescricao() : livroExistente.getDescricao());
        
        return livroRepository.saveAndFlush(livroExistente);
    }
    
    public void deletarLivro(Integer id) {
        if (!livroRepository.existsById(id)) {
            throw new RuntimeException("Livro não encontrado com ID: " + id);
        }
        livroRepository.deleteById(id);
    }
    
    public void marcarComoIndisponivel(Integer id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + id));
        livro.setDisponivel(false);
        livroRepository.saveAndFlush(livro);
    }
    
    public void marcarComoDisponivel(Integer id) {
        Livro livro = livroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + id));
        livro.setDisponivel(true);
        livroRepository.saveAndFlush(livro);
    }
    
    @Transactional(readOnly = true)
    public Long contarLivrosDisponiveis() {
        return livroRepository.countLivrosDisponiveis();
    }
    
    public Livro buscarInformacoesLivroPorIsbn(String isbn) {
        try {
            Optional<LivroApiService.LivroInfo> apiInfo = livroApiService.buscarInformacoesLivro(isbn);
            
            if (apiInfo.isPresent()) {
                LivroApiService.LivroInfo info = apiInfo.get();
                Livro livro = new Livro();
                
                if (info.getTitulo() != null) livro.setTitulo(info.getTitulo());
                if (info.getAutor() != null) livro.setAutor(info.getAutor());
                if (info.getAnoPublicacao() != null) livro.setAnoPublicacao(info.getAnoPublicacao());
                if (info.getEditora() != null) livro.setEditora(info.getEditora());
                if (info.getUrlCapa() != null) livro.setUrlCapa(info.getUrlCapa());
                if (info.getDescricao() != null) livro.setDescricao(info.getDescricao());
                
                livro.setIsbn(isbn);
                livro.setDisponivel(true);
                
                return livro;
            }
            
            throw new RuntimeException("ISBN não encontrado nas APIs externas. Verifique se o ISBN está correto: " + isbn);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar informações do livro: " + e.getMessage());
        }
    }
    
    public Livro atualizarLivroComInformacoesApi(Integer id) {
        try {
            Livro livro = livroRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + id));
            
            // Verificar se o ISBN é válido
            if (livro.getIsbn() == null || livro.getIsbn().trim().isEmpty()) {
                throw new RuntimeException("Livro não possui ISBN válido para busca");
            }
            
            Optional<LivroApiService.LivroInfo> apiInfo = livroApiService.buscarInformacoesLivro(livro.getIsbn());
            
            if (apiInfo.isPresent()) {
                LivroApiService.LivroInfo info = apiInfo.get();
                boolean atualizado = false;
                
                // Atualizar campos, priorizando capa e descrição
                if (info.getTitulo() != null && !info.getTitulo().trim().isEmpty()) {
                    if (livro.getTitulo() == null || livro.getTitulo().trim().isEmpty()) {
                        livro.setTitulo(info.getTitulo());
                        atualizado = true;
                    }
                }
                if (info.getAutor() != null && !info.getAutor().trim().isEmpty()) {
                    if (livro.getAutor() == null || livro.getAutor().trim().isEmpty()) {
                        livro.setAutor(info.getAutor());
                        atualizado = true;
                    }
                }
                if (info.getAnoPublicacao() != null) {
                    if (livro.getAnoPublicacao() == null) {
                        livro.setAnoPublicacao(info.getAnoPublicacao());
                        atualizado = true;
                    }
                }
                if (info.getEditora() != null && !info.getEditora().trim().isEmpty()) {
                    if (livro.getEditora() == null || livro.getEditora().trim().isEmpty()) {
                        livro.setEditora(info.getEditora());
                        atualizado = true;
                    }
                }
                // Sempre tentar atualizar a capa se disponível
                if (info.getUrlCapa() != null && !info.getUrlCapa().trim().isEmpty()) {
                    livro.setUrlCapa(info.getUrlCapa());
                    atualizado = true;
                }
                // Sempre tentar atualizar a descrição se disponível
                if (info.getDescricao() != null && !info.getDescricao().trim().isEmpty()) {
                    if (livro.getDescricao() == null || livro.getDescricao().trim().isEmpty()) {
                        livro.setDescricao(info.getDescricao());
                        atualizado = true;
                    }
                }
                
                if (atualizado) {
                    return livroRepository.saveAndFlush(livro);
                } else {
                    throw new RuntimeException("Nenhuma informação nova encontrada para atualizar o livro");
                }
            }
            
            throw new RuntimeException("ISBN não encontrado nas APIs externas. Verifique se o ISBN está correto: " + livro.getIsbn());
        } catch (RuntimeException e) {
            // Re-throw RuntimeException para manter a mensagem original
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erro interno ao buscar informações do livro: " + e.getMessage());
        }
    }
}
