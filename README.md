# API Biblioteca IFMS — Atividade 3

Atividade em Java 21 com Spring Boot, Spring Web MVC, Spring Data JPA, Lombok e H2.
O repositório reúne dois projetos Spring independentes, conforme o enunciado.

| Aplicação | Pasta a partir da raiz do repositório | Porta | Funcionalidades |
| --- | --- | --- | --- |
| Leitor | `leitor/leitor` | 8080 | Cadastrar, listar, buscar por ID, atualizar e excluir |
| Empréstimo | `emprestimo` | 8081 | Cadastrar e listar |

## Executar no VS Code

Abra a pasta `API-biblioteca-IFMS` no VS Code. É necessário ter o JDK 21 instalado.
O Maven Wrapper (`mvnw.cmd`) acompanha cada projeto; na primeira execução ele pode baixar dependências.

Em um terminal PowerShell aberto na raiz do repositório, execute a aplicação Leitor:

```powershell
cd .\leitor\leitor
.\mvnw.cmd spring-boot:run
```

Em outro terminal PowerShell, também aberto na raiz do repositório, execute Empréstimo:

```powershell
cd .\emprestimo
.\mvnw.cmd spring-boot:run
```

Aguarde a mensagem `Started LeitorApplication` ou `Started EmprestimoApplication`.
Mantenha o terminal da aplicação aberto enquanto usar a API. Para encerrá-la, pressione `Ctrl+C`.
Cada aplicação pode funcionar sozinha; as portas diferentes também permitem executar ambas ao mesmo tempo.

## Endpoints

| Aplicação | Método | Caminho | Resposta esperada |
| --- | --- | --- | --- |
| Leitor | POST | `/leitores` | 201 Created |
| Leitor | GET | `/leitores` | 200 OK |
| Leitor | GET | `/leitores/{id}` | 200 OK ou 404 Not Found |
| Leitor | PUT | `/leitores/{id}` | 200 OK ou 404 Not Found |
| Leitor | DELETE | `/leitores/{id}` | 204 No Content ou 404 Not Found |
| Empréstimo | POST | `/emprestimos` | 201 Created |
| Empréstimo | GET | `/emprestimos` | 200 OK |

O cadastro de leitor recebe `nome` e `email`. O cadastro de empréstimo recebe `livroId`,
`leitorId` e `dataEmprestimo`. Os IDs dos registros são gerados pelo banco.
Em empréstimos, `livroId` e `leitorId` são valores numéricos (`Long`), conforme o enunciado;
esta atividade não inclui consulta a outra aplicação para validar esses valores.

### Exemplo: cadastrar e listar empréstimos

Com Empréstimo em execução, abra outro terminal PowerShell e execute:

```powershell
$corpo = @{
    livroId = 1
    leitorId = 1
    dataEmprestimo = '2026-10-15'
} | ConvertTo-Json

$cadastro = Invoke-WebRequest -UseBasicParsing `
    -Uri 'http://localhost:8081/emprestimos' `
    -Method Post -ContentType 'application/json' -Body $corpo

$cadastro.StatusCode  # esperado: 201
$cadastro.Content

$lista = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:8081/emprestimos'
$lista.StatusCode  # esperado: 200
$lista.Content
```

A data usa o formato `AAAA-MM-DD`. Cada POST cria um novo registro no banco.
Também é possível abrir a listagem no navegador em <http://localhost:8081/emprestimos>.

## Console H2 e persistência

Os dados ficam na pasta `data` de cada aplicação e permanecem após reiniciá-la.
Execute os comandos a partir das pastas indicadas acima, pois o caminho do banco é relativo.
Os arquivos de banco e a pasta `target` são ignorados pelo Git.

| Configuração | Leitor | Empréstimo |
| --- | --- | --- |
| Console | <http://localhost:8080/h2-console> | <http://localhost:8081/h2-console> |
| Driver Class | `org.h2.Driver` | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/biblioteca-leitor` | `jdbc:h2:file:./data/biblioteca-emprestimo` |
| User Name | `admin` | `admin` |
| Password | deixar vazio | deixar vazio |

A aplicação correspondente precisa estar em execução para abrir seu console.
Depois de conectar, use `SELECT * FROM LEITOR;` ou `SELECT * FROM EMPRESTIMO;`.

## Testes

Na pasta `emprestimo`, execute:

```powershell
.\mvnw.cmd test
```

Os quatro testes de integração verificam listagem vazia, cadastro com resposta 201,
persistência dos campos e da data, listagem dos registros e preservação de um registro
existente quando o cliente envia um ID no POST. Usam um banco H2 em memória exclusivo
do perfil `test`, sem alterar os arquivos de banco usados pela aplicação.

Para gerar o JAR, incluindo a execução dos testes:

```powershell
.\mvnw.cmd package
```

Para conferir a aplicação Leitor já em execução, use o script existente, a partir de `leitor/leitor`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\testar-leitores.ps1
```

Esse script verifica o CRUD e os retornos 404; ao final, remove o leitor que criou para o teste.

## Organização do código

Cada projeto contém a classe principal e os pacotes `entity`, `repository`, `service` e `controller`.
A entidade define os dados persistidos; o repositório fornece acesso ao banco; o serviço
executa as operações; o controlador expõe os endpoints HTTP.
