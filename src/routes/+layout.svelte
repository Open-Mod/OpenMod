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
  const peers = {};
  const states = {};
  let sel;
  onMount(async () => {
    PeerJS = (await import("peerjs")).default;
    window.error = err;
    window.success = succ;
    window.connect = connect;
    window.host = host;
    window.disconnect = disconnect;
    window.send_changes = send_changes;
    window.getState = getState;
    sel = selected;
    ipc.on("select", (ev, p) => {
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
      const conns = [peer.connect(id)];
      const current = selected;
      const dataPath = pathModule.join(current, "Project", "src", "data");
      conns[0].on("data", (packet) => {
        const data = JSON.parse(packet);
        if (data.type == "SETUP") {
          data.files.forEach((file) => {
            fs.writeJSONSync(pathModule.join(dataPath, file.file), file.data);
          });
        } else if (data.type == "CHANGE") {
          fs.writeJSONSync(
            pathModule.join(dataPath, data.flle.file),
            data.file.data
          );
          if (current != selected) return;
          window.on_change?.(data);
        } else if (data.type == "USERS") {
          states[selected].users = data.data;
        }
      });
      peer.on("open", function (i) {
        resolve(i);
        states[selected] = { id: i, connid: id, state: 2, users: 0 };
      });
      peers[selected] = { peer, conns };
    });
  }
  function host() {
    return new Promise((resolve) => {
      const peer = new PeerJS();
      const conns = [];
      const current = selected;
      const dataPath = pathModule.join(current, "Project", "src", "data");
      peer.on("connection", (conn) => {
        conns.push(conn);
        conn.on("data", (packet) => {
          const data = JSON.parse(packet);
          if (data.type == "CHANGE") {
            fs.writeJSONSync(
              pathModule.join(dataPath, data.flle.file),
              data.file.data
            );
            if (current != selected) return;
            window.on_change?.(data);
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
            })
          );
          conns.forEach((conn) =>
            conn.send(JSON.stringify({ type: "USERS", data: conns.length }))
          );
          states[selected].users = conns.length;
        });
        conn.on("close", () =>
          conn.send(JSON.stringify({ type: "USERS", data: conns.length }))
        );
        peers[selected] = { peer, conns };
      });
      peer.on("open", function (i) {
        resolve(i);
        states[selected] = { id: i, connid: "", state: 1, users: 0 };
      });
      peers[selected] = { peer, conns };
    });
  }
  function disconnect() {
    if (!peers[selected]) return "";
    peers[selected].peer.destroy();
    states[selected] = { id: "", connid: "", state: 0, users: 0 };
    return "";
  }
  function send_changes(file) {
    if (!peers[selected]) return;
    const packet = JSON.stringify({ type: "CHANGE", file });
    peers[selected].conns.forEach((conn) => {
      conn.send(packet);
    });
  }
  function getState() {
    return states[selected] ?? { id: "", connid: "", state: 0, users: 0 };
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
    <MenuItem tip="Recpies" page="/recipes">
      <i class="fa-solid fa-scroll" />
    </MenuItem>
    <MenuItem tip="Biomes" page="/biomes">
      <i class="fa-solid fa-mountain-sun" />
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
    {#if states[sel]?.state}
      <a class="tooltip tooltip-top" data-tip="Users">
        <button class="btn btn-neutral rounded-full relative w-12"
          ><i class="fa-solid fa-user-group text-lg" />
          <div
            class="badge badge-info rounded-full w-3 absolute top-[-5px] right-[-5px]"
          >
            {states[sel].users + 1}
          </div>
        </button>
      </a>
    {/if}
  </div>
</div>
<Error {error} />
<Success {success} />
