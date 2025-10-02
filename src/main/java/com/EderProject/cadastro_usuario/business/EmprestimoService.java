package com.EderProject.cadastro_usuario.business;

import com.EderProject.cadastro_usuario.infrastructure.entitys.Emprestimo;
import com.EderProject.cadastro_usuario.infrastructure.entitys.Livro;
import com.EderProject.cadastro_usuario.infrastructure.repository.EmprestimoRepository;
import com.EderProject.cadastro_usuario.infrastructure.repository.LivroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmprestimoService {
    
    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;
    
    public Emprestimo realizarEmprestimo(Integer livroId, String nomePessoa, String emailPessoa, 
                                        String telefonePessoa, Integer diasEmprestimo, String observacoes) {
        
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + livroId));
        
        // Verificar se o livro está disponível
        if (!livro.getDisponivel()) {
            throw new RuntimeException("Livro não está disponível para empréstimo");
        }
        
        // Verificar se já existe empréstimo ativo para este livro
        if (emprestimoRepository.findEmprestimoAtivoPorLivro(livroId).isPresent()) {
            throw new RuntimeException("Este livro já possui um empréstimo ativo");
        }
        
        LocalDateTime dataEmprestimo = LocalDateTime.now();
        LocalDateTime dataPrevistaDevolucao = dataEmprestimo.plusDays(diasEmprestimo != null ? diasEmprestimo : 7);
        
        Emprestimo emprestimo = Emprestimo.builder()
                .livro(livro)
                .nomePessoa(nomePessoa)
                .emailPessoa(emailPessoa)
                .telefonePessoa(telefonePessoa)
                .dataEmprestimo(dataEmprestimo)
                .dataPrevistaDevolucao(dataPrevistaDevolucao)
                .status(Emprestimo.StatusEmprestimo.ATIVO)
                .observacoes(observacoes)
                .build();
        
        // Marcar livro como indisponível
        livro.setDisponivel(false);
        livroRepository.saveAndFlush(livro);
        
        return emprestimoRepository.saveAndFlush(emprestimo);
    }
    
    public Emprestimo devolverLivro(Integer emprestimoId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado com ID: " + emprestimoId));
        
        if (emprestimo.getStatus() != Emprestimo.StatusEmprestimo.ATIVO) {
            throw new RuntimeException("Este empréstimo já foi finalizado");
        }
        
        // Marcar como devolvido
        emprestimo.setStatus(Emprestimo.StatusEmprestimo.DEVOLVIDO);
        emprestimo.setDataDevolucao(LocalDateTime.now());
        
        // Marcar livro como disponível
        emprestimo.getLivro().setDisponivel(true);
        livroRepository.saveAndFlush(emprestimo.getLivro());
        
        return emprestimoRepository.saveAndFlush(emprestimo);
    }
    
    public Emprestimo devolverLivroPorIdLivro(Integer livroId) {
        Emprestimo emprestimo = emprestimoRepository.findEmprestimoAtivoPorLivro(livroId)
                .orElseThrow(() -> new RuntimeException("Não há empréstimo ativo para este livro"));
        
        return devolverLivro(emprestimo.getId());
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarTodosEmprestimos() {
        return emprestimoRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<Emprestimo> buscarEmprestimoPorId(Integer id) {
        return emprestimoRepository.findById(id);
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosPorEmail(String email) {
        return emprestimoRepository.findByEmailPessoa(email);
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosAtivos() {
        return emprestimoRepository.findByStatus(Emprestimo.StatusEmprestimo.ATIVO);
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosDevolvidos() {
        return emprestimoRepository.findByStatus(Emprestimo.StatusEmprestimo.DEVOLVIDO);
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosAtrasados() {
        return emprestimoRepository.findEmprestimosAtrasados(LocalDateTime.now());
    }
    
    @Transactional(readOnly = true)
    public List<Emprestimo> buscarEmprestimosPorLivro(Integer livroId) {
        Livro livro = livroRepository.findById(livroId)
                .orElseThrow(() -> new RuntimeException("Livro não encontrado com ID: " + livroId));
        return emprestimoRepository.findByLivro(livro);
    }
    
    public Emprestimo atualizarEmprestimo(Integer id, Emprestimo emprestimoAtualizado) {
        Emprestimo emprestimoExistente = emprestimoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado com ID: " + id));
        
        // Atualizar campos permitidos
        if (emprestimoAtualizado.getNomePessoa() != null) {
            emprestimoExistente.setNomePessoa(emprestimoAtualizado.getNomePessoa());
        }
        if (emprestimoAtualizado.getEmailPessoa() != null) {
            emprestimoExistente.setEmailPessoa(emprestimoAtualizado.getEmailPessoa());
        }
        if (emprestimoAtualizado.getTelefonePessoa() != null) {
            emprestimoExistente.setTelefonePessoa(emprestimoAtualizado.getTelefonePessoa());
        }
        if (emprestimoAtualizado.getDataPrevistaDevolucao() != null) {
            emprestimoExistente.setDataPrevistaDevolucao(emprestimoAtualizado.getDataPrevistaDevolucao());
        }
        if (emprestimoAtualizado.getObservacoes() != null) {
            emprestimoExistente.setObservacoes(emprestimoAtualizado.getObservacoes());
        }
        
        return emprestimoRepository.saveAndFlush(emprestimoExistente);
    }
    
    public void deletarEmprestimo(Integer id) {
        Emprestimo emprestimo = emprestimoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empréstimo não encontrado com ID: " + id));
        
        // Se o empréstimo estiver ativo, marcar livro como disponível antes de deletar
        if (emprestimo.getStatus() == Emprestimo.StatusEmprestimo.ATIVO) {
            emprestimo.getLivro().setDisponivel(true);
            livroRepository.saveAndFlush(emprestimo.getLivro());
        }
        
        emprestimoRepository.deleteById(id);
    }
    
    @Transactional(readOnly = true)
    public Long contarEmprestimosAtivos() {
        return emprestimoRepository.countEmprestimosAtivos();
    }
}
