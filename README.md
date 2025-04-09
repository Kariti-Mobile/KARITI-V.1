# KARITI MOBILE
## Este repositório destina-se ao código fonte do aplicativo Kariti Mobile - Versão 1

## ORIGEM
O Kariti, ferramenta para correção de provas objetivas utilizando visão computacional, foi originado por um aluno da Universidade do Estado do Amazonas sob orientação de um professor da Universidade durante projeto de pesquisa. Inicialmente, foi desenvolvido para Web, utilizando as linguagens Python (Para o código que corrige as provas usando visão computacional), PHP, JavaScript, CSS e HTML para interface da ferramenta. 

Os códigos estão disponiveis no repositório do [GitHub](https://github.com/karitisystem/kariti)
Caso deseje saber mais sobre, veja também o artigo: [Kariti: sistema gratuito de correção automatizada de provas objetivas](https://projecaociencia.com.br/index.php/Projecao4/article/view/1814)

## VERSÃO MOBILE
Com o intuito de alcançar todo o público interessado em utilizar a ferramenta de forma prática e principalmente gratuita, foi desenvolvida uma versão mobile da ferramenta, esta versão realiza todo gerenciamento relacionado aos dados das provas.

### Principais funcionalidades
* Cadastro de usuários - Para ter acesso ao aplicativo, primeiramente será necessário realizar o cadastro informando dados como: nome, e-mail e uma senha.
* Cadastro de Escolas - Cadastro da(s) escola(s) em que atua.
* Cadastro de Alunos - Cadastrar alunos pertencentes a escola selecionada.
* Cadastro de Turma - Cadastrar turmas pertencentes a escola selecionada e incluir alunos a essa turma (aqui o Kariti permite o cadastro de alunos anônimos - alunos identificados sequêncialmente, sem a necessidade de utilizar a funcionalidade anterior).
* Cadastro de Provas - Uma das funcionalidades principais do aplicativo, o usuário informa informações como: Nome da prova, turma, data de aplicação, quantidade de questões, quantidade de alternativas e gabarito.
* Download de Cartões - Após a prova cadastrada, será disponibilizado para download os cartões respostas dessa prova com a quantidade equivalente a quantidade de alunos pertencentes a turma selecionada.
* Correção da Prova - Nesta versão do app, são capturadas as imagens dos cartões em fundo escuro, em seguida são compactadas e envidos ao Kariti online (Módulo web que realiza a correção da prova usando a linguagem Python e devolve um Json com as respostas encontradas nas provas que posteriormente é processado pelo Kariti Mobile e mostrado na tela ao usuário o resultado da correção).

