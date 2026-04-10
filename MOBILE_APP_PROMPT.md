# Prompt Para Criar O App Mobile

Você vai criar um aplicativo mobile em `React Native + Expo` para consumir a API deste projeto backend.

Antes de começar:

1. Leia este arquivo inteiro.
2. Considere que eu ainda vou te mostrar uma referência visual de tela.
3. Use a documentação Swagger local para confirmar contratos e payloads:
   - `http://localhost:8080/swagger-ui/index.html`
   - `http://localhost:8080/v3/api-docs`
4. Trabalhe de forma prática: implemente o app, rode, valide e me explique como executar.

## Objetivo

Criar um app mobile funcional e bem organizado para consumir esta API de automação predial IoT.

O app deve ter, no mínimo:

1. Tela de login com CPF e senha.
2. Persistência do token JWT.
3. Fluxo autenticado.
4. Dashboard inicial consumindo dados reais da API.
5. Navegação entre telas.
6. Camada de serviços para API bem separada.
7. Tratamento de loading, erro e estado vazio.
8. Estrutura pronta para crescer.

## Stack Obrigatória

Use:

1. `Expo`
2. `TypeScript`
3. `Expo Router` ou `React Navigation`
4. `Axios`
5. `@tanstack/react-query`
6. `react-hook-form`
7. `zod`
8. Armazenamento seguro para token:
   - preferencialmente `expo-secure-store`

Pode usar uma biblioteca de UI se isso acelerar, mas sem exagerar na complexidade. O visual deve ficar limpo, moderno e consistente.

## Estrutura Esperada

Crie o app em uma pasta nova dentro deste workspace, de preferência:

- `./mobile-app`

Organize algo próximo disso:

```txt
mobile-app/
  app/                  ou src/
  components/
  features/
  services/
  hooks/
  store/
  constants/
  types/
  utils/
  assets/
```

## Contexto Da API

API local:

- Base atual no backend: `http://localhost:8080`

Importante para Expo:

`localhost` no celular/emulador nem sempre aponta para o backend no host.

Implemente uma configuração de base URL que seja fácil de trocar, por exemplo:

1. Android emulator: `http://10.0.2.2:8080`
2. iOS simulator: `http://localhost:8080`
3. Dispositivo físico: usar o IP local da máquina, por exemplo `http://192.168.x.x:8080`

Crie isso em um arquivo de configuração simples, com instruções claras para troca.

## Autenticação

Existe endpoint público:

- `POST /api/auth/login`

Payload:

```json
{
  "cpf": "string",
  "senha": "string"
}
```

Resposta:

```json
{
  "message": "string",
  "token": "string",
  "user": {
    "id": 1,
    "cpf": "string",
    "nome": "string",
    "ativo": true,
    "roles": ["string"],
    "unidades": ["string"]
  }
}
```

Depois do login, a API usa header:

```txt
Authorization: Bearer <token>
```

Quase todos os outros endpoints exigem autenticação.

## Domínio Principal Da API

Os recursos mais importantes para o app neste momento são:

1. `Blocos`
2. `Salas`
3. `Luzes`

Endpoints úteis já identificados:

### Blocos

- `GET /api/blocos`
- `GET /api/blocos/{id}`
- `POST /api/blocos`
- `PUT /api/blocos/{id}`
- `PATCH /api/blocos/{id}/status`
- `GET /api/blocos/ativos`
- `GET /api/blocos/status/{status}`

### Salas

- `GET /api/salas`
- `GET /api/salas/{id}`
- `POST /api/salas`
- `PUT /api/salas/{id}`
- `DELETE /api/salas/{id}`
- `PATCH /api/salas/{id}/status`
- `GET /api/salas/bloco/{blocoId}`
- `GET /api/salas/livres`
- `GET /api/salas/filtros`
- `GET /api/salas/buscar?nome=...`

### Luzes

- `GET /api/luzes/{id}`
- `POST /api/luzes`
- `PUT /api/luzes/{id}`
- `DELETE /api/luzes/{id}`
- `PATCH /api/luzes/{id}/ligar`
- `PATCH /api/luzes/{id}/desligar`
- `PATCH /api/luzes/bloco/{blocoId}/ligar-todas`
- `PATCH /api/luzes/bloco/{blocoId}/desligar-todas`

## Sugestão De Fluxo Inicial Do App

Implemente primeiro um MVP útil:

1. `Login`
2. `Home/Dashboard`
3. `Lista de blocos`
4. `Detalhe do bloco`
5. `Lista de salas do bloco`
6. `Ações rápidas para luzes`

### Tela de Login

Requisitos:

1. Campo CPF com máscara amigável, mas envie sem quebrar o contrato da API.
2. Campo senha.
3. Validação com `zod`.
4. Exibir erro de autenticação de forma clara.
5. Salvar token e dados básicos do usuário.

### Dashboard

Pode mostrar:

1. Nome do usuário logado.
2. Quantidade de blocos ativos.
3. Atalhos para blocos, salas e luzes.
4. Estado de carregamento.

### Blocos

Na lista, mostrar pelo menos:

1. Nome
2. Descrição
3. Status

No detalhe do bloco:

1. Dados do bloco
2. Lista de salas do bloco
3. Ações relacionadas às luzes do bloco, se fizer sentido

### Salas

Mostrar:

1. Nome
2. Capacidade
3. Status
4. Tipo
5. Bloco

### Luzes

Se houver tela ou seção de controle:

1. Permitir ligar/desligar uma luz
2. Permitir ligar/desligar todas de um bloco quando fizer sentido
3. Atualizar a UI após ação com invalidação de cache do React Query

## Regras De Implementação

1. Não fazer tudo em uma tela só.
2. Separar `api client`, `services`, `hooks` e `screens/routes`.
3. Centralizar interceptors do Axios.
4. Injetar token automaticamente nas requisições autenticadas.
5. Tratar `401` para redirecionar ao login quando necessário.
6. Criar tipagens TypeScript para os principais DTOs.
7. Usar React Query para cache, refetch e mutation.
8. Evitar código improvisado ou repetido.

## UX E Qualidade

Quero um app com cara de produto real, não um protótipo desleixado.

Então:

1. Crie um tema consistente.
2. Capriche em espaçamento, tipografia e hierarquia visual.
3. Faça loading states decentes.
4. Faça empty states decentes.
5. Mostre mensagens de erro úteis.
6. Funcione bem em Android e iPhone.

Quando eu te mandar a imagem de referência da tela, adapte o visual a ela.

## Integração Com Swagger

Use a documentação Swagger para validar:

1. nomes de campos
2. enums
3. formatos de payload
4. respostas reais
5. endpoints adicionais que sejam úteis

Se encontrar inconsistência entre Swagger e comportamento real da API, me avise e siga a implementação mais robusta.

## Entregáveis

Ao final, quero que você:

1. Tenha criado o projeto Expo.
2. Tenha implementado o fluxo de login.
3. Tenha implementado pelo menos o fluxo principal autenticado.
4. Tenha deixado a base URL configurável.
5. Tenha documentado como rodar.
6. Tenha listado dependências instaladas.
7. Tenha me dito quais arquivos principais foram criados/editados.

## Comandos E Validação

Depois de implementar:

1. rode instalação de dependências
2. valide TypeScript/lint se estiver configurado
3. suba o app com Expo
4. me diga exatamente como testar

## Observações Importantes

1. O backend está local e protegido com JWT.
2. Swagger está disponível em `http://localhost:8080/swagger-ui/index.html`.
3. OpenAPI JSON está em `http://localhost:8080/v3/api-docs`.
4. Se precisar, analise o backend local para entender respostas e regras.
5. Se houver decisão de arquitetura relevante, escolha a opção mais simples e sustentável.

## Resultado Esperado

Quero que você aja como um desenvolvedor sênior implementando de verdade, e não apenas sugerindo.

Comece criando o projeto Expo e a estrutura base, depois implemente autenticação e o fluxo principal de blocos/salas/luzes consumindo a API real.
