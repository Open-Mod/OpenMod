<script>
  import "../app.css";
  import { onMount } from "svelte";
  import Menu from "../components/Menu.svelte";
  import MenuItem from "../components/MenuItem.svelte";
  import Error from "../components/Error.svelte";
  import Success from "../components/Success.svelte";
  let PeerJS;
  let error = "";
  let success = "";
  let currentPeer = {};
  let state = {};
  let projects = [];
  let projectsPath = "";
  let sel;
  onMount(async () => {
    PeerJS = (await import("peerjs")).default;
    window.error = err;
    window.success = succ;
    window.connect = connect;
    window.host = host;
    window.disconnect = disconnect;
    window.send_changes = send_changes;
    window.send_selected = send_selected;
    window.send_plugin = send_plugin;
    window.getState = getState;
    projectsPath = pathModule.join(appPath, "projects.json");
    projects = fs.readJSONSync(projectsPath);
    sel = selected;
    ipc.on("select", (ev, p) => {
      disconnect();
      selected = p;
      sel = selected;
    });
  });
  function err(err) {
    error = err;
    setTimeout(() => (error = ""), 2000);
  }
  function succ(succ) {
    success = succ;
    setTimeout(() => (success = ""), 2000);
  }
  function connect(id) {
    return new Promise((resolve) => {
      const peer = new PeerJS();
      peer.on("open", (i) => {
        state = { id: i, connid: id, state: 2, users: 0 };
        resolve(i);
        const conns = [peer.connect(id)];
        const current = selected;
        const dataPath = pathModule.join(current, "Project", "src", "data");
        const pluginsPath = pathModule.join(
          current,
          "Project",
          "src",
          "main",
          "java",
          "dev",
          "openmod",
          "plugins"
        );
        const pluginsModPath = pathModule.join(pluginsPath, "mod");
        const pluginsUIPath = pathModule.join(pluginsPath, "ui");
        conns[0].on("data", (packet) => {
          const data = JSON.parse(packet);
          if (data.type == "SETUP") {
            data.files.forEach((file) => {
              fs.writeJSONSync(pathModule.join(dataPath, file.file), file.data);
            });
            fs.rmdirSync(pluginsModPath, { force: true, recursive: true });
            fs.rmdirSync(pluginsUIPath, { force: true, recursive: true });
            fs.mkdirSync(pluginsModPath);
            fs.mkdirSync(pluginsUIPath);
            data.plugins.mod.forEach((file) => {
              fs.writeFileSync(
                pathModule.join(pluginsModPath, file.file),
                file.data
              );
            });
            data.plugins.ui.forEach((file) => {
              fs.writeFileSync(
                pathModule.join(pluginsUIPath, file.file),
                file.data
              );
            });
            projects[selected].plugins = data.plugins.data;
            fs.writeJSONSync(projectsPath, projects);
            window.on_plugin?.(projects[selected].plugins);
          } else if (data.type == "CHANGE") {
            fs.writeJSONSync(
              pathModule.join(dataPath, data.file.file),
              data.file.data
            );
            window.on_change?.(data);
          } else if (data.type == "SELECTED") {
            window.on_change?.(data);
          } else if (data.type == "PLUGIN") {
            fs.writeFileSync(
              pathModule.join(pluginsModPath, data.plugin.mod.file),
              data.plugin.mod.data
            );
            fs.writeJSONSync(
              pathModule.join(pluginsUIPath, data.plugin.ui.file),
              data.plugin.ui.data
            );
            projects[selected].plugins = data.data;
            fs.writeJSONSync(projectsPath, projects);
            window.on_plugin?.(projects[selected].plugins);
          } else if (data.type == "USERS") {
            state.users = data.data;
            state = state;
          }
        });
        currentPeer = { peer, conns };
      });
    });
  }
  function host() {
    return new Promise((resolve) => {
      const peer = new PeerJS();
      const conns = [];
      const current = selected;
      const dataPath = pathModule.join(current, "Project", "src", "data");
      const pluginsPath = pathModule.join(
        current,
        "Project",
        "src",
        "main",
        "java",
        "dev",
        "openmod",
        "plugins"
      );
      const pluginsModPath = pathModule.join(pluginsPath, "mod");
      const pluginsUIPath = pathModule.join(pluginsPath, "ui");
      window.peer = peer;
      peer.on("connection", (conn) => {
        conns.push(conn);
        conn.on("data", (packet) => {
          const data = JSON.parse(packet);
          if (data.type == "CHANGE") {
            fs.writeJSONSync(
              pathModule.join(dataPath, data.file.file),
              data.file.data
            );
            window.on_change?.(data);
          } else if (data.type == "SELECTED") {
            window.on_change?.(data);
          } else if (data.type == "PLUGIN") {
            fs.writeFileSync(
              pathModule.join(pluginsModPath, data.plugin.mod.file),
              data.plugin.mod.data
            );
            fs.writeJSONSync(
              pathModule.join(pluginsUIPath, data.plugin.ui.file),
              data.plugin.ui.data
            );
            projects[selected].plugins = data.data;
            fs.writeJSONSync(projectsPath, projects);
            window.on_plugin?.(projects[selected].plugins);
          }
        });
        conn.on("open", () => {
          conn.send(
            JSON.stringify({
              type: "SETUP",
              files: fs.readdirSync(dataPath).map((file) => ({
                file,
                data: fs.readJSONSync(pathModule.join(dataPath, file)),
              })),
              plugins: {
                mod: fs.readdirSync(pluginsModPath).map((file) => ({
                  file,
                  data: fs
                    .readFileSync(pathModule.join(pluginsModPath, file))
                    .toString(),
                })),
                ui: fs.readdirSync(pluginsUIPath).map((file) => ({
                  file,
                  data: fs
                    .readFileSync(pathModule.join(pluginsUIPath, file))
                    .toString(),
                })),
                data: projects[selected].plugins,
              },
            })
          );
          conns.forEach((conn) =>
            conn.send(JSON.stringify({ type: "USERS", data: conns.length }))
          );
          state.users = conns.length;
          state = state;
        });
        conn.on("close", () => {
          conns.splice(conns.indexOf(conn, 1));
          conns.forEach((conn) =>
            conn.send(JSON.stringify({ type: "USERS", data: conns.length }))
          );
          state.users = conns.length;
          state = state;
        });
        currentPeer = { peer, conns };
      });
      peer.on("open", function (i) {
        resolve(i);
        state = { id: i, connid: "", state: 1, users: 0 };
      });
      currentPeer = { peer, conns };
    });
  }
  function disconnect() {
    if (!currentPeer.peer) return "";
    currentPeer.peer.destroy();
    state = { id: "", connid: "", state: 0, users: 0 };
    return "";
  }
  function send_changes(file) {
    if (!currentPeer.peer) return;
    const packet = JSON.stringify({ type: "CHANGE", file });
    currentPeer.conns.forEach((conn) => {
      conn.send(packet);
    });
  }
  function send_selected(s) {
    if (!currentPeer.peer) return;
    const packet = JSON.stringify({ type: "SELECTED", selected: s });
    currentPeer.conns.forEach((conn) => {
      conn.send(packet);
    });
  }
  function send_plugin(p, d) {
    if (!currentPeer.peer) return;
    const packet = JSON.stringify({ type: "PLUGIN", plugin: p, data: d });
    currentPeer.conns.forEach((conn) => {
      conn.send(packet);
    });
  }
  function getState() {
    return state.id ? state : { id: "", connid: "", state: 0, users: 0 };
  }
  function devtools() {
    ipc.invoke("devtools");
  }
</script>

<div class="flex w-screen h-screen" data-theme="dark">
  <Menu>
    <MenuItem tip="Home" page="/">
      <i class="fa-solid fa-house" />
    </MenuItem>
    <MenuItem tip="Items" page="/items">
      <i class="fa-solid fa-apple-whole" />
    </MenuItem>
    <MenuItem tip="Blocks" page="/blocks">
      <i class="fa-solid fa-cube" />
    </MenuItem>
    <MenuItem tip="Armors" page="/armors">
      <i class="fa-solid fa-shield-halved" />
    </MenuItem>
    <MenuItem tip="Tools" page="/tools">
      <i class="fa-solid fa-screwdriver-wrench" />
    </MenuItem>
    <MenuItem tip="Armor Materials" page="/materials">
      <i class="fa-solid fa-gem" />
    </MenuItem>
    <MenuItem tip="Tool Tiers" page="/tiers">
      <i class="fa-solid fa-ranking-star" />
    </MenuItem>
    <MenuItem tip="Mobs" page="/mobs">
      <i class="fa-solid fa-paw" />
    </MenuItem>
    <MenuItem tip="Potions" page="/potions">
      <i class="fa-solid fa-flask" />
    </MenuItem>
    <MenuItem tip="Sounds" page="/sounds">
      <i class="fa-solid fa-volume-high" />
    </MenuItem>
    <MenuItem tip="Creative Tabs" page="/tabs">
      <i class="fa-solid fa-layer-group" />
    </MenuItem>
    <MenuItem tip="Loot Tables" page="/loottables">
      <i class="fa-solid fa-gift" />
    </MenuItem>
    <MenuItem tip="Recipes" page="/recipes">
      <i class="fa-solid fa-scroll" />
    </MenuItem>
    <MenuItem tip="Biomes" page="/biomes">
      <i class="fa-solid fa-mountain-sun" />
    </MenuItem>
    <MenuItem tip="Structures" page="/structures">
      <i class="fa-solid fa-building" />
    </MenuItem>
    <MenuItem tip="Trees" page="/trees">
      <i class="fa-solid fa-tree" />
    </MenuItem>
    <MenuItem tip="Player" page="/player">
      <i class="fa-solid fa-user-pen" />
    </MenuItem>
    <MenuItem tip="Collaboration" page="/collaboration">
      <i class="fa-solid fa-user-plus" />
    </MenuItem>
    <MenuItem tip="Plugins" page="/plugins">
      <i class="fa-solid fa-puzzle-piece" />
    </MenuItem>
    <MenuItem tip="Run" page="/run">
      <i class="fa-solid fa-play" />
    </MenuItem>
  </Menu>
  <slot />
  <div class="absolute bottom-3 right-3">
    <a class="tooltip tooltip-top" data-tip="Dev Tools">
      <button
        class="btn btn-neutral rounded-full relative w-12"
        on:click={devtools}
        ><i class="fa-solid fa-terminal text-lg" />
      </button>
    </a>
    {#if state.state}
      <a class="tooltip tooltip-top" data-tip="Users">
        <button class="btn btn-neutral rounded-full relative w-12"
          ><i class="fa-solid fa-user-group text-lg" />
          <div
            class="badge badge-info rounded-full w-3 absolute top-[-5px] right-[-5px]"
          >
            {state.users + 1}
          </div>
        </button>
      </a>
    {/if}
  </div>
</div>
<Error {error} />
<Success {success} />
