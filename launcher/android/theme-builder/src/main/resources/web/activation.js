import { api, showFieldError, clearErrors } from "./api.js";

// Tela "Ativar TV" (RF-12, RF-13): o segredo nunca chega à página, só o resultado.
const form = document.getElementById("activation-form");
const result = document.getElementById("activation-result");
const secretStatus = document.getElementById("secret-status");

export async function showActivation() {
  result.hidden = true;
  const status = await api("GET", "/api/ativacao");
  secretStatus.hidden = status.secret_available;
  secretStatus.textContent = status.secret_available ? "" :
    `Segredo de ativação não encontrado em ${status.secret_path}. Restaure o arquivo do seu backup ` +
    `(ou, só na primeira vez, gere com "activation-tool gerar-segredo"). Os temas continuam funcionando.`;
  form.querySelector("button[type=submit]").disabled = !status.secret_available;
}

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  clearErrors(form);
  result.hidden = true;
  const data = new FormData(form);
  try {
    const generated = await api("POST", "/api/ativacao", {
      device_code: data.get("device_code"),
      customer: data.get("customer"),
    });
    result.className = "result";
    result.replaceChildren(
      Object.assign(document.createElement("div"), { textContent: `Código do aparelho: ${generated.device_code}` }),
      Object.assign(document.createElement("div"), { textContent: "Senha de ativação:" }),
      Object.assign(document.createElement("div"), { className: "password", textContent: generated.activation_code }),
      Object.assign(document.createElement("div"), { className: "muted", textContent: "Registrada em ~/tematv/ativacoes.csv." }),
    );
    result.hidden = false;
  } catch (error) {
    if (!showFieldError(form, error)) {
      result.className = "result error";
      result.textContent = error.message;
      result.hidden = false;
    }
  }
});
