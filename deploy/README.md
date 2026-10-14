# Deploy numa VM Oracle Cloud (Always Free)

Stack completo (Postgres+pgvector, API, WireGuard) via Docker Compose numa
VM comum com root e systemd — nada de workarounds de host compartilhado.
**Não testei contra a VM real** — não tenho acesso a ela; os passos abaixo
são o roteiro para você rodar via SSH. Se algo não bater, me avise com o
erro exato para ajustarmos.

## 0. Criar a VM

No console da Oracle Cloud, "Always Free":

- **Forma**: Ampere A1 (ARM, até 4 OCPU/24GB de RAM no tier grátis) ou uma
  das VMs AMD micro (1 OCPU/1GB). Ampere A1 é bem mais confortável para
  rodar Postgres+API+WireGuard juntos.
- **Imagem**: Ubuntu (a mais comum/direta para isso). Se usar Oracle Linux,
  os passos de firewall mudam um pouco (`firewalld` em vez de `ufw`/
  `iptables` direto) — avise que eu ajusto.
- **Chave SSH**: deixe a Oracle gerar o par, ou suba sua chave pública.
- **IP**: reserve um "Reserved Public IP" (grátis no tier free) em vez de
  usar o IP efêmero padrão — assim `WIREGUARD_SERVER_URL` não muda se a VM
  reiniciar.

As imagens usadas (`eclipse-temurin`, `pgvector/pgvector`,
`linuxserver/wireguard`) publicam build para ARM64, então Ampere A1 deve
funcionar sem ajuste — não testado por mim, primeira coisa a confirmar se
algo não subir.

## 1. Abrir a porta do WireGuard — duas camadas de firewall

Oracle Cloud tem **dois** firewalls independentes; esquecer um dos dois é o
erro mais comum nessa plataforma:

1. **Security List / Network Security Group da VCN** (nível de rede,
   console da Oracle): adicionar regra de Ingress para UDP, porta 51820,
   origem `0.0.0.0/0`.
2. **Firewall do sistema operacional**, na própria VM:
   ```bash
   sudo ufw allow 51820/udp   # Ubuntu
   # ou, se for iptables direto:
   sudo iptables -A INPUT -p udp --dport 51820 -j ACCEPT
   ```

Sem as duas, a porta fica "aberta" no compose mas inacessível de fora.

## 2. Instalar Docker

```bash
ssh ubuntu@SEU_IP
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
# sair e logar de novo para o grupo docker valer
sudo systemctl enable --now docker   # crucial: é isso que faz os
                                      # containers voltarem sozinhos depois
                                      # de um reboot (restart: unless-stopped
                                      # só funciona com o serviço habilitado)
docker --version && docker compose version
```

## 3. Copiar o projeto

Na sua máquina:

```bash
cd D:/11F0/Sync/1Santiago11/CODE_PROJECTS/solaris-memory-assistent
scp -r backend deploy ubuntu@SEU_IP:~/solaris-memory-assistent/
```

(Ou `git clone` direto na VM, se preferir.)

## 4. Configurar segredos

Na VM:

```bash
cd ~/solaris-memory-assistent/deploy
cp .env.example .env
chmod 600 .env
./generate-password-hash.sh   # gera o APP_PASSWORD_HASH sem precisar de
                               # bcrypt/htpasswd instalado — a senha nunca
                               # aparece em `ps`, só pede e devolve o hash
nano .env   # preencher com os valores gerados — ver comentários no arquivo
```

**Nunca reusar os valores de dev do `backend/application.yml`** (são
públicos, estão no repositório). Guarde uma cópia do `APP_ENCRYPTION_KEY`
fora da VM — perder essa chave é perder o conteúdo de todas as memórias.

## 5. Subir o stack

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

## 6. Pegar a configuração do celular

```bash
docker compose exec wireguard cat /config/peer1/peer1.conf
# ou, para QR code (mais fácil de escanear no app):
docker compose exec wireguard /app/show-peer 1
```

## 7. Configurar o Android

1. Instalar o app **WireGuard** (Play Store/F-Droid).
2. Importar via QR code (passo 6) ou colar o conteúdo do `.conf`.
3. Ativar o túnel.
4. Com o túnel ativo, ajustar a URL do servidor em Ajustes no app para
   `http://172.28.0.3:8080` (IP fixo do `backend` na rede interna, definido
   em `docker-compose.yml`).

## Validado localmente, nesta sessão

Rodei o stack inteiro (Postgres+pgvector, backend, WireGuard) no Docker
Desktop aqui: os três containers sobem, o backend migra o banco
corretamente, o WireGuard ativa um túnel de verdade (gera chaves, QR code
nos logs), e o container do WireGuard alcança a API pela rede interna —
confirmando que a topologia funciona e que só a porta UDP fica exposta.
Isso usa Docker de verdade (não Podman/rootless), então deve valer
igualzinho na VM Oracle — mas a conexão de fato vindo da internet pública
até lá eu não testei, por não ter acesso à VM.

## O que ficou de fora (de propósito)

- **TLS/HTTPS**: o tráfego já vai cifrado ponta a ponta pelo WireGuard
  (igual a uma VPN comum) — adicionar TLS por cima da API é defesa em
  profundidade, não bloqueante para o MVP (ponto do documento, seção 7.1:
  "TLS quando aplicável").
- **Backups automáticos**: `deploy/backup.sh` existe mas não está
  agendado — rode via `cron` (`crontab -e` na VM) ou agende manualmente.
  Guarde os backups fora da VM (a Oracle não faz backup automático do
  disco no tier free).
- **Atualização de deploy**: por enquanto é manual (`scp`/`git pull` +
  `docker compose up -d --build` de novo). Um pipeline de CI/CD fica para
  depois.
