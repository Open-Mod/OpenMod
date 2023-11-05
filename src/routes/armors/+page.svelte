<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let armors = {};
  let tabs = {};
  let materials = {};
  let projectPath = "";
  let path = "";
  let projectName = "";
  let tabsPath = "";
  let materialsPath = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "armors.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    materialsPath = pathModule.join(
      projectPath,
      "src",
      "data",
      "materials.json"
    );
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
    armors = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    materials = fs.existsSync(materialsPath)
      ? fs.readJSONSync(materialsPath)
      : {};
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "armor" && !n.showInContext) || n.showInContext);
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
          armors[selectedArmor].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        armors[selectedArmor].node_data.connected_nodes.push({
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
    Object.keys(armors).forEach((armor) => {
      armors[armor].name = armor;
      armors[armor].material = armors[armor].material.trim()
        ? armors[armor].material
        : Object.keys(materials)[0] ?? armors[armor].material;
    });
    selectedArmor = Object.keys(armors)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "armors.json") return;
        armors = data.file.data;
        updateEditor();
      } else if (data.type == "SELECTED") selectedArmor = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "armors.json", data: armors });
    };
  });
  let selectedArmor = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_armor_${Object.keys(armors).length + 1}`;
    armors[name] = {
      name,
      stacksTo: 1,
      type: "helmet",
      material: Object.keys(materials)[0] ?? "",
      tab: "none",
      rarity: "common",
      fuel: false,
      food: false,
      fireResistant: false,
      setRepair: true,
      burnTime: 1,
      food_alwaysEat: false,
      food_fast: false,
      food_meat: false,
      food_nutrition: 0,
      food_saturationMod: 0,
      effects: [],
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
    selectedArmor = name;
    updateEditor();
    send_changes({ file: "armors.json", data: armors });
  }
  function save() {
    const obj = {};
    Object.keys(armors).forEach((armor) => {
      const name = armors[armor].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(armors[armor]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = armors[armor][property];
      });
    });
    fs.writeJSONSync(path, obj);
    armors = obj;
    Object.keys(armors).forEach((armor) => {
      armors[armor].name = armor;
    });
    selectedArmor = armors[selectedArmor]
      ? selectedArmor
      : Object.keys(armors)[0];
    success("Armors saved successfully!");
  }
  function deleteArmor() {
    if (!selectedArmor) return;
    delete armors[selectedArmor];
    armors = armors;
    selectedArmor = Object.keys(armors)[0];
    updateEditor();
    send_changes({ file: "armors.json", data: armors });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture(property, filters) {
    const response = await ipc.invoke("dialog", "openFile", filters);
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const item = fs.readFileSync(splitted.join("/"), "base64");
      armors[selectedArmor][property] = {
        name: splitted[splitted.length - 1],
        data: item,
      };
      send_changes({ file: "armors.json", data: armors });
    }
  }
  function setTexture(property, ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      armors[selectedArmor][property] = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result
          .replace("data:image/png;base64,", "")
          .replace("data:application/json;base64,", ""),
      };
      send_changes({ file: "armors.json", data: armors });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  let editor;
  function setEditor() {
    if (!armors[selectedArmor]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      armors[selectedArmor].node_data.connected_nodes = [];
      armors[selectedArmor].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "armors.json", data: armors });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedArmor || !editor) return;
    editor.graph.configure(armors[selectedArmor].node_data.graph);
    const unadded = [];
    nodes
      .filter((n) => !n.showInContext)
      .forEach((n) => {
        if (
          !editor.graph._nodes.find(
            (node) => node.type == `${n.category}/${n.name}`
          )
        ) {
          const ev = LiteGraph.createNode(`${n.category}/${n.name}`);
          editor.graph.add(ev);
          unadded.push(ev);
        }
      });
    arrangeNodesInGrid(unadded, editor.canvas.height);
  }
  function arrangeNodesInGrid(nodes, containerHeight) {
    let x = 0;
    let y = 30;
    let maxWidth = 0;
    for (let i = 0; i < nodes.length; i++) {
      let node = nodes[i];
      if (y + node.size[1] > containerHeight) {
        y = 30;
        x += maxWidth + 5;
        maxWidth = 0;
      }
      node.pos[0] = x;
      node.pos[1] = y;
      y += node.size[1] + 35;
      maxWidth = Math.max(maxWidth, node.size[0]);
      if (y >= containerHeight) {
        y = 30;
        x += maxWidth + 5;
        maxWidth = 0;
      }
    }
  }
</script>

<svelte:head>
  <title>OpenMod - Armors</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Armor:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedArmor}
      on:change={updateEditor}
    >
      {#if !Object.keys(armors).length}
        <option disabled value={selectedArmor}>No armors</option>
      {/if}
      {#each Object.keys(armors) as armor}
        <option value={armor}>{armor}</option>
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
        <button class="btn btn-error" on:click={deleteArmor}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if armors[selectedArmor]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={armors[selectedArmor].name}
            />
          </div>
          <div>
            <label class="text-lg">Stack Size</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={armors[selectedArmor].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].type}
              ><option value="helmet">Helmet</option>
              <option value="boots">Boots</option>
              <option value="chestplate">Chestplate</option>
              <option value="leggings">Leggings</option></select
            >
          </div>
          <div>
            <label class="text-lg">Material</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].material}
            >
              {#if !Object.keys(materials).length}
                <option disabled value={armors[selectedArmor].material}
                  >No materials</option
                >
              {/if}
              {#each Object.keys(materials) as material}
                <option value={material}
                  >{projectName.toLowerCase()}:{material}</option
                >
              {/each}</select
            >
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].tab}
            >
              <option value="none">None</option>
              {#each Object.keys(tabs) as tab}
                <option value={tab}>{projectName.toLowerCase()}:{tab}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Rarity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].rarity}
              ><option value="common">Common Rarity</option><option
                value="uncommon">Uncommon Rarity</option
              ><option value="rare">Rare Rarity</option><option value="epic"
                >Epic Rarity</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fuel?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].fuel}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if armors[selectedArmor].fuel}
            <div>
              <label class="text-lg">Burn Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={armors[selectedArmor].burnTime}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Food?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].food}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Can Be Repaired?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].setRepair}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div class="col-start-1">
            <label class="text-lg">Item Texture</label>
            <img
              class="w-48 h-48 cursor-pointer rounded-lg"
              src={`data:image/png;base64,${armors[selectedArmor].itemTexture?.data}`}
              on:error={fallbackTexture}
              on:click={chooseTexture.bind(this, "itemTexture", "png")}
              on:drop={setTexture.bind(this, "itemTexture")}
              on:dragover|preventDefault
            />
          </div>
        </div>
      </Accordion>
      {#if armors[selectedArmor].food}
        <Accordion title="Food">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Is Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={armors[selectedArmor].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Can Be Eaten Fast?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={armors[selectedArmor].food_fast}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Is Meat?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={armors[selectedArmor].food_meat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Nutrition</label>
              <input
                type="number"
                min="0"
                max="10"
                step="0.5"
                class="input w-full"
                bind:value={armors[selectedArmor].food_nutrition}
              />
            </div>
            <div>
              <label class="text-lg">Saturation (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={armors[selectedArmor].food_saturationMod}
              />
            </div>
          </div>
        </Accordion>
        <Accordion title="Effects">
          <div class="flex flex-col gap-3">
            {#each armors[selectedArmor].effects as effect, index}
              <div class="grid grid-cols-3 gap-3">
                <div>
                  <label class="text-lg">Effect</label>
                  <select
                    class="select font-normal text-base w-full"
                    bind:value={effect.name}
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
                    <option value="hero_of_the_village"
                      >Hero Of The Village</option
                    >
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
                  <label class="text-lg">Probability (%)</label>
                  <input
                    type="number"
                    min="0"
                    max="100"
                    class="input w-full"
                    bind:value={effect.probability}
                  />
                </div>
                <div>
                  <label class="text-lg">Duration (In Seconds)</label>
                  <input
                    type="number"
                    min="0"
                    class="input w-full"
                    bind:value={effect.duration}
                  />
                </div>
                <div>
                  <label class="text-lg">Level</label>
                  <input
                    type="number"
                    min="1"
                    class="input w-full"
                    bind:value={effect.amplifier}
                  />
                </div>
                <div>
                  <label class="text-lg"> Is Ambient?</label>
                  <select
                    class="select font-normal text-base w-full"
                    bind:value={effect.ambient}
                    ><option value={true}>True</option><option value={false}
                      >False</option
                    ></select
                  >
                </div>
                <div>
                  <label class="text-lg">Is Visible?</label>
                  <select
                    class="select font-normal text-base w-full"
                    bind:value={effect.visible}
                    ><option value={true}>True</option><option value={false}
                      >False</option
                    ></select
                  >
                </div>
                <div>
                  <label class="text-lg">Can Show Icon?</label>
                  <select
                    class="select font-normal text-base w-full"
                    bind:value={effect.showIcon}
                    ><option value={true}>True</option><option value={false}
                      >False</option
                    ></select
                  >
                </div>
                <a
                  class="tooltip tooltip-top mr-auto mt-auto"
                  data-tip="Delete"
                >
                  <button
                    class="btn btn-error text-lg"
                    on:click={deleteEffect.bind(this, index)}
                  >
                    <i class="fa-solid fa-trash" />
                  </button>
                </a>
              </div>
            {/each}
            <a class="tooltip tooltip-top" data-tip="Add">
              <button
                class="btn btn-warning w-full text-lg"
                on:click={addEffect}
              >
                <i class="fa-solid fa-plus" />
              </button>
            </a>
          </div>
        </Accordion>
      {/if}
      <Accordion title="Events" style="overflow:hidden;" mount={setEditor}>
        <div id="editor" class="w-full h-[40vh]" />
      </Accordion>
    {/if}
  </div>
</div>
