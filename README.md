# Personal Cyber AI

Central pessoal de agentes de IA para cibersegurança defensiva. O projeto segue a ideia do repositório original: um orquestrador **CENTRAL** coordena seis agentes especializados:

- `VIRUS_GUARD` — triagem de vírus, malware, ransomware e arquivos suspeitos;
- `THREAT_ANALYST` — riscos, phishing, incidentes e indicadores;
- `WEB_SCOUT` — pesquisa defensiva de sites, domínios, links e fontes públicas;
- `CODE_GUARD` — revisão de código, dependências, configurações e segredos expostos;
- `FILE_GUARD` — triagem de arquivos e anexos sem executar conteúdo suspeito;
- `ACTION_AGENT` — planos de ação defensivos com aprovação humana.

## Estado atual

O repositório começou com apenas o README e a licença. Esta primeira versão adiciona uma aplicação web funcional, um backend HTTP em Python, roteamento entre agentes, fallback local e integração opcional com a API da OpenAI.

Sem `OPENAI_API_KEY`, o sistema permanece funcional em modo local. Com uma chave válida, o agente selecionado usa o modelo configurado no backend.

## Segurança

- Atua somente em sistemas, contas, arquivos e repositórios autorizados;
- Não executa comandos recebidos do usuário;
- Não ensina invasão, roubo de credenciais, malware ou evasão;
- Não guarda chaves no frontend;
- Ações externas exigem aprovação humana;
- O `ACTION_AGENT` apenas prepara a ação até existir confirmação;
- O Render recebe a chave por variável secreta, nunca pelo GitHub.

## Executar localmente

```bash
python server.py
```

Abra `http://localhost:10000` ou use a porta definida pela variável `PORT`.

Para ativar a OpenAI no backend, configure a variável de ambiente `OPENAI_API_KEY`. Opcionalmente, configure `OPENAI_MODEL`.

## API

- `GET /api/health` — saúde, provedor e quantidade de agentes;
- `GET /api/agents` — catálogo dos agentes;
- `POST /api/chat` — recebe `{ "message": "..." }` e retorna agente, resposta e necessidade de aprovação.

## Deploy no Render

O arquivo `render.yaml` está preparado para um Web Service Python. No Render:

1. selecione este repositório;
2. use o Blueprint ou configure o serviço web;
3. mantenha `OPENAI_API_KEY` como variável secreta;
4. não coloque a chave em arquivos do repositório;
5. valide `/api/health` após o deploy;
6. teste uma pergunta defensiva antes de usar qualquer ação.

## Limites desta primeira versão

A versão inicial não realiza varredura automática de dispositivos, não executa arquivos, não bloqueia ameaças sozinha e não publica alterações sem confirmação. Essas funções exigem conectores específicos, escopo autorizado e revisão adicional.
