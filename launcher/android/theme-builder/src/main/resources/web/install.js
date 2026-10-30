import { api, clearErrors, showFieldError } from "./api.js";

// Aba "Instalar na TV" (feature 006): a página conduz as etapas uma a uma (interfaces/api-instalacao.md).
const STEPS = [
  ["conectar", "Conectar e autorizar"],
  ["instalar", "Instalar o launcher"],
  ["inicio-automatico", "Início automático"],
  ["codigo", "Código do aparelho"],
  ["tema", "Tema do cliente"],
  ["ativacao", "Ativação"],
  ["acessibilidade", "Troca da tela inicial"],
  ["conferencia", "Conferência com o Home"],
];
const ICONS = { pending: "·", running: "…", ok: "✓", skipped: "✓", error: "✕" };
const IP_STORAGE_KEY = "tematv.ultimoIp";

const form = document.getElementById("install-form");
const stepList = document.getElementById("install-steps");
const environmentBanner = document.getElementById("install-environment");
const tvStatus = document.getElementById("tv-status");
const startButton = document.getElementById("start-install");
const checkButton = document.getElementById("check-tv");
const undoButton = document.getElementById("undo-install");
const authorizationHint = document.getElementById("authorization-hint");
const result = document.getElementById("install-result");
const elapsedLabel = document.getElementById("install-elapsed");

let environment = null;
let tvFound = false;
let running = false;
let install = null;
let timer = null;

function readStoredIp() {
  try { return localStorage.getItem(IP_STORAGE_KEY) ?? ""; } catch { return ""; }
}

function storeIp(ip) {
  try { localStorage.setItem(IP_STORAGE_KEY, ip); } catch { /* navegador sem armazenamento: só não lembra o IP */ }
}

function formatSeconds(ms) {
  const seconds = Math.round(ms / 1000);
  return `${Math.floor(seconds / 60)}:${String(seconds % 60).padStart(2, "0")}`;
}

function updateButtons() {
  const ready = environment?.adb_available && environment?.launcher && form.elements.theme_id.value;
  startButton.disabled = running || !ready || !tvFound;
  checkButton.disabled = running;
  undoButton.disabled = running || !environment?.adb_available;
}

export async function showInstall() {
  clearErrors(form);
  environment = await api("GET", "/api/instalacao/ambiente");
  const problems = [];
  if (!environment.adb_available) problems.push(`O adb não está instalado no notebook. Instale com: ${environment.adb_install_command}`);
  if (!environment.launcher) problems.push(`APK de produção não encontrado. ${environment.launcher_missing_hint}`);
  environmentBanner.textContent = problems.join(" ");
  environmentBanner.className = problems.length ? "banner error" : "banner";
  environmentBanner.hidden = problems.length === 0;

  const clients = (await api("GET", "/api/clientes")).filter((client) => client.package_exists);
  const select = form.elements.theme_id;
  const previous = select.value;
  select.replaceChildren(...clients.map((client) => new Option(client.display_name, client.theme_id)));
  if (clients.length === 0) select.append(new Option("Nenhum cliente com pacote gerado", ""));
  if (previous) select.value = previous;
  if (!form.elements.ip.value) form.elements.ip.value = readStoredIp();
  if (!install) renderSteps({});
  updateButtons();
}

function renderSteps(states) {
  stepList.replaceChildren(...STEPS.map(([id, label], index) => {
    const state = states[id] ?? { status: "pending" };
    const item = document.createElement("li");
    item.className = state.status;
    item.append(
      Object.assign(document.createElement("span"), { className: "icon", textContent: ICONS[state.status] }),
      Object.assign(document.createElement("div"), {}),
      Object.assign(document.createElement("span"), { className: "time", textContent: state.elapsed ?? "" }),
    );
    const body = item.children[1];
    body.append(Object.assign(document.createElement("strong"), { textContent: label }));
    if (state.message) body.append(Object.assign(document.createElement("div"), { className: "detail", textContent: state.message }));
    if (state.status === "error" && !running) {
      const actions = Object.assign(document.createElement("div"), { className: "step-actions" });
      actions.append(button("Tentar de novo a partir desta etapa", () => runFrom(index)));
      if (state.errorCode === "assinatura-diferente") {
        actions.append(button("Desfazer e instalar de novo", () => reinstall(index)));
      }
      item.append(actions);
    }
    return item;
  }));
}

function button(text, onClick) {
  const node = Object.assign(document.createElement("button"), { type: "button", textContent: text });
  node.addEventListener("click", onClick);
  return node;
}

function request() {
  return { install_id: install.id, ip: install.ip, theme_id: install.themeId, device_code: install.deviceCode };
}

async function runFrom(startIndex) {
  running = true;
  result.hidden = true;
  updateButtons();
  for (let index = startIndex; index < STEPS.length; index++) {
    const [id] = STEPS[index];
    install.states[id] = { status: "running", message: "Em andamento…" };
    install.currentStepStartedAt = Date.now();
    authorizationHint.hidden = id !== "conectar";
    renderSteps(install.states);
    let outcome;
    try {
      outcome = await api("POST", `/api/instalacao/etapas/${id}`, request());
    } catch (error) {
      outcome = { status: "error", message: error.message };
    }
    if (outcome.device_code) install.deviceCode = outcome.device_code;
    install.states[id] = {
      status: outcome.status,
      message: outcome.message,
      errorCode: outcome.error_code,
      elapsed: formatSeconds(Date.now() - install.currentStepStartedAt),
    };
    authorizationHint.hidden = true;
    if (outcome.status === "error") {
      running = false;
      renderSteps(install.states);
      updateButtons();
      return;
    }
  }
  running = false;
  clearInterval(timer);
  renderSteps(install.states);
  result.className = "result";
  result.textContent = `TV pronta em ${formatSeconds(Date.now() - install.startedAt)}. Registro em ~/tematv/instalacoes/.`;
  result.hidden = false;
  updateButtons();
}

async function reinstall(index) {
  if (!confirm("Desfazer remove o launcher desta TV e apaga a ativação atual. Em seguida a instalação continua e ativa a TV de novo. Continuar?")) return;
  running = true;
  updateButtons();
  install.states.instalar = { status: "running", message: "Removendo o launcher anterior…" };
  renderSteps(install.states);
  const undone = await api("POST", "/api/instalacao/desfazer", request());
  install.deviceCode = undefined;
  if (undone.status === "error") {
    running = false;
    install.states.instalar = { status: "error", message: undone.message };
    renderSteps(install.states);
    updateButtons();
    return;
  }
  await runFrom(index);
}

checkButton.addEventListener("click", async () => {
  clearErrors(form);
  tvStatus.textContent = "Procurando a TV…";
  try {
    const outcome = await api("POST", "/api/instalacao/verificar", { ip: form.elements.ip.value });
    tvFound = outcome.status === "ok";
    tvStatus.textContent = outcome.message;
    tvStatus.className = tvFound ? "" : "field-error";
    if (tvFound) storeIp(form.elements.ip.value.trim());
  } catch (error) {
    tvFound = false;
    tvStatus.textContent = "";
    if (!showFieldError(form, error)) tvStatus.textContent = error.message;
  }
  updateButtons();
});

form.elements.ip.addEventListener("input", () => { tvFound = false; tvStatus.textContent = ""; updateButtons(); });
form.elements.theme_id.addEventListener("change", updateButtons);

startButton.addEventListener("click", () => {
  install = {
    id: new Date().toISOString(),
    ip: form.elements.ip.value.trim(),
    themeId: form.elements.theme_id.value,
    deviceCode: undefined,
    states: {},
    startedAt: Date.now(),
  };
  clearInterval(timer);
  timer = setInterval(() => { elapsedLabel.textContent = formatSeconds(Date.now() - install.startedAt); }, 1000);
  runFrom(0);
});

undoButton.addEventListener("click", async () => {
  const ip = form.elements.ip.value.trim();
  if (!confirm(`Remover o launcher da TV ${ip}? O Home volta ao launcher do Google e a ativação desta TV se perde.`)) return;
  clearErrors(form);
  running = true;
  updateButtons();
  try {
    const outcome = await api("POST", "/api/instalacao/desfazer", {
      install_id: new Date().toISOString(), ip, theme_id: form.elements.theme_id.value || "-",
    });
    result.className = outcome.status === "error" ? "result error" : "result";
    result.textContent = outcome.message;
    result.hidden = false;
  } catch (error) {
    if (!showFieldError(form, error)) {
      result.className = "result error";
      result.textContent = error.message;
      result.hidden = false;
    }
  } finally {
    running = false;
    updateButtons();
  }
});
