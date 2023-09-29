<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let players = {};
  let projectPath = "";
  let path = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "players.json");
    nodesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "plugins",
      "ui"
    );
    players = fs.existsSync(path)
      ? fs.readJSONSync(path)
      : {
          player: {
            node_data: {
              connected_nodes: [],
              graph: {
                last_node_id: 0,
                last_link_id: 0,
                nodes: [],
                links: [],
                groups: [],
                config: {},
                extra: {},
                version: 0.4,
              },
            },
          },
        };
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter(
        (n) => (n.for == "player" && !n.showInContext) || n.showInContext
      );
    nodes.forEach((n) => {
      function node() {
        this.size = n.size;
        for (let index = 0; index < n.inputs.length; index++) {
          const ip = n.inputs[index];
          this.addInput(ip.name, ip.type != "any" ? ip.type : undefined);
        }
        for (let index = 0; index < n.outputs.length; index++) {
          const op = n.outputs[index];
          this.addOutput(op.name, op.type != "any" ? op.type : undefined);
        }
        for (let index = 0; index < n.properties.length; index++) {
          const property = n.properties[index];
          if (property.type.toLowerCase() == "number") {
            this.addProperty(property.name, property.value);
            this.addWidget(
              property.type.toLowerCase(),
              property.name,
              Number(property.value),
              null,
              {
                property: property.name,
                step: property.step * 10,
                precision: property.step < 1 ? undefined : 0,
              }
            );
          } else {
            this.addProperty(property.name, property.value);
            this.addWidget(
              property.type.toLowerCase(),
              property.name,
              property.value,
              null,
              { property: property.name, values: property.values }
            );
          }
        }
      }
      node.title = n.name;
      node.showInContext = n.showInContext;
      node.plugin = n.plugin;
      node.prototype.onExecute = function () {
        if (
          players[selectedPlayer].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        players[selectedPlayer].node_data.connected_nodes.push({
          id: this.id,
          title: this.title,
          properties: this.properties,
          inputs: this.inputs ?? [],
          outputs: this.outputs ?? [],
          plugin: n.plugin,
        });
        for (let index = 0; index < this.outputs?.length; index++) {
          const op = this.outputs[index];
          if (op.type == "connector") this.trigger(op.name);
        }
      };
      LiteGraph.registerNodeType(`${n.category}/${n.name}`, node);
    });
    selectedPlayer = Object.keys(players)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "players.json") return;
      players = data.file.content;
    };
  });
  let selectedPlayer = "";
  let nodes = [];
  function save() {
    fs.writeJSONSync(path, players);
    selectedPlayer = players[selectedPlayer]
      ? selectedPlayer
      : Object.keys(players)[0];
    success("Player saved successfully!");
  }
  let editor;
  function setEditor() {
    if (!players[selectedPlayer]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      players[selectedPlayer].node_data.connected_nodes = [];
      players[selectedPlayer].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "players.json", data: players });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedPlayer || !editor) return;
    editor.graph.configure(players[selectedPlayer].node_data.graph);
    nodes
      .filter((n) => !n.showInContext)
      .forEach((n, i) => {
        if (
          !editor.graph._nodes.find(
            (node) => node.type == `${n.category}/${n.name}`
          )
        ) {
          const ev = LiteGraph.createNode(`${n.category}/${n.name}`);
          editor.graph.add(ev);
          const node = editor.graph._nodes[i - 1]?.getBounding();
          const y = node ? node[1] + node[3] + 50 : 30;
          const x = node ? node[0] + node[2] + 20 : 30;
          if (y < editor.canvas.height) ev.pos = [30, y];
          else ev.pos = [x, 30];
        }
      });
  }
</script>

<svelte:head>
  <title>OpenMod - Players</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <div class="flex flex-row w-full gap-3">
    <a class="tooltip tooltip-top ml-auto" data-tip="Save">
      <button class="btn btn-success" on:click={save}
        ><i class="fa-solid fa-floppy-disk text-lg" /></button
      >
    </a>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if players[selectedPlayer]}
      <Accordion title="Events" style="overflow:hidden;" mount={setEditor}>
        <div id="editor" class="w-full h-[40vh]" />
      </Accordion>
    {/if}
  </div>
</div>
