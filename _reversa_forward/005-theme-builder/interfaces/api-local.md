# Interface: API local da página do theme-builder

> Identificador da feature: `005-theme-builder`
> Tipo: HTTP, somente `127.0.0.1`
> Produtor: `:theme-builder` (servidor)
> Consumidor: a página servida pelo próprio servidor

## Regras gerais

- Endereço: `http://localhost:<porta>`, porta 8787 ou a próxima livre até 8797.
- Pedidos com `Host` diferente de `localhost:<porta>` ou `127.0.0.1:<porta>`: `403`.
- `POST` e `PUT`: exigem `Content-Type: application/json` e, se houver `Origin`, que seja o próprio endereço; senão `403`.
- Corpo e respostas em JSON UTF-8. Erros: `{"error": "<mensagem em português>", "field": "<campo ou null>"}`.

## Rotas

| Método e caminho | Request | Response |
|------------------|---------|----------|
| `GET /` e arquivos da página | | HTML, CSS, JS |
| `GET /api/presets` | | molduras prontas (`id`, nome, áreas, URL da imagem), lista NVI inicial, atribuição da NVI |
| `GET /api/presets/<id>/moldura` | | PNG da moldura pronta |
| `GET /api/clientes` | | lista `{theme_id, display_name, type, updated_at, package_exists}` |
| `POST /api/clientes` | `{type, display_name, theme_id}` | `201` com o projeto inicial; `409` se o `theme_id` já existe |
| `GET /api/clientes/<theme_id>` | | projeto |
| `PUT /api/clientes/<theme_id>` | projeto sem imagens | projeto gravado; `400` com o campo |
| `PUT /api/clientes/<theme_id>/imagens/<logo\|fundo\|moldura>` | `{file_name, base64}` | `{file}`; `400` (tipo errado, acima de 5 MB no logotipo, WEBP grande) |
| `DELETE /api/clientes/<theme_id>/imagens/fundo` | | `204` |
| `GET /api/clientes/<theme_id>/imagens/<logo\|fundo\|moldura>` | | a imagem |
| `POST /api/clientes/<theme_id>/pacote` | `{}` | `{path, size_bytes, adb_commands}`; `422` com `code` e `field` do `ThemeError` |
| `GET /api/ativacao` | | `{secret_available, secret_path}` (nunca o segredo) |
| `POST /api/ativacao` | `{device_code, customer}` | `{device_code, activation_code}` formatados; `400` código inválido; `409` sem segredo |

## Idempotência

`PUT` regrava o mesmo estado. `POST /pacote` repetido gera pacote igual. `POST /api/ativacao` repetido devolve a mesma senha e acrescenta uma linha a `ativacoes.csv` a cada vez (como o `activation-tool`).

## Timeouts

Sem rede externa. Geração e validação em até 10 s para pacotes de até 15 MB.

## Segurança

O segredo de ativação e a senha gerada não aparecem no log do terminal.
