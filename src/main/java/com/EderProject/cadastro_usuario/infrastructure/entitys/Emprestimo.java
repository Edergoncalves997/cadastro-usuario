package com.EderProject.cadastro_usuario.infrastructure.entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "emprestimo")
@Entity
public class Emprestimo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "livro_id", nullable = false)
    private Livro livro;
    
    @Column(name = "nome_pessoa", nullable = false)
    private String nomePessoa;
    
    @Column(name = "email_pessoa", nullable = false)
    private String emailPessoa;
    
    @Column(name = "telefone_pessoa")
    private String telefonePessoa;
    
    @Column(name = "data_emprestimo", nullable = false)
    @Builder.Default
    private LocalDateTime dataEmprestimo = LocalDateTime.now();
    
    @Column(name = "data_prevista_devolucao")
    private LocalDateTime dataPrevistaDevolucao;
    
    @Column(name = "data_devolucao")
    private LocalDateTime dataDevolucao;
    
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusEmprestimo status = StatusEmprestimo.ATIVO;
    
    @Column(name = "observacoes")
    private String observacoes;
    
    public enum StatusEmprestimo {
        ATIVO, DEVOLVIDO, ATRASADO
    }
}
