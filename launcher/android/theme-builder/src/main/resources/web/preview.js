// Prévia da tela inicial (RF-07), com as medidas de activity_home.xml e dimens.xml em dp:
// a TV 1920×1080 de densidade 320 tem 960×540 dp.
const STAGE_WIDTH = 960;
const STAGE_HEIGHT = 540;
const SAMPLE_APPS = [
  ["YouTube", "#e53935"], ["Netflix", "#b71c1c"], ["Globoplay", "#fb8c00"], ["Spotify", "#43a047"], ["Prime Video", "#1e88e5"],
  ["Disney+", "#3949ab"], ["Max", "#5e35b1"], ["Navegador", "#00897b"], ["Música", "#d81b60"], ["Fotos", "#f9a825"],
];

function el(tag, style = {}, text) {
  const node = document.createElement(tag);
  Object.assign(node.style, style);
  if (text !== undefined) node.textContent = text;
  return node;
}

function slotStyle(slot) {
  return {
    position: "absolute",
    left: `${slot.x * 100}%`, top: `${slot.y * 100}%`,
    width: `${slot.w * 100}%`, height: `${slot.h * 100}%`,
  };
}

function tile(name, color, frame, textColor) {
  const node = el("div", {
    position: "relative", width: "130px", height: "170px", margin: "8px 12px",
    backgroundImage: frame.url ? `url("${frame.url}")` : "none",
    backgroundSize: "100% 100%",
  });
  const iconArea = el("div", { ...slotStyle(frame.iconSlot), display: "flex", alignItems: "center", justifyContent: "center" });
  const iconSize = Math.min(frame.iconSlot.w * 130, frame.iconSlot.h * 170) * 0.9;
  iconArea.append(el("div", {
    width: `${iconSize}px`, height: `${iconSize}px`, borderRadius: "14px", background: color,
    color: "#fff", display: "flex", alignItems: "center", justifyContent: "center",
    fontWeight: "700", fontSize: `${iconSize * 0.45}px`,
  }, name[0]));
  const labelHeight = frame.labelSlot.h * 170;
  const label = el("div", {
    ...slotStyle(frame.labelSlot), color: textColor, display: "flex", alignItems: "center", justifyContent: "center",
    fontSize: `${Math.max(9, labelHeight * 0.6)}px`, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis",
  }, name);
  node.append(iconArea, label);
  return node;
}

function headerButton(text, colors) {
  return el("div", {
    height: "40px", marginLeft: "12px", padding: "0 16px", display: "flex", alignItems: "center",
    border: `2px solid ${colors.accent}`, borderRadius: "8px", color: colors.text, fontSize: "14px", flex: "none",
  }, text);
}

/**
 * Desenha a prévia em `container`.
 * assets: { logoUrl, backgroundUrl, frame: { url, iconSlot, labelSlot }, attribution, version }
 */
export function renderPreview(container, project, assets) {
  const colors = project.colors;
  const stage = el("div", {
    position: "absolute", left: "0", top: "0", width: `${STAGE_WIDTH}px`, height: `${STAGE_HEIGHT}px`,
    transformOrigin: "0 0", background: colors.background, overflow: "hidden",
    fontFamily: "Roboto, system-ui, sans-serif",
  });
  if (assets.backgroundUrl) {
    stage.append(el("div", {
      position: "absolute", inset: "0", backgroundImage: `url("${assets.backgroundUrl}")`,
      backgroundSize: "cover", backgroundPosition: "center",
    }));
  }
  const content = el("div", {
    position: "absolute", inset: "0", padding: "16px 32px 0", display: "flex", flexDirection: "column",
  });

  const header = el("div", { height: "64px", display: "flex", alignItems: "center", flex: "none" });
  if (assets.logoUrl) {
    const logo = el("img", { height: "56px", maxWidth: "160px", objectFit: "contain", flex: "none" });
    logo.src = assets.logoUrl;
    logo.alt = "";
    header.append(logo);
  }
  header.append(
    el("div", {
      marginLeft: "16px", flex: "1", color: colors.text, fontSize: "20px", fontWeight: "700",
      display: "-webkit-box", WebkitLineClamp: "2", WebkitBoxOrient: "vertical", overflow: "hidden",
    }, project.message),
    headerButton("Configurações", colors),
    headerButton("Tema", colors),
  );

  const verse = project.verses[0] ?? { reference: "", text: "" };
  const verseText = el("div", {
    height: "56px", marginTop: "8px", color: colors.text, fontStyle: "italic", fontSize: "18px",
    display: "flex", alignItems: "center", justifyContent: "center", textAlign: "center", overflow: "hidden", flex: "none",
  }, verse.text);
  const reference = el("div", {
    color: colors.text, fontSize: "11px", textAlign: "center", whiteSpace: "nowrap", overflow: "hidden",
    textOverflow: "ellipsis", flex: "none",
  }, verse.reference ? `${verse.reference} (${assets.version}) · ${assets.attribution}` : "");

  const grid = el("div", {
    width: "770px", margin: "0 auto", paddingTop: "8px", display: "flex", flexWrap: "wrap", alignContent: "flex-start",
  });
  SAMPLE_APPS.forEach(([name, color]) => grid.append(tile(name, color, assets.frame, colors.text)));

  content.append(header, verseText, reference, grid);
  stage.append(content);
  container.replaceChildren(stage);
  fitStage(container, stage);
}

function fitStage(container, stage) {
  const scale = container.clientWidth / STAGE_WIDTH;
  stage.style.transform = `scale(${scale})`;
}

export function keepPreviewFitted(container) {
  new ResizeObserver(() => {
    const stage = container.firstElementChild;
    if (stage) fitStage(container, stage);
  }).observe(container);
}
