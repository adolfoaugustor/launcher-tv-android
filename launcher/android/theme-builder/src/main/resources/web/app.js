import { api, clearErrors, showFieldError } from "./api.js";
import { renderPreview, keepPreviewFitted } from "./preview.js";
import { showActivation } from "./activation.js";
import { showInstall } from "./install.js";

const $ = (id) => document.getElementById(id);
const globalError = $("global-error");
const projectForm = $("project-form");
const previewContainer = $("preview");

let presets = null;
let project = null;
let imageVersion = Date.now();

function showGlobalError(message) {
  globalError.textContent = message;
  globalError.hidden = !message;
}

function showView(name) {
  showGlobalError("");
  for (const view of ["clients", "project", "install", "activation"]) $(`${view}-view`).hidden = view !== name;
  document.querySelectorAll(".tab").forEach((tab) => {
    tab.classList.toggle("active", tab.dataset.view === name || (name === "project" && tab.dataset.view === "clients"));
  });
}

async function run(task) {
  try {
    await task();
  } catch (error) {
    showGlobalError(error.message);
  }
}

// ---------- Clientes (RF-02) ----------

function suggestThemeId(name) {
  return name.toLowerCase().normalize("NFD").replace(/\p{M}/gu, "")
    .replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").slice(0, 40).replace(/-+$/, "");
}

async function showClients() {
  showView("clients");
  const clients = await api("GET", "/api/clientes");
  const list = $("client-list");
  list.replaceChildren(...clients.map((client) => {
    const button = document.createElement("button");
    button.type = "button";
    const type = client.type === "igreja" ? "Igreja" : "Loja";
    const updated = new Date(client.updated_at).toLocaleString("pt-BR");
    button.append(
      Object.assign(document.createElement("strong"), { textContent: client.display_name }),
      Object.assign(document.createElement("span"), {
        className: "muted",
        textContent: `${type} · ${client.theme_id} · ${client.package_exists ? "pacote gerado" : "sem pacote"} · ${updated}`,
      }),
    );
    button.addEventListener("click", () => run(() => openProject(client.theme_id)));
    const item = document.createElement("li");
    item.append(button);
    return item;
  }));
  $("empty-clients").hidden = clients.length > 0;
}

const newClientForm = $("new-client-form");
let themeIdEdited = false;
newClientForm.elements.display_name.addEventListener("input", (event) => {
  if (!themeIdEdited) newClientForm.elements.theme_id.value = suggestThemeId(event.target.value);
});
newClientForm.elements.theme_id.addEventListener("input", () => { themeIdEdited = true; });
$("new-client-button").addEventListener("click", () => {
  newClientForm.reset();
  themeIdEdited = false;
  clearErrors(newClientForm);
  newClientForm.hidden = false;
  newClientForm.elements.display_name.focus();
});
$("cancel-new-client").addEventListener("click", () => { newClientForm.hidden = true; });
newClientForm.addEventListener("submit", (event) => {
  event.preventDefault();
  clearErrors(newClientForm);
  const data = new FormData(newClientForm);
  run(async () => {
    try {
      const created = await api("POST", "/api/clientes", {
        type: data.get("type"), display_name: data.get("display_name"), theme_id: data.get("theme_id"),
      });
      newClientForm.hidden = true;
      await openProject(created.theme_id);
    } catch (error) {
      if (!showFieldError(newClientForm, error)) throw error;
    }
  });
});

// ---------- Projeto (RF-03 a RF-11) ----------

function isChurch() {
  return project.type === "igreja";
}

function imageUrl(kind) {
  return `/api/clientes/${project.theme_id}/imagens/${kind}?v=${imageVersion}`;
}

function frameAssets() {
  const choice = project.icon_frame;
  if (choice.preset) {
    const preset = presets.frames.find((frame) => frame.id === choice.preset);
    return { url: preset.image_url, iconSlot: preset.icon_slot, labelSlot: preset.label_slot };
  }
  const fallback = { x: 0, y: 0, w: 1, h: 1 };
  return {
    url: project.hasCustomFrame ? imageUrl("moldura") : null,
    iconSlot: choice.icon_slot ?? fallback, labelSlot: choice.label_slot ?? fallback,
  };
}

function refreshPreview() {
  renderPreview(previewContainer, project, {
    logoUrl: project.logo_file ? imageUrl("logo") : null,
    backgroundUrl: project.background_file ? imageUrl("fundo") : null,
    frame: frameAssets(),
    attribution: isChurch() ? presets.nvi_attribution : (project.verses_attribution ?? ""),
    version: isChurch() ? "NVI" : "LOJA",
  });
  $("message-counter").textContent = `(${project.message.length}/120)`;
}

async function openProject(themeId) {
  project = await api("GET", `/api/clientes/${themeId}`);
  project.hasCustomFrame = !project.icon_frame.preset && await customFrameExists();
  imageVersion = Date.now();
  showView("project");
  clearErrors(projectForm);
  $("package-result").hidden = true;
  $("save-status").textContent = "";
  fillProjectForm();
  refreshPreview();
}

async function customFrameExists() {
  const response = await fetch(imageUrl("moldura"), { method: "GET" });
  return response.ok;
}

function fillProjectForm() {
  const form = projectForm.elements;
  $("project-title").textContent = project.display_name;
  $("project-type").textContent = isChurch() ? "Igreja" : "Loja";
  form.display_name.value = project.display_name;
  form.background.value = project.colors.background;
  form.text.value = project.colors.text;
  form.accent.value = project.colors.accent;
  form.message.value = project.message;
  form.logo.value = "";
  form.fundo.value = "";
  form.moldura.value = "";
  $("remove-background").hidden = !project.background_file;

  form.frame.replaceChildren(
    ...presets.frames.map((frame) => new Option(frame.display_name, frame.id)),
    new Option("Moldura própria (PNG)", ""),
  );
  form.frame.value = project.icon_frame.preset ?? "";
  fillCustomFrame();

  $("verses-legend").textContent = isChurch() ? "Versículos (NVI)" : "Mensagens da loja";
  $("add-verse").textContent = isChurch() ? "Acrescentar versículo" : "Acrescentar mensagem";
  renderVerses();

  form.attribution.value = isChurch() ? presets.nvi_attribution : (project.verses_attribution ?? "");
  form.attribution.readOnly = isChurch();
}

function fillCustomFrame() {
  const custom = !project.icon_frame.preset;
  $("custom-frame").hidden = !custom;
  for (const container of document.querySelectorAll("#custom-frame .slots")) {
    const key = container.dataset.slot;
    const slot = project.icon_frame[key] ?? { x: 0.1, y: 0.1, w: 0.8, h: 0.5 };
    container.querySelectorAll("input").forEach((input) => input.remove());
    for (const part of ["x", "y", "w", "h"]) {
      const input = Object.assign(document.createElement("input"), {
        type: "number", min: "0", max: "1", step: "0.01", value: slot[part], title: part,
      });
      input.dataset.field = `icon_frame.${key}`;
      input.addEventListener("input", () => {
        project.icon_frame[key] = readSlot(container);
        refreshPreview();
      });
      container.append(input);
    }
    if (custom) project.icon_frame[key] = readSlot(container);
  }
}

function readSlot(container) {
  const [x, y, w, h] = [...container.querySelectorAll("input")].map((input) => Number(input.value));
  return { x, y, w, h };
}

function renderVerses() {
  const list = $("verse-list");
  list.replaceChildren(...project.verses.map((verse, index) => {
    const item = document.createElement("li");
    const fields = Object.assign(document.createElement("div"), { className: "verse-fields" });
    const reference = Object.assign(document.createElement("input"), {
      value: verse.reference, maxLength: 40, placeholder: isChurch() ? "Livro 1:1" : "Título",
    });
    reference.dataset.field = `verses[${index}].reference`;
    reference.addEventListener("input", () => { verse.reference = reference.value; if (index === 0) refreshPreview(); });
    const text = Object.assign(document.createElement("textarea"), { value: verse.text, maxLength: 600, rows: 2 });
    text.dataset.field = `verses[${index}].text`;
    text.addEventListener("input", () => { verse.text = text.value; if (index === 0) refreshPreview(); });
    fields.append(reference, text);

    const buttons = Object.assign(document.createElement("div"), { className: "verse-buttons" });
    const move = (offset) => {
      const target = index + offset;
      if (target < 0 || target >= project.verses.length) return;
      [project.verses[index], project.verses[target]] = [project.verses[target], project.verses[index]];
      renderVerses();
      refreshPreview();
    };
    buttons.append(
      button("↑", "Subir", () => move(-1)),
      button("↓", "Descer", () => move(1)),
      button("✕", "Remover", () => { project.verses.splice(index, 1); renderVerses(); refreshPreview(); }),
    );
    item.append(fields, buttons);
    return item;
  }));
}

function button(text, title, onClick) {
  const node = Object.assign(document.createElement("button"), { type: "button", textContent: text, title });
  node.addEventListener("click", onClick);
  return node;
}

$("add-verse").addEventListener("click", () => {
  if (project.verses.length >= 366) {
    showGlobalError("A lista aceita no máximo 366 itens.");
    return;
  }
  project.verses.push({ reference: "", text: "" });
  renderVerses();
  const inputs = $("verse-list").querySelectorAll("input");
  inputs[inputs.length - 1].focus();
});

projectForm.addEventListener("input", (event) => {
  const form = projectForm.elements;
  switch (event.target.name) {
    case "display_name": project.display_name = form.display_name.value; break;
    case "background": case "text": case "accent": project.colors[event.target.name] = event.target.value; break;
    case "message": project.message = form.message.value; break;
    case "attribution": if (!isChurch()) project.verses_attribution = form.attribution.value; break;
    default: return;
  }
  refreshPreview();
});

projectForm.elements.frame.addEventListener("change", (event) => {
  project.icon_frame = event.target.value ? { preset: event.target.value } : {};
  fillCustomFrame();
  refreshPreview();
});

for (const kind of ["logo", "fundo", "moldura"]) {
  projectForm.elements[kind].addEventListener("change", (event) => {
    const file = event.target.files[0];
    if (!file) return;
    clearErrors(projectForm);
    const reader = new FileReader();
    reader.onload = () => run(async () => {
      try {
        const saved = await api("PUT", `/api/clientes/${project.theme_id}/imagens/${kind}`, {
          file_name: file.name, base64: reader.result.split(",")[1],
        });
        if (kind === "logo") project.logo_file = saved.file;
        if (kind === "fundo") project.background_file = saved.file;
        if (kind === "moldura") project.hasCustomFrame = true;
        $("remove-background").hidden = !project.background_file;
        imageVersion = Date.now();
        refreshPreview();
      } catch (error) {
        event.target.value = "";
        if (!showFieldError(projectForm, error)) throw error;
      }
    });
    reader.readAsDataURL(file);
  });
}

$("remove-background").addEventListener("click", () => run(async () => {
  await api("DELETE", `/api/clientes/${project.theme_id}/imagens/fundo`);
  project.background_file = null;
  $("remove-background").hidden = true;
  refreshPreview();
}));

async function saveProject() {
  clearErrors(projectForm);
  const { hasCustomFrame, ...body } = project;
  const saved = await api("PUT", `/api/clientes/${project.theme_id}`, body);
  Object.assign(project, saved, { hasCustomFrame });
  $("project-title").textContent = project.display_name;
  refreshPreview();
  $("save-status").textContent = `Salvo às ${new Date().toLocaleTimeString("pt-BR")}`;
}

async function withFieldErrors(task) {
  try {
    await task();
  } catch (error) {
    if (!showFieldError(projectForm, error)) throw error;
  }
}

$("save-project").addEventListener("click", () => run(() => withFieldErrors(saveProject)));

$("generate-package").addEventListener("click", () => run(() => withFieldErrors(async () => {
  const result = $("package-result");
  result.hidden = true;
  await saveProject();
  const built = await api("POST", `/api/clientes/${project.theme_id}/pacote`, {});
  const size = built.size_bytes < 1024 * 1024
    ? `${Math.ceil(built.size_bytes / 1024)} KB`
    : `${(built.size_bytes / 1024 / 1024).toFixed(1)} MB`;
  const commands = built.adb_commands.join("\n");
  const copy = button("Copiar comandos", "Copiar comandos do ADB", async () => {
    await navigator.clipboard.writeText(commands);
    copy.textContent = "Copiado";
  });
  result.className = "result";
  result.replaceChildren(
    Object.assign(document.createElement("strong"), { textContent: `Pacote gerado em ${built.path} (${size})` }),
    Object.assign(document.createElement("p"), {
      textContent: "Para aplicar na TV, troque <IP-DA-TV> pelo IP da TV e rode no terminal, com a TV ligada:",
    }),
    Object.assign(document.createElement("pre"), { textContent: commands }),
    copy,
  );
  result.hidden = false;
  result.scrollIntoView({ block: "nearest", behavior: "smooth" });
})));

$("back-to-clients").addEventListener("click", () => run(showClients));

// ---------- Navegação ----------

document.querySelectorAll(".tab").forEach((tab) => {
  tab.addEventListener("click", () => run(async () => {
    if (tab.dataset.view === "activation") {
      showView("activation");
      await showActivation();
    } else if (tab.dataset.view === "install") {
      showView("install");
      await showInstall();
    } else {
      await showClients();
    }
  }));
});

keepPreviewFitted(previewContainer);
run(async () => {
  presets = await api("GET", "/api/presets");
  await showClients();
});
