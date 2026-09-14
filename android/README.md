# Personal Cyber AI — integração Android

Este módulo cria um aplicativo Android leve para tornar a central acessível pelo sistema de compartilhamento.

## O que ele faz

- aparece em **Compartilhar → Personal Cyber AI** para textos e links;
- aparece ao selecionar texto e escolher **Processar texto com Personal Cyber AI**;
- adiciona o prefixo global `•` automaticamente;
- envia o comando para o backend público `/api/chat`;
- não lê mensagens, não monitora a tela e não usa acessibilidade invasiva;
- não guarda chaves secretas no aplicativo.

## Gerar o APK

Abra a pasta `android/` no Android Studio e gere o APK de debug. Também é possível usar o workflow do GitHub Actions `Android APK`, que publica o APK como artefato da execução.

O app usa o serviço público do Personal Cyber AI e pode sofrer o atraso de inicialização do plano Free do Render. A API OpenAI continua protegida no Render; nenhuma chave é incluída neste módulo.

## Limite intencional

O prefixo `•` é aplicado aos comandos iniciados no app e aos conteúdos compartilhados para ele. O módulo não intercepta digitação em outros aplicativos nem monitora conversas privadas. Isso preserva a privacidade e evita pedir a permissão de acessibilidade sem necessidade.
