(() => {
    'use strict';

    const MOEDA = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
    const DATA = new Intl.DateTimeFormat('pt-BR', {
        day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit'
    });

    const ROTULO_SERVICO = {
        BANHO: 'Banho',
        TOSA: 'Tosa',
        BANHO_E_TOSA: 'Banho + Tosa'
    };

    const estado = {
        clientes: [],
        pets: [],
        agendamentos: [],
        filtroStatus: 'TODOS'
    };

    const $ = (id) => document.getElementById(id);

    async function api(caminho, opcoes = {}) {
        const resposta = await fetch(caminho, {
            headers: { 'Content-Type': 'application/json' },
            ...opcoes
        });

        if (!resposta.ok) {
            let mensagem = `Falha na requisição (HTTP ${resposta.status}).`;
            try {
                const corpo = await resposta.json();
                if (corpo && corpo.mensagem) {
                    mensagem = corpo.mensagem;
                }
            } catch {
            }
            throw new Error(mensagem);
        }

        return resposta.status === 204 ? null : resposta.json();
    }

    async function carregarTudo() {
        const [clientes, pets, agendamentos] = await Promise.all([
            api('/clientes'),
            api('/pets'),
            api('/agendamentos')
        ]);
        estado.clientes = clientes;
        estado.pets = pets;
        estado.agendamentos = agendamentos;
        desenharTudo();
    }

    const moeda = (valor) => MOEDA.format(Number(valor ?? 0));
    const dataHora = (iso) => (iso ? DATA.format(new Date(iso)) : '—');
    const peso = (kg) => `${Number(kg).toLocaleString('pt-BR', { maximumFractionDigits: 2 })} kg`;
    const servico = (tipo) => ROTULO_SERVICO[tipo] ?? tipo;

    function esc(texto) {
        return String(texto ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;');
    }

    function desenharTudo() {
        desenharResumo();
        desenharBarras();
        desenharAgendamentos();
        desenharClientes();
        desenharPets();
        preencherSelects();
        atualizarPrevia();
    }

    function desenharResumo() {
        const porStatus = (status) => estado.agendamentos.filter((a) => a.status === status);

        const agendados = porStatus('AGENDADO');
        const concluidos = porStatus('CONCLUIDO');
        const cancelados = porStatus('CANCELADO');

        const total = [...agendados, ...concluidos]
            .reduce((soma, a) => soma + Number(a.valor ?? 0), 0);

        $('faturamentoTotal').textContent = moeda(total);
        $('totalAgendados').textContent = agendados.length;
        $('totalConcluidos').textContent = concluidos.length;
        $('totalCancelados').textContent = cancelados.length;

        const ativos = agendados.length + concluidos.length;
        $('resumoNota').textContent = ativos === 0
            ? 'Nenhum agendamento ativo'
            : `${ativos} agendamento${ativos > 1 ? 's' : ''} ativo${ativos > 1 ? 's' : ''} · cancelados não entram na soma`;
    }

    function desenharBarras() {
        const lista = $('barrasServicos');
        const ativos = estado.agendamentos.filter((a) => a.status !== 'CANCELADO');

        if (ativos.length === 0) {
            lista.innerHTML = '<li class="vazio">Sem dados ainda.</li>';
            return;
        }

        const totais = new Map();
        for (const agendamento of ativos) {
            const atual = totais.get(agendamento.tipoServico) ?? { quantidade: 0, valor: 0 };
            atual.quantidade += 1;
            atual.valor += Number(agendamento.valor ?? 0);
            totais.set(agendamento.tipoServico, atual);
        }

        const ordenados = [...totais.entries()].sort((a, b) => b[1].valor - a[1].valor);
        const maior = ordenados[0][1].valor || 1;

        lista.innerHTML = ordenados.map(([tipo, dados]) => `
            <li class="barra">
                <div class="barra__topo">
                    <span class="barra__nome">${esc(servico(tipo))}</span>
                    <span class="barra__numeros">
                        <span class="barra__valor">${moeda(dados.valor)}</span>
                        &nbsp;·&nbsp;${dados.quantidade}x
                    </span>
                </div>
                <div class="barra__trilho">
                    <div class="barra__preenchimento" style="width: ${(dados.valor / maior) * 100}%"></div>
                </div>
            </li>
        `).join('');
    }

    function desenharAgendamentos() {
        const corpo = $('tabelaAgendamentos');
        const lista = estado.filtroStatus === 'TODOS'
            ? estado.agendamentos
            : estado.agendamentos.filter((a) => a.status === estado.filtroStatus);

        if (lista.length === 0) {
            corpo.innerHTML = `<tr><td colspan="7" class="vazio">${
                estado.agendamentos.length === 0
                    ? 'Nenhum agendamento ainda.'
                    : 'Nenhum agendamento com esse status.'
            }</td></tr>`;
            return;
        }

        const ordenados = [...lista].sort((a, b) => new Date(b.dataHora) - new Date(a.dataHora));

        corpo.innerHTML = ordenados.map((a) => `
            <tr>
                <td class="celula-forte">${esc(a.pet?.nome)}</td>
                <td class="celula-suave">${esc(a.cliente?.nome)}</td>
                <td>${esc(servico(a.tipoServico))}</td>
                <td class="celula-suave">${dataHora(a.dataHora)}</td>
                <td class="num">${moeda(a.valor)}</td>
                <td><span class="badge badge--${a.status.toLowerCase()}">${esc(a.status)}</span></td>
                <td>
                    <div class="acoes">${
                        a.status === 'AGENDADO'
                            ? `<button class="btn--acao" data-concluir="${a.id}">concluir</button>
                               <button class="btn--acao perigo" data-cancelar="${a.id}">cancelar</button>`
                            : ''
                    }</div>
                </td>
            </tr>
        `).join('');
    }

    function desenharClientes() {
        const corpo = $('tabelaClientes');
        $('contadorClientes').textContent = estado.clientes.length;

        if (estado.clientes.length === 0) {
            corpo.innerHTML = '<tr><td colspan="2" class="vazio">Nenhum cliente ainda.</td></tr>';
            return;
        }

        corpo.innerHTML = estado.clientes.map((c) => `
            <tr>
                <td class="celula-forte">${esc(c.nome)}</td>
                <td class="celula-suave">${esc(c.telefone)}<br>${esc(c.email)}</td>
            </tr>
        `).join('');
    }

    function desenharPets() {
        const corpo = $('tabelaPets');
        $('contadorPets').textContent = estado.pets.length;

        if (estado.pets.length === 0) {
            corpo.innerHTML = '<tr><td colspan="4" class="vazio">Nenhum pet ainda.</td></tr>';
            return;
        }

        corpo.innerHTML = estado.pets.map((p) => `
            <tr>
                <td class="celula-forte">${esc(p.nome)}<br><span class="celula-suave">${esc(p.especie)}</span></td>
                <td class="celula-suave">${esc(p.tutor?.nome)}</td>
                <td class="num">${peso(p.pesoKg)}</td>
                <td class="num"><button class="btn--acao perigo" data-excluir-pet="${p.id}">excluir</button></td>
            </tr>
        `).join('');
    }

    function preencherSelects() {
        const selectTutor = $('selectTutor');
        const selectPet = $('selectPet');
        const tutorEscolhido = selectTutor.value;
        const petEscolhido = selectPet.value;

        selectTutor.innerHTML = estado.clientes.length === 0
            ? '<option value="">Cadastre um cliente primeiro</option>'
            : estado.clientes.map((c) => `<option value="${c.id}">${esc(c.nome)}</option>`).join('');

        selectPet.innerHTML = estado.pets.length === 0
            ? '<option value="">Cadastre um pet primeiro</option>'
            : estado.pets.map((p) =>
                `<option value="${p.id}">${esc(p.nome)} — ${esc(p.tutor?.nome)}</option>`).join('');

        if (tutorEscolhido) selectTutor.value = tutorEscolhido;
        if (petEscolhido) selectPet.value = petEscolhido;
    }

    function atualizarPrevia() {
        const previa = $('previaPreco');
        const pet = estado.pets.find((p) => String(p.id) === $('selectPet').value);

        if (!pet) {
            previa.textContent = 'Selecione um pet para ver a faixa de preço.';
            return;
        }

        const faixa = pet.pesoKg <= 10 ? 'até 10 kg' : 'acima de 10 kg';
        previa.innerHTML =
            `<strong>${esc(pet.nome)}</strong> tem ${peso(pet.pesoKg)} — faixa <strong>${faixa}</strong>. ` +
            'O valor final é calculado pelo servidor ao confirmar.';
    }

    function avisar(mensagem, tipo = 'ok') {
        const caixa = document.createElement('div');
        caixa.className = `aviso aviso--${tipo}`;
        caixa.textContent = mensagem;
        $('avisos').append(caixa);
        setTimeout(() => caixa.remove(), 4500);
    }

    function definirStatusConexao(ok) {
        const elemento = $('statusConexao');
        elemento.className = `status ${ok ? 'status--ok' : 'status--erro'}`;
        elemento.querySelector('.status__label').textContent = ok ? 'conectado' : 'sem conexão';
    }

    async function executar(acao, mensagemSucesso, botao) {
        if (botao) botao.disabled = true;
        try {
            await acao();
            await carregarTudo();
            definirStatusConexao(true);
            avisar(mensagemSucesso, 'ok');
            return true;
        } catch (erro) {
            avisar(erro.message, 'erro');
            return false;
        } finally {
            if (botao) botao.disabled = false;
        }
    }

    function ligarFormularios() {
        $('formCliente').addEventListener('submit', async (evento) => {
            evento.preventDefault();
            const form = evento.currentTarget;
            const dados = Object.fromEntries(new FormData(form));

            const ok = await executar(
                () => api('/clientes', { method: 'POST', body: JSON.stringify(dados) }),
                `Cliente ${dados.nome} cadastrado.`,
                form.querySelector('button[type=submit]')
            );
            if (ok) form.reset();
        });

        $('formPet').addEventListener('submit', async (evento) => {
            evento.preventDefault();
            const form = evento.currentTarget;
            const dados = Object.fromEntries(new FormData(form));

            const corpo = {
                nome: dados.nome,
                especie: dados.especie,
                raca: dados.raca || null,
                pesoKg: Number(dados.pesoKg),
                clienteId: Number(dados.clienteId)
            };

            const ok = await executar(
                () => api('/pets', { method: 'POST', body: JSON.stringify(corpo) }),
                `Pet ${corpo.nome} cadastrado.`,
                form.querySelector('button[type=submit]')
            );
            if (ok) form.reset();
        });

        $('formAgendamento').addEventListener('submit', async (evento) => {
            evento.preventDefault();
            const form = evento.currentTarget;
            const dados = Object.fromEntries(new FormData(form));

            const pet = estado.pets.find((p) => String(p.id) === dados.petId);
            if (!pet) {
                avisar('Escolha um pet para agendar.', 'erro');
                return;
            }

            const corpo = {
                clienteId: pet.tutor.id,
                petId: Number(dados.petId),
                tipoServico: dados.tipoServico,
                dataHora: dados.dataHora
            };

            const ok = await executar(
                () => api('/agendamentos', { method: 'POST', body: JSON.stringify(corpo) }),
                `Agendamento de ${pet.nome} criado.`,
                form.querySelector('button[type=submit]')
            );
            if (ok) {
                form.reset();
                definirDataMinima();
            }
        });

        $('selectPet').addEventListener('change', atualizarPrevia);
    }

    function ligarAbas() {
        for (const aba of document.querySelectorAll('.aba')) {
            aba.addEventListener('click', () => {
                for (const outra of document.querySelectorAll('.aba')) {
                    outra.classList.toggle('aba--ativa', outra === aba);
                }
                for (const form of document.querySelectorAll('.form')) {
                    form.classList.toggle('oculto', form.dataset.painel !== aba.dataset.aba);
                }
            });
        }
    }

    function ligarFiltros() {
        for (const chip of document.querySelectorAll('#filtrosStatus .chip')) {
            chip.addEventListener('click', () => {
                estado.filtroStatus = chip.dataset.status;
                for (const outro of document.querySelectorAll('#filtrosStatus .chip')) {
                    outro.classList.toggle('chip--ativo', outro === chip);
                }
                desenharAgendamentos();
            });
        }
    }

    function ligarAcoesDasTabelas() {
        document.addEventListener('click', (evento) => {
            const botao = evento.target.closest('button');
            if (!botao) return;

            const { concluir, cancelar, excluirPet } = botao.dataset;

            if (concluir) {
                executar(
                    () => api(`/agendamentos/${concluir}/concluir`, { method: 'PATCH' }),
                    'Agendamento concluído.', botao);
            } else if (cancelar) {
                executar(
                    () => api(`/agendamentos/${cancelar}/cancelar`, { method: 'PATCH' }),
                    'Agendamento cancelado.', botao);
            } else if (excluirPet) {
                if (confirm('Excluir este pet? Os agendamentos dele impedem a exclusão.')) {
                    executar(
                        () => api(`/pets/${excluirPet}`, { method: 'DELETE' }),
                        'Pet excluído.', botao);
                }
            }
        });
    }

    function ligarAjuda() {
        const dialogo = $('dialogoAjuda');
        $('btnAjuda').addEventListener('click', () => dialogo.showModal());
        $('btnFecharAjuda').addEventListener('click', () => dialogo.close());
    }

    function definirDataMinima() {
        const agora = new Date(Date.now() - new Date().getTimezoneOffset() * 60000);
        $('inputDataHora').min = agora.toISOString().slice(0, 16);
    }

    async function iniciar() {
        ligarAbas();
        ligarFiltros();
        ligarFormularios();
        ligarAcoesDasTabelas();
        ligarAjuda();
        definirDataMinima();

        try {
            await carregarTudo();
            definirStatusConexao(true);
        } catch (erro) {
            definirStatusConexao(false);
            avisar(`Não foi possível falar com a API: ${erro.message}`, 'erro');
        }
    }

    document.addEventListener('DOMContentLoaded', iniciar);
})();

