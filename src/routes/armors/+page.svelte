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
      if (data.file.file != "armors.json") return;
      armors = data.file.content;
      updateEditor();
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
      fireResistant: false,
      setRepair: true,
      modelType: "default",
      burnTime: 1,
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
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={armors[selectedArmor].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
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
          {#if armors[selectedArmor].modelType == "blockbench"}
            <div>
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${armors[selectedArmor].texture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture", "png")}
                on:drop={setTexture.bind(this, "texture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Geo</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{armors[selectedArmor].geo
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "geo", "json")}
                on:drop={setTexture.bind(this, "geo")}
                on:dragover|preventDefault
              >
                {armors[selectedArmor].geo?.name ?? ""}
              </div>
            </div>
            <div>
              <label class="text-lg">Animations</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{armors[selectedArmor].animation
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "animation", "json")}
                on:drop={setTexture.bind(this, "animation")}
                on:dragover|preventDefault
              >
                {armors[selectedArmor].animation?.name ?? ""}
              </div>
            </div>
          {/if}
        </div>
      </Accordion>
      <Accordion title="Events" style="overflow:hidden;" mount={setEditor}>
        <div id="editor" class="w-full h-[40vh]" />
      </Accordion>
    {/if}
  </div>
</div>
