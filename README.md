# Zupbank Microservices

## O que é este repositório?

Repositório contendo o código para resolução do desafio proposto pela Zup Innovation. O desafio proposto é para a criação de um conjunto de APIs de suporte de um banco digital. As especificações passadas podem ser encontradas [nesse Trello público](https://trello.com/b/aiSaSAbi/nosso-banco-digital)

## Quais microserviços possui o ecossistema e o que cada um faz?

- **Proposal**: microserviço que irá realizar o cadastro e as devidas validações de uma proposta de conta corrente digital. Armazena todos os dados do cliente para posterior validação do banco. Caso o banco autorize, a conta é criada, passando a responsabilidade para o microserviço Account;
- **Account**: microserviço que será responsável pela criação da conta corrente de fato, assim como as operações vinculadas a mesma;
- **Transfer Worker**: microserviço que será responsável pelo recebimento de solicitação de transferências externas e suas devidas manipulações.

## Quais tecnologias são utilizadas em todo o ecossistema?
- Java 8
- Spring Framework
- MongoDB
- Keycloak
- Docker

## Como faço para ver a documentação da API?

Cada pasta de microserviço possui sua própria documentação baseada na especificação OpenAPI versão 3 (antiga Swagger API). Entretanto, a pasta raiz possui a documentação de todos os endpoints, independente do microserviço.

## Como faço para colocar em funcionamento ("subir") todo o ecossistema localmente?

O ecossistema dos microserviços utiliza o Docker e [Docker Compose](https://docs.docker.com/compose/) para subir todas as aplicações necessárias para o funcionamento do ecossistema.

Realize o clone deste repositório com o seguinte comando: 

`git clone https://github.com/joojano/zupbank-microservices.git`

Com o Docker e o Docker Compose instalados, execute o seguinte comando para subir as aplicações em uma máquina que possua arquitetura amd64:

`docker-compose -f docker-compose.amd64.yml up -d`

Caso a máquina esteja com arquitetura arm64, execute o seguinte comando:

`docker-compose -f docker-compose.arm64.yml up -d`

## Como faço para subir apenas um microserviço?

Em cada pasta de microserviço possui a documentação mostrando como subir apenas ele.
