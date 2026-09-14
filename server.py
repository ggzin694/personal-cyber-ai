from __future__ import annotations

import json
import os
import re
from datetime import datetime, timezone
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent
STATIC = ROOT / "static"
HOST = os.getenv("HOST", "0.0.0.0")
PORT = int(os.getenv("PORT", "10000"))
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY", "").strip()
OPENAI_MODEL = os.getenv("OPENAI_MODEL", "gpt-4o-mini").strip()
COMMAND_PREFIX = "•"

AGENTS = [
    {
        "id": "central",
        "name": "CENTRAL",
        "role": "Orquestrador principal",
        "description": "Entende o pedido, escolhe o agente adequado e organiza a resposta.",
        "keywords": [],
    },
    {
        "id": "virus_guard",
        "name": "VIRUS_GUARD",
        "role": "Triagem de malware",
        "description": "Ajuda a avaliar sinais de vírus, malware, ransomware e arquivos suspeitos.",
        "keywords": ["vírus", "virus", "malware", "trojan", "ransomware", "antivírus", "antivirus", "arquivo suspeito"],
    },
    {
        "id": "threat_analyst",
        "name": "THREAT_ANALYST",
        "role": "Análise de ameaças",
        "description": "Analisa riscos, phishing, incidentes, indicadores e medidas defensivas.",
        "keywords": ["ameaça", "ameaca", "threat", "phishing", "incidente", "risco", "ioc", "indicador", "ataque"],
    },
    {
        "id": "web_scout",
        "name": "WEB_SCOUT",
        "role": "Pesquisa defensiva na web",
        "description": "Organiza pesquisas sobre domínios, links, notícias e fontes públicas.",
        "keywords": ["site", "link", "url", "domínio", "dominio", "pesquisar", "pesquisa", "notícia", "noticia", "web"],
    },
    {
        "id": "code_guard",
        "name": "CODE_GUARD",
        "role": "Revisão segura de código",
        "description": "Procura falhas, segredos expostos, dependências vulneráveis e problemas de configuração.",
        "keywords": ["código", "codigo", "github", "programa", "api", "dependência", "dependencia", "bug", "python", "javascript", "servidor"],
    },
    {
        "id": "file_guard",
        "name": "FILE_GUARD",
        "role": "Proteção de arquivos",
        "description": "Orienta triagem de documentos e anexos sem executar conteúdo perigoso.",
        "keywords": ["arquivo", "anexo", "documento", "pdf", "zip", "download", "pasta", "documentação", "documentacao"],
    },
    {
        "id": "action_agent",
        "name": "ACTION_AGENT",
        "role": "Ações defensivas autorizadas",
        "description": "Transforma decisões em planos reversíveis e pede aprovação antes de agir.",
        "keywords": ["bloquear", "remover", "isolar", "executar", "workflow", "ação", "acao", "enviar", "publicar", "corrigir"],
    },
]

SAFE_SYSTEM = (
    "Você é o Personal Cyber AI, uma central pessoal de cibersegurança defensiva. "
    "Ajude somente em sistemas, contas, arquivos e repositórios autorizados pelo usuário. "
    "Não ensine invasão, roubo de credenciais, evasão, malware, exploração ofensiva ou dano. "
    "Priorize prevenção, triagem, correção, privacidade e confirmação humana. "
    "Não invente verificações: diferencie fato, hipótese e próximo teste. "
    "Responda em português do Brasil, com clareza e passos seguros."
)


def now() -> str:
    return datetime.now(timezone.utc).isoformat()


def agent_by_id(agent_id: str) -> dict:
    return next((a for a in AGENTS if a["id"] == agent_id), AGENTS[0])


def route_message(message: str) -> dict:
    text = message.casefold()
    # More specific agents win over the general orchestrator.
    matches = []
    for agent in AGENTS[1:]:
        score = sum(1 for word in agent["keywords"] if word in text)
        if score:
            matches.append((score, agent))
    if not matches:
        return AGENTS[0]
    matches.sort(key=lambda item: item[0], reverse=True)
    return matches[0][1]


def needs_approval(agent: dict, message: str) -> bool:
    if agent["id"] == "action_agent":
        return True
    return bool(re.search(r"\b(enviar|publicar|excluir|apagar|bloquear|isolar|comprar|alterar produção)\b", message.casefold()))


def normalize_command(message: str) -> tuple[str, bool]:
    """Remove the optional global mobile prefix while preserving the command signal."""
    text = message.strip()
    if text.startswith(COMMAND_PREFIX):
        return text[len(COMMAND_PREFIX):].strip(), True
    return text, False


def fallback_reply(message: str, agent: dict, provider_error: str | None = None) -> str:
    if agent["id"] == "central": 
        return (
            "Sou a CENTRAL do Personal Cyber AI. Posso encaminhar seu pedido para "
            "VIRUS_GUARD, THREAT_ANALYST, WEB_SCOUT, CODE_GUARD, FILE_GUARD ou ACTION_AGENT. "
            "Descreva o que precisa verificar em um sistema autorizado."
        )
    if agent["id"] == "action_agent":
        return (
            "Preparei a tarefa para o ACTION_AGENT. Antes de qualquer ação externa, "
            "preciso confirmar o alvo autorizado, o efeito esperado e uma forma de desfazer a mudança."
        )
    if provider_error == "http_429":
        return (
            f"A solicitação foi encaminhada para {agent['name']}. "
            "A OpenAI recusou esta tentativa por limite de uso, créditos ou cobrança da API. "
            "O fallback local continua ativo e nenhuma ação externa foi executada."
        )
    if provider_error == "network_error":
        return (
            f"A solicitação foi encaminhada para {agent['name']}, mas o provedor de IA não respondeu. "
            "O fallback local continua ativo e nenhuma ação externa foi executada."
        )
    if provider_error == "missing_api_key":
        return (
            f"A solicitação foi encaminhada para {agent['name']}. "
            "A chave OpenAI não está configurada; o fallback local continua ativo."
        )
    return (
        f"A solicitação foi encaminhada para {agent['name']}. "
        "O fallback local registrou a triagem e indicou o próximo passo seguro."
    )


def openai_reply(message: str, agent: dict) -> tuple[str | None, str | None]:
    if not OPENAI_API_KEY:
        return None, "missing_api_key"
    prompt = (
        f"Agente selecionado: {agent['name']} — {agent['role']}.\n"
        f"Descrição: {agent['description']}\n\nPedido do usuário:\n{message}"
    )
    payload = {
        "model": OPENAI_MODEL,
        "messages": [
            {"role": "system", "content": SAFE_SYSTEM},
            {"role": "user", "content": prompt},
        ],
        "temperature": 0.2,
        "max_tokens": 1200,
    }
    request = Request(
        "https://api.openai.com/v1/chat/completions",
        data=json.dumps(payload, ensure_ascii=False).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "Authorization": f"Bearer {OPENAI_API_KEY}",
        },
        method="POST",
    )
    try:
        with urlopen(request, timeout=45) as response:
            result = json.loads(response.read().decode("utf-8"))
        text = result.get("choices", [{}])[0].get("message", {}).get("content", "").strip()
        return (text or None), None if text else "empty_response"
    except HTTPError as exc:
        return None, f"http_{exc.code}"
    except (URLError, TimeoutError):
        return None, "network_error"
    except (OSError, json.JSONDecodeError, KeyError, IndexError, TypeError):
        return None, "invalid_response"


def json_bytes(payload: object) -> bytes:
    return json.dumps(payload, ensure_ascii=False).encode("utf-8")


class Handler(BaseHTTPRequestHandler):
    server_version = "PersonalCyberAI/0.1"

    def log_message(self, fmt: str, *args) -> None:
        print(f"[{datetime.now().isoformat(timespec='seconds')}] {fmt % args}")

    def send_json(self, payload: object, status: int = 200) -> None:
        data = json_bytes(payload)
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("Access-Control-Allow-Origin", "*")
        self.end_headers()
        self.wfile.write(data)

    def send_file(self, path: Path, content_type: str) -> None:
        if not path.is_file() or ROOT not in path.resolve().parents:
            self.send_error(404)
            return
        data = path.read_bytes()
        self.send_response(200)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def body(self) -> dict:
        length = int(self.headers.get("Content-Length", "0"))
        if length > 64 * 1024:
            raise ValueError("payload too large")
        raw = self.rfile.read(length)
        return json.loads(raw.decode("utf-8") or "{}")

    def do_OPTIONS(self) -> None:
        self.send_response(204)
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Headers", "Content-Type")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
        self.end_headers()

    def do_GET(self) -> None:
        path = urlparse(self.path).path
        if path == "/api/health":
            self.send_json({
                "ok": True,
                "service": "personal-cyber-ai",
                "version": "0.1",
                "provider": "openai" if OPENAI_API_KEY else "local_fallback",
                "model": OPENAI_MODEL if OPENAI_API_KEY else None,
                "agents": len(AGENTS),
                "command_prefix": COMMAND_PREFIX,
                "time": now(),
            })
        elif path == "/api/agents":
            self.send_json({"agents": [{k: v for k, v in a.items() if k != "keywords"} for a in AGENTS]})
        elif path in ("/", "/index.html"):
            self.send_file(STATIC / "index.html", "text/html; charset=utf-8")
        elif path == "/manifest.webmanifest":
            self.send_file(ROOT / "manifest.webmanifest", "application/manifest+json; charset=utf-8")
        elif path == "/sw.js":
            self.send_file(STATIC / "sw.js", "text/javascript; charset=utf-8")
        elif path.startswith("/static/"): 
            relative = Path(path.removeprefix("/static/"))
            if ".." in relative.parts:
                self.send_error(404)
                return
            types = {".css": "text/css; charset=utf-8", ".js": "text/javascript; charset=utf-8", ".png": "image/png", ".jpg": "image/jpeg"}
            self.send_file(STATIC / relative, types.get(relative.suffix, "application/octet-stream"))
        else:
            self.send_error(404)

    def do_POST(self) -> None:
        if urlparse(self.path).path != "/api/chat":
            self.send_error(404)
            return
        try:
            payload = self.body()
            raw_message = str(payload.get("message", "")).strip()
            message, prefixed = normalize_command(raw_message)
            if not message or len(message) > 5000:
                self.send_json({"error": "message_invalid"}, 400)
                return
            agent = route_message(message)
            requires_approval = needs_approval(agent, message)
            answer, provider_error = openai_reply(message, agent)
            answer = answer or fallback_reply(message, agent, provider_error)
            self.send_json({
                "status": "completed",
                "agent": {k: v for k, v in agent.items() if k != "keywords"},
                "provider": "openai" if provider_error is None and OPENAI_API_KEY else "local_fallback",
                "provider_error": provider_error,
                "command_prefix": COMMAND_PREFIX if prefixed else None,
                "requires_approval": requires_approval,
                "message": answer,
            })
        except (ValueError, json.JSONDecodeError):
            self.send_json({"error": "invalid_json"}, 400)


if __name__ == "__main__":
    print(f"Personal Cyber AI running at http://{HOST}:{PORT}")
    ThreadingHTTPServer((HOST, PORT), Handler).serve_forever()
