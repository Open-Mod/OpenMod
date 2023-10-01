<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let potions = {};
  let tabs = {};
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "potions.json");
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
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
    potions = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter(
        (n) => (n.for == "potion" && !n.showInContext) || n.showInContext
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
          potions[selectedPotion].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        potions[selectedPotion].node_data.connected_nodes.push({
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
    Object.keys(potions).forEach((potion) => {
      potions[potion].name = potion;
    });
    selectedPotion = Object.keys(potions)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "potions.json") return;
      potions = data.file.content;
      updateEditor();
    };
    window.onchange = () => {
      send_changes({ file: "potions.json", data: potions });
    };
  });
  let selectedPotion = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_potion_${Object.keys(potions).length + 1}`;
    potions[name] = {
      name,
      effect: "absorption",
      duration: 0,
      amplifier: 1,
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
    };
    selectedPotion = name;
    updateEditor();
    send_changes({ file: "potions.json", data: potions });
  }
  function save() {
    const obj = {};
    Object.keys(potions).forEach((potion) => {
      const name = potions[potion].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(potions[potion]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = potions[potion][property];
      });
    });
    fs.writeJSONSync(path, obj);
    potions = obj;
    Object.keys(potions).forEach((potion) => {
      potions[potion].name = potion;
    });
    selectedPotion = potions[selectedPotion]
      ? selectedPotion
      : Object.keys(potions)[0];
    success("Potions saved successfully!");
  }
  function deletePotion() {
    if (!selectedPotion) return;
    delete potions[selectedPotion];
    potions = potions;
    selectedPotion = Object.keys(potions)[0];
    updateEditor();
    send_changes({ file: "potions.json", data: potions });
  }
  let editor;
  function setEditor() {
    if (!potions[selectedPotion]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      potions[selectedPotion].node_data.connected_nodes = [];
      potions[selectedPotion].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "potions.json", data: potions });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedPotion || !editor) return;
    editor.graph.configure(potions[selectedPotion].node_data.graph);
    [...nodes.filter((n) => !n.showInContext)].forEach((n, i) => {
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
  function convertToCamelCase(inputString) {
    const words = inputString.split("_");
    const convertedString = words
      .map((word) => {
        return word.charAt(0).toUpperCase() + word.slice(1);
      })
      .join(" ");
    return convertedString;
  }
</script>

<svelte:head>
  <title>OpenMod - Potions</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Potion:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedPotion}
      on:change={updateEditor}
    >
      {#if !Object.keys(potions).length}
        <option disabled value={selectedPotion}>No potions</option>
      {/if}
      {#each Object.keys(potions) as potion}
        <option value={potion}>{potion}</option>
      {/each}
    </select>
    <div class="flex gap-1">
      <a class="tooltip tooltip-top" data-tip="Add">
        <button class="btn btn-warning" on:click={add}
          ><i class="fa-solid fa-plus text-lg" /></button
        ></a
      >
      <a class="tooltip tooltip-top" data-tip="Save">
        <button class="btn btn-success" on:click={save}
          ><i class="fa-solid fa-floppy-disk text-lg" /></button
        >
      </a>
      <a class="tooltip tooltip-top" data-tip="Delete">
        <button class="btn btn-error" on:click={deletePotion}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if potions[selectedPotion]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={potions[selectedPotion].name}
            />
          </div>
          <div>
            <label class="text-lg">Effect</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={potions[selectedPotion].effect}
            >
              <option value="absorption">Absorption</option>
              <option value="bad_omen">Bad Omen</option>
              <option value="blindness">Blindness</option>
              <option value="confusion">Nausea</option>
              <option value="conduit_power">Conduit Power</option>
              <option value="damage_boost">Damage Boost</option>
              <option value="damage_resistance">Damage Resistance</option>
              <option value="darkness">Darkness</option>
              <option value="dig_slowdown">Dig Slowdown</option>
              <option value="dig_speed">Dig Speed</option>
              <option value="dolphins_grace">Dolphin's Grace</option>
              <option value="fire_resistance">Fire Resistance</option>
              <option value="glowing">Glowing</option>
              <option value="harm">Harm</option>
              <option value="heal">Heal</option>
              <option value="health_boost">Health Boost</option>
              <option value="hero_of_the_village">Hero Of The Village</option>
              <option value="hunger">Hunger</option>
              <option value="invisibility">Invisibility</option>
              <option value="jump">Jump</option>
              <option value="levitation">Levitation</option>
              <option value="luck">Luck</option>
              <option value="movement_slowdown">Movement Slowdown</option>
              <option value="movement_speed">Movement Speed</option>
              <option value="night_vision">Night Vision</option>
              <option value="poison">Poison</option>
              <option value="regeneration">Regeneration</option>
              <option value="saturation">Saturation</option>
              <option value="slow_falling">Slow Falling</option>
              <option value="unluck">Unluck</option>
              <option value="water_breathing">Water Breathing</option>
              <option value="weakness">Weakness</option>
              <option value="wither">Wither</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Duration (In Seconds)</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={potions[selectedPotion].duration}
            />
          </div>
          <div>
            <label class="text-lg">Level</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={potions[selectedPotion].amplifier}
            />
          </div>
        </div>
      </Accordion>
      <Accordion title="Events" style="overflow:hidden;" mount={setEditor}>
        <div id="editor" class="w-full h-[40vh]" />
      </Accordion>
    {/if}
  </div>
</div>
