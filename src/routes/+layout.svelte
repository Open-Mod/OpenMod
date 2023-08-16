<script>
  import "../app.css";
  import { onMount } from "svelte";
  import Menu from "../components/Menu.svelte";
  import MenuItem from "../components/MenuItem.svelte";
  let PeerJS;
  const peers = {};
  const states = {};
  onMount(async () => {
    PeerJS = (await import("peerjs")).default;
    window.connect = connect;
    window.host = host;
    window.disconnect = disconnect;
    window.send_changes = send_changes;
    window.getState = getState;
    ipc.on("select", (ev, p) => {
      selected = p;
    });
  });
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
          fs.writeJSONSync(pathModule.join(dataPath, data.flle));
          if (current != selected) return;
          window.on_change?.(data);
        }
      });
      peer.on("open", function (i) {
        resolve(i);
        states[selected] = { id: i, connid: id, state: 2 };
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
        });
        peers[selected] = { peer, conns };
      });
      peer.on("open", function (i) {
        resolve(i);
        states[selected] = { id: i, connid: "", state: 1 };
      });
      peers[selected] = { peer, conns };
    });
  }
  function disconnect() {
    if (!peers[selected]) return "";
    peers[selected].peer.destroy();
    states[selected] = { id: "", connid: "", state: 0 };
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
    return states[selected] ?? { id: "", connid: "", state: 0 };
  }
</script>

<div class="flex w-screen h-screen" data-theme="dark">
  <Menu>
    <MenuItem tip="Home" page="/">
      <i class="fa-solid fa-house text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Items" page="/items">
      <i class="fa-solid fa-apple-whole text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Blocks" page="/blocks">
      <i class="fa-solid fa-cube text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Armor" page="/armor">
      <i class="fa-solid fa-shield-halved text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Tools" page="/tools">
      <i class="fa-solid fa-screwdriver-wrench text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Tool Tiers" page="/tiers">
      <i class="fa-solid fa-ranking-star text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Potions" page="/potions">
      <i class="fa-solid fa-flask text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Creative Tabs" page="/tabs">
      <i class="fa-solid fa-layer-group text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Loot Tables" page="/loottables">
      <i class="fa-solid fa-gift text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Recpies" page="/recipes">
      <i class="fa-solid fa-table-cells text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Biomes" page="/biomes">
      <i class="fa-solid fa-mountain-sun text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Trees" page="/trees">
      <i class="fa-solid fa-tree text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Collaboration" page="/collaboration">
      <i class="fa-solid fa-user-plus text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Run" page="/run">
      <i class="fa-solid fa-play text-center inline-block" />
    </MenuItem>
  </Menu>
  <slot />
</div>
