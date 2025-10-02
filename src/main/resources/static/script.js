// Biblioteca Digital - Frontend JavaScript
class BibliotecaApp {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.currentTab = 'dashboard';
        this.livros = [];
        this.emprestimos = [];
        this.livrosDisponiveis = [];
        
        this.init();
    }

    init() {
        this.setupEventListeners();
        this.checkConnection();
        this.loadDashboard();
    }

    setupEventListeners() {
        // Navigation tabs
        document.querySelectorAll('.nav-tab').forEach(tab => {
            tab.addEventListener('click', (e) => this.switchTab(e.target.dataset.tab));
        });

        // Dashboard
        document.getElementById('refreshStats').addEventListener('click', () => this.loadDashboard());

        // Livros
        document.getElementById('addLivroBtn').addEventListener('click', () => this.openLivroModal());
        document.getElementById('refreshLivros').addEventListener('click', () => this.loadLivros());
        document.getElementById('livroFilter').addEventListener('change', () => this.filterLivros());
        document.getElementById('livroForm').addEventListener('submit', (e) => this.saveLivro(e));
        document.getElementById('closeLivroModal').addEventListener('click', () => this.closeLivroModal());
        document.getElementById('cancelLivro').addEventListener('click', () => this.closeLivroModal());

        // Empréstimos
        document.getElementById('addEmprestimoBtn').addEventListener('click', () => this.openEmprestimoModal());
        document.getElementById('refreshEmprestimos').addEventListener('click', () => this.loadEmprestimos());
        document.getElementById('emprestimoFilter').addEventListener('change', () => this.filterEmprestimos());
        document.getElementById('emprestimoForm').addEventListener('submit', (e) => this.saveEmprestimo(e));
        document.getElementById('closeEmprestimoModal').addEventListener('click', () => this.closeEmprestimoModal());
        document.getElementById('cancelEmprestimo').addEventListener('click', () => this.closeEmprestimoModal());

        // Busca
        document.getElementById('searchBtn').addEventListener('click', () => this.performSearch());
        document.getElementById('searchTerm').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.performSearch();
        });

        // Modal close on outside click
        window.addEventListener('click', (e) => {
            if (e.target.classList.contains('modal')) {
                this.closeAllModals();
            }
        });
    }

    // Connection and Status
    async checkConnection() {
        try {
            const response = await fetch(`${this.baseURL}/biblioteca/status`);
            if (response.ok) {
                this.updateConnectionStatus(true);
            } else {
                this.updateConnectionStatus(false);
            }
        } catch (error) {
            this.updateConnectionStatus(false);
        }
    }

    updateConnectionStatus(isOnline) {
        const statusElement = document.getElementById('connectionStatus');
        if (isOnline) {
            statusElement.className = 'status online';
            statusElement.innerHTML = '<i class="fas fa-circle"></i> Online';
        } else {
            statusElement.className = 'status offline';
            statusElement.innerHTML = '<i class="fas fa-circle"></i> Offline';
        }
    }

    // Navigation
    switchTab(tabName) {
        // Update active tab
        document.querySelectorAll('.nav-tab').forEach(tab => tab.classList.remove('active'));
        document.querySelector(`[data-tab="${tabName}"]`).classList.add('active');

        // Update active content
        document.querySelectorAll('.tab-content').forEach(content => content.classList.remove('active'));
        document.getElementById(tabName).classList.add('active');

        this.currentTab = tabName;

        // Load content based on tab
        switch (tabName) {
            case 'dashboard':
                this.loadDashboard();
                break;
            case 'livros':
                this.loadLivros();
                break;
            case 'emprestimos':
                this.loadEmprestimos();
                break;
            case 'buscar':
                this.clearSearchResults();
                break;
        }
    }

    // Dashboard
    async loadDashboard() {
        this.showLoading();
        try {
            const [statsResponse, livrosResponse, emprestimosResponse] = await Promise.all([
                fetch(`${this.baseURL}/biblioteca/estatisticas`),
                fetch(`${this.baseURL}/livros`),
                fetch(`${this.baseURL}/emprestimos`)
            ]);

            if (statsResponse.ok) {
                const stats = await statsResponse.json();
                this.updateStatsDisplay(stats);
            }

            if (livrosResponse.ok) {
                this.livros = await livrosResponse.json();
            }

            if (emprestimosResponse.ok) {
                this.emprestimos = await emprestimosResponse.json();
                this.updateRecentActivity();
            }

        } catch (error) {
            this.showToast('Erro ao carregar dashboard', 'error');
        } finally {
            this.hideLoading();
        }
    }

    updateStatsDisplay(stats) {
        document.getElementById('totalLivros').textContent = stats.totalLivros || 0;
        document.getElementById('livrosDisponiveis').textContent = stats.livrosDisponiveis || 0;
        document.getElementById('livrosIndisponiveis').textContent = stats.livrosIndisponiveis || 0;
        document.getElementById('emprestimosAtivos').textContent = stats.emprestimosAtivos || 0;
        document.getElementById('emprestimosAtrasados').textContent = stats.emprestimosAtrasados || 0;
    }

    updateRecentActivity() {
        const activityContainer = document.getElementById('recentActivity');
        const recentEmprestimos = this.emprestimos
            .sort((a, b) => new Date(b.dataEmprestimo) - new Date(a.dataEmprestimo))
            .slice(0, 5);

        if (recentEmprestimos.length === 0) {
            activityContainer.innerHTML = '<p class="text-muted text-center">Nenhuma atividade recente</p>';
            return;
        }

        activityContainer.innerHTML = recentEmprestimos.map(emprestimo => `
            <div class="activity-item">
                <div class="activity-icon">
                    <i class="fas fa-${emprestimo.status === 'ATIVO' ? 'hand-holding' : 'check-circle'}"></i>
                </div>
                <div class="activity-content">
                    <div class="activity-title">
                        ${emprestimo.status === 'ATIVO' ? 'Livro emprestado' : 'Livro devolvido'}: 
                        ${emprestimo.livro ? emprestimo.livro.titulo : 'N/A'}
                    </div>
                    <div class="activity-time">
                        ${emprestimo.nomePessoa} - ${this.formatDate(emprestimo.dataEmprestimo)}
                    </div>
                </div>
            </div>
        `).join('');
    }

    // Livros Management
    async loadLivros() {
        this.showLoading();
        try {
            const response = await fetch(`${this.baseURL}/livros`);
            if (response.ok) {
                this.livros = await response.json();
                this.displayLivros(this.livros);
                this.updateLivrosDisponiveis();
            } else {
                this.showToast('Erro ao carregar livros', 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão ao carregar livros', 'error');
        } finally {
            this.hideLoading();
        }
    }

    displayLivros(livros) {
        const tbody = document.getElementById('livrosTableBody');
        if (livros.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">Nenhum livro encontrado</td></tr>';
            return;
        }

        tbody.innerHTML = livros.map(livro => `
            <tr>
                <td>${livro.id}</td>
                <td>
                    <div class="livro-info">
                        <div class="livro-capa-container">
                            ${livro.urlCapa ? 
                                `<img src="${livro.urlCapa}" alt="Capa do livro" class="livro-capa" onerror="this.style.display='none'; this.nextElementSibling.style.display='flex'" onload="this.nextElementSibling.style.display='none'">` : 
                                ''
                            }
                            <div class="livro-capa-placeholder" style="${livro.urlCapa ? 'display: none;' : 'display: flex;'}">
                                <i class="fas fa-book"></i>
                            </div>
                        </div>
                        <div class="livro-detalhes">
                            <strong>${livro.titulo}</strong>
                        </div>
                    </div>
                </td>
                <td>${livro.autor || '-'}</td>
                <td>${livro.isbn}</td>
                <td>${livro.anoPublicacao || '-'}</td>
                <td>
                    <span class="status-badge ${livro.disponivel ? 'status-available' : 'status-unavailable'}">
                        ${livro.disponivel ? 'Disponível' : 'Emprestado'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-info" onclick="app.buscarInformacoesLivro(${livro.id})" title="Buscar informações da API">
                        <i class="fas fa-search"></i>
                    </button>
                    <button class="btn btn-sm btn-secondary" onclick="app.editLivro(${livro.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-warning" onclick="app.toggleLivroDisponibilidade(${livro.id}, ${livro.disponivel})">
                        <i class="fas fa-${livro.disponivel ? 'lock' : 'unlock'}"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="app.deleteLivro(${livro.id}, '${livro.titulo}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    filterLivros() {
        const filter = document.getElementById('livroFilter').value;
        let filteredLivros = this.livros;

        switch (filter) {
            case 'available':
                filteredLivros = this.livros.filter(livro => livro.disponivel);
                break;
            case 'unavailable':
                filteredLivros = this.livros.filter(livro => !livro.disponivel);
                break;
        }

        this.displayLivros(filteredLivros);
    }

    updateLivrosDisponiveis() {
        this.livrosDisponiveis = this.livros.filter(livro => livro.disponivel);
    }

    openLivroModal(livro = null) {
        const modal = document.getElementById('livroModal');
        const form = document.getElementById('livroForm');
        const title = document.getElementById('livroModalTitle');

        if (livro) {
            title.textContent = 'Editar Livro';
            this.populateLivroForm(livro);
        } else {
            title.textContent = 'Novo Livro';
            form.reset();
        }

        modal.style.display = 'block';
    }

    populateLivroForm(livro) {
        document.getElementById('livroId').value = livro.id;
        document.getElementById('titulo').value = livro.titulo;
        document.getElementById('autor').value = livro.autor;
        document.getElementById('isbn').value = livro.isbn;
        document.getElementById('anoPublicacao').value = livro.anoPublicacao || '';
        document.getElementById('editora').value = livro.editora || '';
        document.getElementById('descricao').value = livro.descricao || '';
        
        // Atualizar capa do livro no modal
        this.updateLivroCapaModal(livro.urlCapa);
    }
    
    updateLivroCapaModal(urlCapa) {
        const capaContainer = document.getElementById('livroCapaContainer');
        if (!capaContainer) {
            // Criar container se não existir
            const form = document.getElementById('livroForm');
            const tituloField = document.getElementById('titulo');
            const capaDiv = document.createElement('div');
            capaDiv.id = 'livroCapaContainer';
            capaDiv.className = 'livro-capa-modal-container';
            capaDiv.innerHTML = `
                <label style="font-size: 0.9rem; margin-bottom: 5px;">Capa do Livro:</label>
                <div class="livro-capa-modal">
                    <div class="livro-capa-placeholder-modal">
                        <i class="fas fa-book"></i>
                    </div>
                </div>
            `;
            form.insertBefore(capaDiv, tituloField.parentNode);
        }
        
        const capaImg = capaContainer.querySelector('.livro-capa-modal img');
        const placeholder = capaContainer.querySelector('.livro-capa-placeholder-modal');
        
        if (urlCapa) {
            if (capaImg) {
                capaImg.src = urlCapa;
                capaImg.style.display = 'block';
                placeholder.style.display = 'none';
            } else {
                const img = document.createElement('img');
                img.src = urlCapa;
                img.className = 'livro-capa-modal-img';
                img.alt = 'Capa do livro';
                img.onerror = function() {
                    this.style.display = 'none';
                    placeholder.style.display = 'flex';
                };
                img.onload = function() {
                    placeholder.style.display = 'none';
                };
                capaContainer.querySelector('.livro-capa-modal').appendChild(img);
                placeholder.style.display = 'none';
            }
        } else {
            if (capaImg) capaImg.style.display = 'none';
            placeholder.style.display = 'flex';
        }
    }

    closeLivroModal() {
        document.getElementById('livroModal').style.display = 'none';
        document.getElementById('livroForm').reset();
    }

    async saveLivro(e) {
        e.preventDefault();
        
        const formData = new FormData(e.target);
        const livroData = {
            titulo: formData.get('titulo') || document.getElementById('titulo').value,
            autor: formData.get('autor') || document.getElementById('autor').value,
            isbn: formData.get('isbn') || document.getElementById('isbn').value,
            anoPublicacao: parseInt(document.getElementById('anoPublicacao').value) || null,
            editora: document.getElementById('editora').value || null,
            descricao: document.getElementById('descricao').value || null
        };

        const livroId = document.getElementById('livroId').value;
        const isEdit = !!livroId;

        this.showLoading();
        try {
            const url = isEdit ? `${this.baseURL}/livros/${livroId}` : `${this.baseURL}/livros`;
            const method = isEdit ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(livroData)
            });

            if (response.ok) {
                this.showToast(`Livro ${isEdit ? 'atualizado' : 'cadastrado'} com sucesso!`, 'success');
                this.closeLivroModal();
                this.loadLivros();
                if (this.currentTab === 'dashboard') {
                    this.loadDashboard();
                }
            } else {
                const error = await response.text();
                this.showToast(`Erro: ${error}`, 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.hideLoading();
        }
    }

    async editLivro(id) {
        const livro = this.livros.find(l => l.id === id);
        if (livro) {
            this.openLivroModal(livro);
        }
    }

    async toggleLivroDisponibilidade(id, currentStatus) {
        this.showLoading();
        try {
            const endpoint = currentStatus ? 'marcar-indisponivel' : 'marcar-disponivel';
            const response = await fetch(`${this.baseURL}/livros/${id}/${endpoint}`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showToast(`Livro marcado como ${currentStatus ? 'indisponível' : 'disponível'}`, 'success');
                this.loadLivros();
                if (this.currentTab === 'dashboard') {
                    this.loadDashboard();
                }
            } else {
                this.showToast('Erro ao alterar status do livro', 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.hideLoading();
        }
    }

    async deleteLivro(id, titulo) {
        if (confirm(`Tem certeza que deseja excluir o livro "${titulo}"?`)) {
            this.showLoading();
            try {
                const response = await fetch(`${this.baseURL}/livros/${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    this.showToast('Livro excluído com sucesso!', 'success');
                    this.loadLivros();
                    if (this.currentTab === 'dashboard') {
                        this.loadDashboard();
                    }
                } else {
                    this.showToast('Erro ao excluir livro', 'error');
                }
            } catch (error) {
                this.showToast('Erro de conexão', 'error');
            } finally {
                this.hideLoading();
            }
        }
    }

    async buscarInformacoesLivro(id) {
        this.showLoading();
        try {
            const response = await fetch(`${this.baseURL}/livros/${id}/buscar-informacoes`, {
                method: 'PUT'
            });

            if (response.ok) {
                this.showToast('Informações do livro atualizadas com sucesso!', 'success');
                this.loadLivros();
                if (this.currentTab === 'dashboard') {
                    this.loadDashboard();
                }
            } else {
                let errorMessage = 'Erro desconhecido';
                try {
                    const errorData = await response.json();
                    if (errorData.message) {
                        errorMessage = errorData.message;
                    } else if (errorData.error) {
                        errorMessage = errorData.error;
                    }
                } catch (e) {
                    const errorText = await response.text();
                    if (errorText) {
                        errorMessage = errorText;
                    }
                }
                
                // Tratar erros específicos
                if (errorMessage.includes('ISBN não encontrado')) {
                    this.showToast('ISBN não encontrado nas APIs externas. Verifique se o ISBN está correto.', 'warning');
                } else if (errorMessage.includes('Nenhuma informação nova encontrada')) {
                    this.showToast('O livro já possui todas as informações disponíveis.', 'info');
                } else {
                    this.showToast(`Erro: ${errorMessage}`, 'error');
                }
            }
        } catch (error) {
            this.showToast('Erro de conexão com o servidor', 'error');
        } finally {
            this.hideLoading();
        }
    }

    // Empréstimos Management
    async loadEmprestimos() {
        this.showLoading();
        try {
            const response = await fetch(`${this.baseURL}/emprestimos`);
            if (response.ok) {
                this.emprestimos = await response.json();
                this.displayEmprestimos(this.emprestimos);
            } else {
                this.showToast('Erro ao carregar empréstimos', 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão ao carregar empréstimos', 'error');
        } finally {
            this.hideLoading();
        }
    }

    displayEmprestimos(emprestimos) {
        const tbody = document.getElementById('emprestimosTableBody');
        if (emprestimos.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">Nenhum empréstimo encontrado</td></tr>';
            return;
        }

        tbody.innerHTML = emprestimos.map(emprestimo => `
            <tr>
                <td>${emprestimo.id}</td>
                <td>${emprestimo.livro ? emprestimo.livro.titulo : 'N/A'}</td>
                <td>${emprestimo.nomePessoa}</td>
                <td>${emprestimo.emailPessoa}</td>
                <td>${this.formatDate(emprestimo.dataEmprestimo)}</td>
                <td>${emprestimo.dataPrevistaDevolucao ? this.formatDate(emprestimo.dataPrevistaDevolucao) : '-'}</td>
                <td>
                    <span class="status-badge ${this.getStatusClass(emprestimo.status)}">
                        ${this.getStatusText(emprestimo.status)}
                    </span>
                </td>
                <td>
                    ${emprestimo.status === 'ATIVO' ? 
                        `<button class="btn btn-sm btn-success" onclick="app.devolverLivro(${emprestimo.id})">
                            <i class="fas fa-undo"></i>
                        </button>` : ''
                    }
                    <button class="btn btn-sm btn-secondary" onclick="app.editEmprestimo(${emprestimo.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="app.deleteEmprestimo(${emprestimo.id}, '${emprestimo.nomePessoa}')">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    filterEmprestimos() {
        const filter = document.getElementById('emprestimoFilter').value;
        let filteredEmprestimos = this.emprestimos;

        switch (filter) {
            case 'active':
                filteredEmprestimos = this.emprestimos.filter(e => e.status === 'ATIVO');
                break;
            case 'returned':
                filteredEmprestimos = this.emprestimos.filter(e => e.status === 'DEVOLVIDO');
                break;
            case 'overdue':
                // This would need to be implemented based on current date vs dataPrevistaDevolucao
                filteredEmprestimos = this.emprestimos.filter(e => e.status === 'ATIVO' && this.isOverdue(e));
                break;
        }

        this.displayEmprestimos(filteredEmprestimos);
    }

    isOverdue(emprestimo) {
        if (emprestimo.status !== 'ATIVO' || !emprestimo.dataPrevistaDevolucao) return false;
        return new Date(emprestimo.dataPrevistaDevolucao) < new Date();
    }

    openEmprestimoModal(emprestimo = null) {
        const modal = document.getElementById('emprestimoModal');
        const form = document.getElementById('emprestimoForm');
        const title = document.getElementById('emprestimoModalTitle');

        if (emprestimo) {
            title.textContent = 'Editar Empréstimo';
            this.populateEmprestimoForm(emprestimo);
            // Para edição, incluir todos os livros para permitir visualização
            this.populateLivrosSelect(true);
        } else {
            title.textContent = 'Novo Empréstimo';
            form.reset();
            this.populateLivrosSelect(false);
        }

        modal.style.display = 'block';
    }

    populateLivrosSelect(includeAllLivros = false) {
        const select = document.getElementById('livroSelect');
        select.innerHTML = '<option value="">Selecione um livro...</option>';
        
        const livrosToShow = includeAllLivros ? this.livros : this.livrosDisponiveis;
        
        livrosToShow.forEach(livro => {
            const option = document.createElement('option');
            option.value = livro.id;
            option.textContent = `${livro.titulo} - ${livro.autor}${!livro.disponivel ? ' (Emprestado)' : ''}`;
            option.disabled = !livro.disponivel && !includeAllLivros;
            select.appendChild(option);
        });
    }

    populateEmprestimoForm(emprestimo) {
        document.getElementById('emprestimoId').value = emprestimo.id;
        document.getElementById('livroSelect').value = emprestimo.livro ? emprestimo.livro.id : '';
        document.getElementById('nomePessoa').value = emprestimo.nomePessoa;
        document.getElementById('emailPessoa').value = emprestimo.emailPessoa;
        document.getElementById('telefonePessoa').value = emprestimo.telefonePessoa || '';
        document.getElementById('diasEmprestimo').value = this.calculateDaysDifference(emprestimo.dataEmprestimo, emprestimo.dataPrevistaDevolucao) || 7;
        document.getElementById('observacoes').value = emprestimo.observacoes || '';
    }

    calculateDaysDifference(startDate, endDate) {
        if (!startDate || !endDate) return null;
        const start = new Date(startDate);
        const end = new Date(endDate);
        const diffTime = Math.abs(end - start);
        return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    }

    calculateNewReturnDate(startDate, daysToAdd) {
        const start = new Date(startDate);
        const newDate = new Date(start);
        newDate.setDate(start.getDate() + daysToAdd);
        return newDate.toISOString();
    }

    closeEmprestimoModal() {
        document.getElementById('emprestimoModal').style.display = 'none';
        document.getElementById('emprestimoForm').reset();
    }

    async saveEmprestimo(e) {
        e.preventDefault();
        
        const emprestimoId = document.getElementById('emprestimoId').value;
        const isEdit = !!emprestimoId;

        if (isEdit) {
            await this.updateEmprestimo();
        } else {
            await this.createEmprestimo();
        }
    }

    async createEmprestimo() {
        const formData = {
            livroId: parseInt(document.getElementById('livroSelect').value),
            nomePessoa: document.getElementById('nomePessoa').value,
            emailPessoa: document.getElementById('emailPessoa').value,
            telefonePessoa: document.getElementById('telefonePessoa').value || null,
            diasEmprestimo: parseInt(document.getElementById('diasEmprestimo').value),
            observacoes: document.getElementById('observacoes').value || null
        };

        this.showLoading();
        try {
            const url = `${this.baseURL}/emprestimos/emprestar?` + new URLSearchParams(formData);
            const response = await fetch(url, {
                method: 'POST'
            });

            if (response.ok) {
                this.showToast('Empréstimo realizado com sucesso!', 'success');
                this.closeEmprestimoModal();
                this.loadEmprestimos();
                this.loadLivros(); // Refresh to update availability
                if (this.currentTab === 'dashboard') {
                    this.loadDashboard();
                }
            } else {
                const error = await response.text();
                this.showToast(`Erro: ${error}`, 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.hideLoading();
        }
    }

    async updateEmprestimo() {
        const emprestimoId = document.getElementById('emprestimoId').value;
        const emprestimo = this.emprestimos.find(e => e.id == emprestimoId);
        
        // Se o empréstimo foi devolvido, não permitir edição - sugerir novo empréstimo
        if (emprestimo && emprestimo.status === 'DEVOLVIDO') {
            this.showToast('Empréstimos devolvidos não podem ser editados. Crie um novo empréstimo.', 'warning');
            this.closeEmprestimoModal();
            return;
        }

        const emprestimoData = {
            nomePessoa: document.getElementById('nomePessoa').value,
            emailPessoa: document.getElementById('emailPessoa').value,
            telefonePessoa: document.getElementById('telefonePessoa').value || null,
            dataPrevistaDevolucao: this.calculateNewReturnDate(emprestimo.dataEmprestimo, parseInt(document.getElementById('diasEmprestimo').value)),
            observacoes: document.getElementById('observacoes').value || null
        };

        this.showLoading();
        try {
            const response = await fetch(`${this.baseURL}/emprestimos/${emprestimoId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(emprestimoData)
            });

            if (response.ok) {
                this.showToast('Empréstimo atualizado com sucesso!', 'success');
                this.closeEmprestimoModal();
                this.loadEmprestimos();
            } else {
                const error = await response.text();
                this.showToast(`Erro: ${error}`, 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão', 'error');
        } finally {
            this.hideLoading();
        }
    }

    async editEmprestimo(id) {
        const emprestimo = this.emprestimos.find(e => e.id === id);
        if (emprestimo) {
            // Se o empréstimo foi devolvido, oferecer opção de novo empréstimo
            if (emprestimo.status === 'DEVOLVIDO') {
                if (confirm('Este empréstimo foi devolvido. Deseja criar um novo empréstimo para o mesmo livro?')) {
                    this.createNewEmprestimoFromReturned(emprestimo);
                    return;
                }
            }
            this.openEmprestimoModal(emprestimo);
        }
    }

    createNewEmprestimoFromReturned(emprestimoDevolvido) {
        // Criar um novo empréstimo baseado no devolvido
        const novoEmprestimo = {
            livro: emprestimoDevolvido.livro,
            nomePessoa: emprestimoDevolvido.nomePessoa,
            emailPessoa: emprestimoDevolvido.emailPessoa,
            telefonePessoa: emprestimoDevolvido.telefonePessoa,
            observacoes: `Re-empréstimo baseado no empréstimo #${emprestimoDevolvido.id}`
        };
        
        this.openEmprestimoModal();
        
        // Preencher os campos com os dados do empréstimo devolvido
        document.getElementById('livroSelect').value = novoEmprestimo.livro.id;
        document.getElementById('nomePessoa').value = novoEmprestimo.nomePessoa;
        document.getElementById('emailPessoa').value = novoEmprestimo.emailPessoa;
        document.getElementById('telefonePessoa').value = novoEmprestimo.telefonePessoa || '';
        document.getElementById('observacoes').value = novoEmprestimo.observacoes;
    }

    async devolverLivro(emprestimoId) {
        if (confirm('Confirmar devolução do livro?')) {
            this.showLoading();
            try {
                const response = await fetch(`${this.baseURL}/emprestimos/${emprestimoId}/devolver`, {
                    method: 'PUT'
                });

                if (response.ok) {
                    this.showToast('Livro devolvido com sucesso!', 'success');
                    this.loadEmprestimos();
                    this.loadLivros(); // Refresh to update availability
                    if (this.currentTab === 'dashboard') {
                        this.loadDashboard();
                    }
                } else {
                    this.showToast('Erro ao devolver livro', 'error');
                }
            } catch (error) {
                this.showToast('Erro de conexão', 'error');
            } finally {
                this.hideLoading();
            }
        }
    }

    async deleteEmprestimo(id, nomePessoa) {
        if (confirm(`Tem certeza que deseja excluir o empréstimo de "${nomePessoa}"?`)) {
            this.showLoading();
            try {
                const response = await fetch(`${this.baseURL}/emprestimos/${id}`, {
                    method: 'DELETE'
                });

                if (response.ok) {
                    this.showToast('Empréstimo excluído com sucesso!', 'success');
                    this.loadEmprestimos();
                    this.loadLivros(); // Refresh to update availability
                    if (this.currentTab === 'dashboard') {
                        this.loadDashboard();
                    }
                } else {
                    this.showToast('Erro ao excluir empréstimo', 'error');
                }
            } catch (error) {
                this.showToast('Erro de conexão', 'error');
            } finally {
                this.hideLoading();
            }
        }
    }

    // Search
    async performSearch() {
        const term = document.getElementById('searchTerm').value.trim();
        const type = document.getElementById('searchType').value;

        if (!term) {
            this.showToast('Digite um termo para busca', 'warning');
            return;
        }

        this.showLoading();
        try {
            let url;
            switch (type) {
                case 'title':
                    url = `${this.baseURL}/livros/titulo?titulo=${encodeURIComponent(term)}`;
                    break;
                case 'author':
                    url = `${this.baseURL}/livros/autor?autor=${encodeURIComponent(term)}`;
                    break;
                case 'isbn':
                    url = `${this.baseURL}/livros/isbn/${encodeURIComponent(term)}`;
                    break;
                default:
                    url = `${this.baseURL}/livros/buscar?termo=${encodeURIComponent(term)}`;
            }

            const response = await fetch(url);
            if (response.ok) {
                let results;
                if (type === 'isbn') {
                    const livro = await response.json();
                    results = livro ? [livro] : [];
                } else {
                    results = await response.json();
                }
                this.displaySearchResults(results, term, type);
            } else {
                this.showToast('Erro na busca', 'error');
            }
        } catch (error) {
            this.showToast('Erro de conexão na busca', 'error');
        } finally {
            this.hideLoading();
        }
    }

    displaySearchResults(results, term, type) {
        const container = document.getElementById('searchResults');
        
        if (results.length === 0) {
            container.innerHTML = `
                <div class="text-center text-muted">
                    <i class="fas fa-search fa-3x mb-3"></i>
                    <h3>Nenhum resultado encontrado</h3>
                    <p>Não foram encontrados livros para o termo "${term}"</p>
                </div>
            `;
            return;
        }

        container.innerHTML = `
            <div class="search-header">
                <h3>Resultados da busca por "${term}"</h3>
                <p>${results.length} livro(s) encontrado(s)</p>
            </div>
            <div class="results-grid">
                ${results.map(livro => `
                    <div class="result-card">
                        <div class="result-header">
                            <h4>${livro.titulo}</h4>
                            <span class="status-badge ${livro.disponivel ? 'status-available' : 'status-unavailable'}">
                                ${livro.disponivel ? 'Disponível' : 'Emprestado'}
                            </span>
                        </div>
                        <div class="result-content">
                            <p><strong>Autor:</strong> ${livro.autor}</p>
                            <p><strong>ISBN:</strong> ${livro.isbn}</p>
                            ${livro.anoPublicacao ? `<p><strong>Ano:</strong> ${livro.anoPublicacao}</p>` : ''}
                            ${livro.editora ? `<p><strong>Editora:</strong> ${livro.editora}</p>` : ''}
                            ${livro.descricao ? `<p><strong>Descrição:</strong> ${livro.descricao}</p>` : ''}
                        </div>
                        <div class="result-actions">
                            <button class="btn btn-sm btn-secondary" onclick="app.editLivro(${livro.id})">
                                <i class="fas fa-edit"></i> Editar
                            </button>
                            ${livro.disponivel ? 
                                `<button class="btn btn-sm btn-primary" onclick="app.quickEmprestimo(${livro.id})">
                                    <i class="fas fa-hand-holding"></i> Emprestar
                                </button>` : ''
                            }
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    clearSearchResults() {
        document.getElementById('searchResults').innerHTML = '';
        document.getElementById('searchTerm').value = '';
    }

    async quickEmprestimo(livroId) {
        // Switch to emprestimos tab and open modal with pre-selected livro
        this.switchTab('emprestimos');
        this.updateLivrosDisponiveis();
        this.openEmprestimoModal();
        document.getElementById('livroSelect').value = livroId;
    }

    // Utility Methods
    getStatusClass(status) {
        switch (status) {
            case 'ATIVO': return 'status-active';
            case 'DEVOLVIDO': return 'status-returned';
            case 'ATRASADO': return 'status-overdue';
            default: return 'status-active';
        }
    }

    getStatusText(status) {
        switch (status) {
            case 'ATIVO': return 'Ativo';
            case 'DEVOLVIDO': return 'Devolvido';
            case 'ATRASADO': return 'Atrasado';
            default: return 'Ativo';
        }
    }

    formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('pt-BR');
    }

    showLoading() {
        document.getElementById('loadingOverlay').style.display = 'block';
    }

    hideLoading() {
        document.getElementById('loadingOverlay').style.display = 'none';
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <i class="fas fa-${this.getToastIcon(type)}"></i>
            <span>${message}</span>
        `;
        
        container.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 5000);
    }

    getToastIcon(type) {
        switch (type) {
            case 'success': return 'check-circle';
            case 'error': return 'exclamation-circle';
            case 'warning': return 'exclamation-triangle';
            default: return 'info-circle';
        }
    }

    closeAllModals() {
        document.getElementById('livroModal').style.display = 'none';
        document.getElementById('emprestimoModal').style.display = 'none';
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    window.app = new BibliotecaApp();
});

// Add CSS for search results and book covers
const additionalCSS = `
    .search-header {
        text-align: center;
        margin-bottom: 30px;
        padding: 20px;
        background: #f7fafc;
        border-radius: 10px;
    }
    
    .results-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
        gap: 20px;
    }
    
    .result-card {
        background: white;
        border-radius: 10px;
        padding: 20px;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
        border: 1px solid #e2e8f0;
    }
    
    .result-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        margin-bottom: 15px;
    }
    
    .result-header h4 {
        color: #4a5568;
        font-size: 1.1rem;
        margin: 0;
        flex: 1;
        margin-right: 10px;
    }
    
    .result-content p {
        margin-bottom: 8px;
        font-size: 0.9rem;
        color: #6b7280;
    }
    
    .result-actions {
        margin-top: 15px;
        display: flex;
        gap: 10px;
        justify-content: flex-end;
    }
    
    /* Book cover styles */
    .livro-info {
        display: flex;
        align-items: center;
        gap: 12px;
    }
    
    .livro-capa-container {
        position: relative;
        width: 50px;
        height: 70px;
    }
    
    .livro-capa {
        width: 50px;
        height: 70px;
        object-fit: cover;
        border-radius: 4px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        border: 1px solid #e2e8f0;
        position: absolute;
        top: 0;
        left: 0;
    }
    
    .livro-capa-placeholder {
        width: 50px;
        height: 70px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        position: absolute;
        top: 0;
        left: 0;
    }
    
    .livro-detalhes {
        flex: 1;
        min-width: 0;
    }
    
    .livro-detalhes strong {
        display: block;
        color: #2d3748;
        font-size: 0.95rem;
        line-height: 1.3;
        margin-bottom: 0;
    }
    
    /* Modal book cover styles */
    .livro-capa-modal-container {
        margin-bottom: 15px;
        text-align: center;
    }
    
    .livro-capa-modal-container label {
        display: block;
        margin-bottom: 8px;
        font-weight: 500;
        color: #374151;
    }
    
    .livro-capa-modal {
        position: relative;
        width: 100px;
        height: 130px;
        margin: 0 auto;
    }
    
    .livro-capa-modal-img {
        width: 100px;
        height: 130px;
        object-fit: cover;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        border: 1px solid #e5e7eb;
    }
    
    .livro-capa-placeholder-modal {
        width: 100px;
        height: 130px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 30px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        border: 1px solid #e5e7eb;
        position: absolute;
        top: 0;
        left: 0;
    }
    
    /* Modal scroll and size fixes */
    .modal {
        max-height: 90vh;
        overflow-y: auto;
    }
    
    .modal-content {
        max-height: 85vh;
        overflow-y: auto;
    }
    
    .modal-body {
        max-height: 60vh;
        overflow-y: auto;
        padding: 20px;
    }
    
    /* Responsive adjustments */
    @media (max-width: 768px) {
        .livro-capa, .livro-capa-placeholder {
            width: 40px;
            height: 56px;
        }
        
        .livro-detalhes strong {
            font-size: 0.9rem;
        }
        
        .livro-capa-modal, .livro-capa-modal-img, .livro-capa-placeholder-modal {
            width: 80px;
            height: 100px;
        }
        
        .livro-capa-placeholder-modal {
            font-size: 24px;
        }
        
        .modal {
            max-height: 95vh;
        }
        
        .modal-content {
            max-height: 90vh;
        }
        
        .modal-body {
            max-height: 70vh;
            padding: 15px;
        }
    }
`;

// Inject additional CSS
const style = document.createElement('style');
style.textContent = additionalCSS;
document.head.appendChild(style);
