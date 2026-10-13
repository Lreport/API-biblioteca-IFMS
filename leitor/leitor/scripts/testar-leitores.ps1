# Execute com a aplicacao rodando na porta 8080.
# O teste cria um leitor proprio e o remove ao terminar.
$ErrorActionPreference = 'Stop'
$baseUrl = 'http://localhost:8080'

function Invoke-ApiRequest {
    param(
        [string]$Metodo,
        [string]$Caminho,
        [object]$Dados = $null
    )

    $opcoes = @{
        Uri = "$baseUrl$Caminho"
        Method = $Metodo
        UseBasicParsing = $true
        ErrorAction = 'Stop'
        TimeoutSec = 10
        Headers = @{ Accept = 'application/json' }
    }

    if ($null -ne $Dados) {
        $json = $Dados | ConvertTo-Json -Compress
        $opcoes.ContentType = 'application/json; charset=utf-8'
        $opcoes.Body = [System.Text.Encoding]::UTF8.GetBytes($json)
    }

    try {
        $resposta = Invoke-WebRequest @opcoes
        return [pscustomobject]@{
            Status = [int]$resposta.StatusCode
            Conteudo = [string]$resposta.Content
        }
    } catch {
        # PowerShell trata respostas como 404 como excecoes.
        if ($null -eq $_.Exception.Response) {
            throw
        }
        return [pscustomobject]@{
            Status = [int]$_.Exception.Response.StatusCode
            Conteudo = [string]$_.ErrorDetails.Message
        }
    }
}

function Assert-Status {
    param($Resposta, [int]$Esperado, [string]$Operacao)

    if ($Resposta.Status -ne $Esperado) {
        throw "$Operacao : esperado HTTP $Esperado, recebido $($Resposta.Status). $($Resposta.Conteudo)"
    }
    Write-Host "[OK] $Operacao -> HTTP $Esperado"
}

function Assert-Leitor {
    param($Leitor, [long]$IdEsperado, [string]$NomeEsperado, [string]$EmailEsperado)

    if ($null -eq $Leitor -or $Leitor.id -ne $IdEsperado -or
        $Leitor.nome -cne $NomeEsperado -or $Leitor.email -cne $EmailEsperado) {
        throw 'Os dados retornados para o leitor diferem dos dados esperados.'
    }
}

$testeId = $null
$removido = $false
$emailTeste = 'teste.' + [guid]::NewGuid().ToString('N') + '@example.com'

try {
    $resposta = Invoke-ApiRequest 'GET' '/leitores'
    Assert-Status $resposta 200 'Listar leitores'

    $dados = @{ nome = 'Leitor de teste'; email = $emailTeste }
    $resposta = Invoke-ApiRequest 'POST' '/leitores' $dados
    Assert-Status $resposta 201 'Cadastrar leitor'
    $leitor = $resposta.Conteudo | ConvertFrom-Json
    if ($null -eq $leitor.id -or [long]$leitor.id -le 0) {
        throw 'O cadastro nao retornou um identificador gerado.'
    }
    $testeId = [long]$leitor.id
    Assert-Leitor $leitor $testeId $dados.nome $dados.email

    $resposta = Invoke-ApiRequest 'GET' '/leitores'
    Assert-Status $resposta 200 'Listar apos cadastrar'
    $lista = @($resposta.Conteudo | ConvertFrom-Json)
    $encontrados = @($lista | Where-Object { $_.id -eq $testeId })
    if ($encontrados.Count -ne 1) {
        throw 'O leitor cadastrado deveria aparecer uma vez na listagem.'
    }
    Assert-Leitor $encontrados[0] $testeId $dados.nome $dados.email

    $caminho = "/leitores/$testeId"
    $resposta = Invoke-ApiRequest 'GET' $caminho
    Assert-Status $resposta 200 'Buscar leitor por ID'
    Assert-Leitor ($resposta.Conteudo | ConvertFrom-Json) $testeId $dados.nome $dados.email

    $dados = @{ nome = 'Leitor atualizado'; email = ('atualizado.' + $emailTeste) }
    $resposta = Invoke-ApiRequest 'PUT' $caminho $dados
    Assert-Status $resposta 200 'Atualizar leitor'
    Assert-Leitor ($resposta.Conteudo | ConvertFrom-Json) $testeId $dados.nome $dados.email

    $resposta = Invoke-ApiRequest 'GET' $caminho
    Assert-Status $resposta 200 'Consultar os dados atualizados'
    Assert-Leitor ($resposta.Conteudo | ConvertFrom-Json) $testeId $dados.nome $dados.email

    $resposta = Invoke-ApiRequest 'DELETE' $caminho
    Assert-Status $resposta 204 'Excluir leitor'
    $removido = $true
    if (-not [string]::IsNullOrEmpty($resposta.Conteudo)) {
        throw 'A resposta HTTP 204 deveria ter o corpo vazio.'
    }

    $resposta = Invoke-ApiRequest 'GET' $caminho
    Assert-Status $resposta 404 'Buscar leitor excluido'

    $resposta = Invoke-ApiRequest 'PUT' $caminho $dados
    Assert-Status $resposta 404 'Atualizar leitor inexistente'

    $resposta = Invoke-ApiRequest 'DELETE' $caminho
    Assert-Status $resposta 404 'Excluir leitor inexistente'

    $resposta = Invoke-ApiRequest 'GET' '/leitores'
    Assert-Status $resposta 200 'Listar apos excluir'
    $restantes = @($resposta.Conteudo | ConvertFrom-Json)
    if (@($restantes | Where-Object { $_.id -eq $testeId }).Count -ne 0) {
        throw 'O leitor de teste ainda aparece na listagem apos a exclusao.'
    }

    Write-Host ''
    Write-Host 'SUCESSO: todos os endpoints e retornos esperados foram verificados.'
    Write-Host "O leitor de teste (ID $testeId) foi excluido."
} finally {
    # Remove somente o registro criado por este teste, se houver falha antes da exclusao.
    if ($null -ne $testeId -and -not $removido) {
        try {
            $limpeza = Invoke-ApiRequest 'DELETE' "/leitores/$testeId"
            if ($limpeza.Status -notin @(204, 404)) {
                Write-Warning "Nao foi possivel remover o leitor de teste de ID $testeId."
            }
        } catch {
            Write-Warning "Nao foi possivel remover o leitor de teste de ID $testeId."
        }
    }
}
