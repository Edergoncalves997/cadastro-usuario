package com.EderProject.cadastro_usuario.business;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LivroApiService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String OPEN_LIBRARY_BASE_URL = "https://openlibrary.org/api/books?bibkeys=ISBN:";
    private static final String GOOGLE_BOOKS_BASE_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    
    public Optional<LivroInfo> buscarInformacoesLivro(String isbn) {
        // Validar ISBN básico
        if (isbn == null || isbn.trim().isEmpty()) {
            log.warn("ISBN vazio ou nulo fornecido");
            return Optional.empty();
        }
        
        // Limpar ISBN (remover hífens e espaços)
        String isbnLimpo = isbn.replaceAll("[^0-9X]", "");
        if (isbnLimpo.length() < 10) {
            log.warn("ISBN inválido (muito curto): {} (limpo: {})", isbn, isbnLimpo);
            return Optional.empty();
        }
        
        // Se o ISBN limpo for muito longo, pegar apenas os primeiros 13 dígitos
        if (isbnLimpo.length() > 13) {
            isbnLimpo = isbnLimpo.substring(0, 13);
        }
        
        log.info("Buscando informações para ISBN: {} (limpo: {})", isbn, isbnLimpo);
        
        // Primeiro tenta Open Library (gratuita, sem chave)
        Optional<LivroInfo> openLibraryInfo = buscarNaOpenLibrary(isbnLimpo);
        if (openLibraryInfo.isPresent()) {
            return openLibraryInfo;
        }
        
        // Se não encontrar, tenta Google Books
        Optional<LivroInfo> googleBooksInfo = buscarNoGoogleBooks(isbnLimpo);
        if (googleBooksInfo.isPresent()) {
            return googleBooksInfo;
        }
        
        log.warn("Nenhuma informação encontrada para ISBN: {}", isbn);
        return Optional.empty();
    }
    
    private Optional<LivroInfo> buscarNaOpenLibrary(String isbn) {
        try {
            String url = OPEN_LIBRARY_BASE_URL + isbn + "&format=json&jscmd=data";
            log.debug("Buscando na Open Library: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.trim().isEmpty()) {
                log.debug("Resposta vazia da Open Library para ISBN: {}", isbn);
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            String isbnKey = "ISBN:" + isbn;
            JsonNode bookData = root.get(isbnKey);
            
            if (bookData != null && !bookData.isNull() && !bookData.isEmpty()) {
                LivroInfo livroInfo = new LivroInfo();
                
                // Título
                if (bookData.has("title")) {
                    livroInfo.setTitulo(bookData.get("title").asText());
                }
                
                // Autor
                if (bookData.has("authors") && bookData.get("authors").isArray() && bookData.get("authors").size() > 0) {
                    JsonNode firstAuthor = bookData.get("authors").get(0);
                    if (firstAuthor.has("name")) {
                        livroInfo.setAutor(firstAuthor.get("name").asText());
                    }
                }
                
                // Ano de publicação
                if (bookData.has("publish_date")) {
                    String publishDate = bookData.get("publish_date").asText();
                    try {
                        // Extrair ano da data (formato pode variar)
                        String year = publishDate.replaceAll(".*(\\d{4}).*", "$1");
                        if (year.matches("\\d{4}")) {
                            livroInfo.setAnoPublicacao(Integer.parseInt(year));
                        }
                    } catch (Exception e) {
                        log.debug("Erro ao extrair ano da data: {}", publishDate);
                    }
                }
                
                // Editora
                if (bookData.has("publishers") && bookData.get("publishers").isArray() && bookData.get("publishers").size() > 0) {
                    JsonNode firstPublisher = bookData.get("publishers").get(0);
                    if (firstPublisher.has("name")) {
                        livroInfo.setEditora(firstPublisher.get("name").asText());
                    }
                }
                
                // URL da capa
                if (bookData.has("cover")) {
                    JsonNode cover = bookData.get("cover");
                    String coverUrl = null;
                    if (cover.has("large")) {
                        coverUrl = cover.get("large").asText();
                    } else if (cover.has("medium")) {
                        coverUrl = cover.get("medium").asText();
                    } else if (cover.has("small")) {
                        coverUrl = cover.get("small").asText();
                    }
                    
                    if (coverUrl != null) {
                        // Usar nosso proxy para evitar problemas de CORS
                        try {
                            String proxiedUrl = "/api/images/proxy?url=" + java.net.URLEncoder.encode(coverUrl, "UTF-8");
                            livroInfo.setUrlCapa(proxiedUrl);
                        } catch (Exception e) {
                            // Se der erro na codificação, usar URL original
                            livroInfo.setUrlCapa(coverUrl);
                        }
                    }
                }
                
                // Descrição
                if (bookData.has("subtitle")) {
                    livroInfo.setDescricao(bookData.get("subtitle").asText());
                }
                
                log.info("Informações encontradas na Open Library para ISBN {}: {}", isbn, livroInfo.getTitulo());
                return Optional.of(livroInfo);
            }
            
        } catch (Exception e) {
            log.warn("Erro ao buscar na Open Library para ISBN {}: {}", isbn, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    private Optional<LivroInfo> buscarNoGoogleBooks(String isbn) {
        try {
            String url = GOOGLE_BOOKS_BASE_URL + isbn;
            log.debug("Buscando no Google Books: {}", url);
            
            String response = restTemplate.getForObject(url, String.class);
            if (response == null || response.trim().isEmpty()) {
                log.debug("Resposta vazia do Google Books para ISBN: {}", isbn);
                return Optional.empty();
            }
            
            JsonNode root = objectMapper.readTree(response);
            
            if (root.has("items") && root.get("items").isArray() && root.get("items").size() > 0) {
                JsonNode firstItem = root.get("items").get(0);
                JsonNode volumeInfo = firstItem.get("volumeInfo");
                
                if (volumeInfo != null) {
                    LivroInfo livroInfo = new LivroInfo();
                    
                    // Título
                    if (volumeInfo.has("title")) {
                        livroInfo.setTitulo(volumeInfo.get("title").asText());
                    }
                    
                    // Autor
                    if (volumeInfo.has("authors") && volumeInfo.get("authors").isArray() && volumeInfo.get("authors").size() > 0) {
                        livroInfo.setAutor(volumeInfo.get("authors").get(0).asText());
                    }
                    
                    // Ano de publicação
                    if (volumeInfo.has("publishedDate")) {
                        String publishedDate = volumeInfo.get("publishedDate").asText();
                        try {
                            String year = publishedDate.replaceAll(".*(\\d{4}).*", "$1");
                            if (year.matches("\\d{4}")) {
                                livroInfo.setAnoPublicacao(Integer.parseInt(year));
                            }
                        } catch (Exception e) {
                            log.debug("Erro ao extrair ano da data: {}", publishedDate);
                        }
                    }
                    
                    // Editora
                    if (volumeInfo.has("publisher")) {
                        livroInfo.setEditora(volumeInfo.get("publisher").asText());
                    }
                    
                    // URL da capa
                    if (volumeInfo.has("imageLinks")) {
                        JsonNode imageLinks = volumeInfo.get("imageLinks");
                        if (imageLinks.has("thumbnail")) {
                            String thumbnailUrl = imageLinks.get("thumbnail").asText();
                            // Melhorar qualidade da imagem e corrigir URL
                            String urlCorrigida = thumbnailUrl
                                .replace("zoom=1", "zoom=2")
                                .replace("&edge=curl", "")
                                .replace("&source=gbs_api", "");
                            
                            // Usar nosso proxy para evitar problemas de CORS
                            String proxiedUrl = "/api/images/proxy?url=" + java.net.URLEncoder.encode(urlCorrigida, "UTF-8");
                            livroInfo.setUrlCapa(proxiedUrl);
                        }
                    }
                    
                    // Descrição
                    if (volumeInfo.has("description")) {
                        String description = volumeInfo.get("description").asText();
                        // Limitar descrição a 500 caracteres
                        if (description.length() > 500) {
                            description = description.substring(0, 497) + "...";
                        }
                        livroInfo.setDescricao(description);
                    }
                    
                    log.info("Informações encontradas no Google Books para ISBN {}: {}", isbn, livroInfo.getTitulo());
                    return Optional.of(livroInfo);
                }
            }
            
        } catch (Exception e) {
            log.warn("Erro ao buscar no Google Books para ISBN {}: {}", isbn, e.getMessage());
        }
        
        return Optional.empty();
    }
    
    public static class LivroInfo {
        private String titulo;
        private String autor;
        private Integer anoPublicacao;
        private String editora;
        private String urlCapa;
        private String descricao;
        
        // Getters e Setters
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        
        public String getAutor() { return autor; }
        public void setAutor(String autor) { this.autor = autor; }
        
        public Integer getAnoPublicacao() { return anoPublicacao; }
        public void setAnoPublicacao(Integer anoPublicacao) { this.anoPublicacao = anoPublicacao; }
        
        public String getEditora() { return editora; }
        public void setEditora(String editora) { this.editora = editora; }
        
        public String getUrlCapa() { return urlCapa; }
        public void setUrlCapa(String urlCapa) { this.urlCapa = urlCapa; }
        
        public String getDescricao() { return descricao; }
        public void setDescricao(String descricao) { this.descricao = descricao; }
    }
}
