package com.EderProject.cadastro_usuario.infrastructure.entitys;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "livro")
@Entity
public class Livro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "titulo", nullable = false)
    private String titulo;
    
    @Column(name = "autor", nullable = false)
    private String autor;
    
    @Column(name = "isbn", unique = true, nullable = false)
    private String isbn;
    
    @Column(name = "ano_publicacao")
    private Integer anoPublicacao;
    
    @Column(name = "editora")
    private String editora;
    
    @Column(name = "disponivel", nullable = false)
    @Builder.Default
    private Boolean disponivel = true;
    
    @Column(name = "data_cadastro")
    @Builder.Default
    private LocalDateTime dataCadastro = LocalDateTime.now();
    
    @Column(name = "descricao", length = 500)
    private String descricao;
    
    @Column(name = "url_capa")
    private String urlCapa;
}
