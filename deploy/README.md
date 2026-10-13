# Deploy na VM (falcon.whatbox.ca)

Isso roda o stack inteiro (Postgres+pgvector, API, WireGuard) via Docker
Compose. **Não testei contra a VM real** — não tenho acesso a ela; os passos
abaixo são o roteiro para você rodar via SSH. Se algo não bater, me avise
com o erro exato para ajustarmos.

Confirmado nesta sessão: `falcon.whatbox.ca` resolve para um IP público real
(`72.21.17.54`), não está atrás de CGNAT — WireGuard direto funciona, sem
precisar de Tailscale/relay.

**Validado localmente** (Docker Desktop, não a VM): os três containers
sobem, o backend conecta no Postgres e roda as migrações, o WireGuard gera
chaves e ativa o túnel de verdade (inclusive o QR code do peer aparece nos
logs), e o container do WireGuard consegue alcançar a API pelo IP interno
fixo — confirmando que a topologia de rede funciona e que só a porta UDP
do WireGuard fica exposta (Postgres e API não têm `ports:` publicado). O
que não dá pra validar sem a VM real: a conexão de fato vindo da internet
até `falcon.whatbox.ca:51820`.

## 1. Pré-requisitos na VM

```bash
ssh seu-usuario@falcon.whatbox.ca
docker --version && docker compose version   # instala Docker se não tiver
```

Se a VM não tiver Docker: siga `https://docs.docker.com/engine/install/` para
a distro dela (normalmente `curl -fsSL https://get.docker.com | sh`).

## 2. Copiar o projeto

Na sua máquina:

```bash
cd D:/11F0/Sync/1Santiago11/CODE_PROJECTS/solaris-memory-assistent
scp -r backend deploy seu-usuario@falcon.whatbox.ca:~/solaris-memory-assistent/
```

(Ou `git clone` direto na VM, se o repositório estiver em algum remoto.)

## 3. Configurar segredos

Na VM:

```bash
cd ~/solaris-memory-assistent/deploy
cp .env.example .env
nano .env   # preencher com valores reais — ver comentários no próprio arquivo
```

**Nunca reusar os valores de dev do `backend/application.yml`** (são
públicos, estão no repositório).

## 4. Subir o stack

```bash
docker compose up -d --build
docker compose ps        # os três serviços devem estar "Up"
docker compose logs backend --tail 50   # confirma que subiu sem erro
```

Teste local na própria VM (ainda sem VPN, só para validar que o backend
está de pé):

```bash
curl http://localhost:8080/api/v1/health   # deve falhar — API não está publicada na porta do host, só na rede interna
docker compose exec backend wget -qO- http://localhost:8080/api/v1/health   # este sim deve funcionar
```

## 5. Pegar a configuração do celular

O container `wireguard` gera automaticamente a config do primeiro peer:

```bash
docker compose exec wireguard cat /config/peer1/peer1.conf
# ou, se preferir QR code para escanear no app:
docker compose exec wireguard /app/show-peer 1
```

## 6. Configurar o Android

1. Instalar o app **WireGuard** (Play Store/F-Droid).
2. Importar via QR code (passo 5) ou colar o conteúdo do `.conf`.
3. Ativar o túnel.
4. Testar: com o túnel ativo, o app solaris-memory-assistent deve
   conseguir falar com `http://172.28.0.3:8080` (IP fixo do `backend` nessa
   rede, definido em `docker-compose.yml`) — ajustar a URL do servidor em
   Ajustes no app para esse endereço.

## O que ficou de fora (de propósito)

- **TLS/HTTPS**: o tráfego já vai cifrado ponta a ponta pelo WireGuard
  (igual a uma VPN comum) — adicionar TLS por cima da API é defesa em
  profundidade, não bloqueante para o MVP (ponto do documento, seção 7.1:
  "TLS quando aplicável").
- **Backups automáticos**: ver `deploy/backup.sh` e o README principal —
  rodar via cron na VM, não está agendado automaticamente ainda.
- **Atualização de deploy**: por enquanto é manual (`scp` + `docker compose
  up -d --build` de novo). Um pipeline de CI/CD fica para depois.
