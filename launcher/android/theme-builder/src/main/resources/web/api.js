export class ApiError extends Error {
  constructor(status, message, field) {
    super(message);
    this.status = status;
    this.field = field;
  }
}

export async function api(method, path, body) {
  const options = { method, headers: {} };
  if (body !== undefined) {
    options.headers["Content-Type"] = "application/json";
    options.body = JSON.stringify(body);
  }
  const response = await fetch(path, options);
  if (response.status === 204) return null;
  const data = await response.json();
  if (!response.ok) throw new ApiError(response.status, data.error, data.field ?? null);
  return data;
}

export function clearErrors(root) {
  root.querySelectorAll(".field-error").forEach((node) => node.remove());
}

/** Mostra o erro ao lado do campo com `data-field` igual; devolve false se o campo não está na tela. */
export function showFieldError(root, error) {
  if (!error.field) return false;
  const target = root.querySelector(`[data-field="${CSS.escape(error.field)}"]`);
  if (!target) return false;
  const message = Object.assign(document.createElement("span"), { className: "field-error", textContent: error.message });
  target.after(message);
  target.scrollIntoView({ block: "center", behavior: "smooth" });
  target.focus?.();
  return true;
}
