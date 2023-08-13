const child_process = require("child_process");
const kill = require("tree-kill");
const { app, ipcMain, BrowserWindow, dialog, shell } = require("electron");
const serve = require("electron-serve");
const ws = require("electron-window-state");
const pathModule = require("path");
const fs = require("fs-extra");
let cpy;
import("cpy").then((p) => (cpy = p.default));
const loadURL = serve({ directory: __dirname });
const port = 3000;
const isdev = !app.isPackaged || process.env.NODE_ENV == "development";
const projectsPath = pathModule.join(app.getPath("userData"), "projects.json");
const projects = fs.existsSync(projectsPath)
  ? fs.readJSONSync(projectsPath)
  : {};
let selected = Object.keys(projects).length ? Object.keys(projects)[0] : "";
let child;
let stdout = "";
let stderr = [];
let mainwindow;

function loadVite(port) {
  mainwindow.loadURL(`http://localhost:${port}/`).catch(() => {
    setTimeout(() => {
      loadVite(port);
    }, 200);
  });
}

function createMainWindow() {
  let mws = ws({
    defaultWidth: 1000,
    defaultHeight: 800,
  });

  mainwindow = new BrowserWindow({
    x: mws.x,
    y: mws.y,
    width: mws.width,
    height: mws.height,

    webPreferences: {
      nodeIntegration: true,
      contextIsolation: false,
      devTools: isdev || true,
    },
  });

  mainwindow.once("close", () => {
    mainwindow = null;
  });

  if (!isdev) mainwindow.removeMenu();
  else mainwindow.webContents.openDevTools();
  mws.manage(mainwindow);

  if (isdev) loadVite(port);
  else loadURL(mainwindow);
}
app.once("ready", createMainWindow);
app.on("activate", () => {
  if (!mainwindow) createMainWindow();
});
app.on("window-all-closed", () => {
  if (process.platform !== "darwin") app.quit();
});
app.on("web-contents-created", (event, contents) => {
  contents.setWindowOpenHandler((details) => {
    shell.openExternal(details.url);
    return { action: "deny" };
  });
});
ipcMain.on("appPath", (ev) => {
  ev.returnValue = app.getPath("userData");
});
ipcMain.on("isDev", (ev) => {
  ev.returnValue = isdev;
});
ipcMain.on("selected", (ev) => {
  ev.returnValue = selected;
});
ipcMain.on("isRunning", (ev) => {
  ev.returnValue = child != undefined;
});
ipcMain.on("lastError", (ev) => {
  ev.returnValue = stderr;
});
ipcMain.on("lastLog", (ev) => {
  ev.returnValue = stdout;
});
ipcMain.handle("select", (ev, path) => {
  selected = path;
});
ipcMain.handle("dialog", async (ev, property, ...filters) => {
  const filtersData = [...filters].flat();
  const response = await dialog.showOpenDialog({
    properties: [property].flat(),
    filters: filtersData.length ? filtersData : undefined,
  });
  return response.canceled ? undefined : response;
});
ipcMain.handle("createProject", async (ev, path) => {
  mainwindow.webContents.send("updateStart", "");
  const projectPath = isdev
    ? pathModule.join(__dirname, "..", "Project", "**")
    : pathModule.join(__dirname, "..", "..", "..", "Project", "**");
  await cpy(projectPath, path).on("progress", (progress) => {
    mainwindow.webContents.send(
      "updateProgress",
      (progress.percent * 100).toFixed(0)
    );
  });
  mainwindow.webContents.send("updateStop", "");
});
ipcMain.handle("run", (ev) => {
  const projectPath = pathModule.join(selected, "Project");
  const path = pathModule.join(projectPath, "gradlew.bat");
  child = child_process.spawn(path, ["runClient"], {
    stdio: ["pipe", "pipe", "pipe"],
    cwd: projectPath,
  });
  child.stdout.on("data", (data) => {
    stdout += data.toString();
    mainwindow.webContents.send("log", stdout);
  });
  child.stderr.on("data", (data) => {
    const err = data.toString();
    if (err.includes("Note:")) return;
    stderr.push(
      `[${formatDateToHHMMSS(new Date())}]: ${
        err.startsWith("\n") ? err.replace("\n", "") : err
      }`
    );
    mainwindow.webContents.send("err", stderr);
  });
  child.on("exit", () => {
    child = undefined;
    mainwindow.webContents.send("exit");
  });
});
ipcMain.handle("stop", (ev) => {
  child.stdout.removeAllListeners();
  kill(child.pid);
  child = undefined;
});
ipcMain.handle("addError", (ev, error) => {
  stderr.push(error);
});
ipcMain.handle("clearLogs", (ev) => {
  stderr = [];
  stdout = "";
});
function formatDateToHHMMSS(date) {
  const hours = date.getHours();
  const minutes = date.getMinutes();
  const seconds = date.getSeconds();
  const formattedHours = hours.toString().padStart(2, "0");
  const formattedMinutes = minutes.toString().padStart(2, "0");
  const formattedSeconds = seconds.toString().padStart(2, "0");
  return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
}