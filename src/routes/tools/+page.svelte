<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let tools = {};
  let tabs = {};
  let tiers = {};
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let tiersPath = "";
  let nodesPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "tools.json");
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    tiersPath = pathModule.join(projectPath, "src", "data", "tiers.json");
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
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    tools = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    tiers = fs.existsSync(tiersPath) ? fs.readJSONSync(tiersPath) : {};
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "tool" && !n.showInContext) || n.showInContext);
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
          } else if (property.type.toLowerCase() == "button") {
            this.addWidget(
              property.type.toLowerCase(),
              property.name,
              null,
              property.callback,
              { property: property.name }
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
          tools[selectedTool].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        tools[selectedTool].node_data.connected_nodes.push({
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
    Object.keys(tools).forEach((tool) => {
      tools[tool].name = tool;
      tools[tool].tier = tools[tool].tier.trim()
        ? tools[tool].tier
        : Object.keys(tiers)[0] ?? tools[tool].tier;
    });
    selectedTool = Object.keys(tools)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "tools.json") return;
      tools = data.file.content;
    };
  });
  let selectedTool = "";
  let name = "";
  let error = "";
  let success = "";
  let nodes = [];
  function add() {
    name = `new_tool_${Object.keys(tools).length + 1}`;
    tools[name] = {
      name,
      stacksTo: 1,
      attackDamage: 0,
      attackSpeed: 1,
      type: "sword",
      tier: Object.keys(tiers)[0] ?? "",
      tab: "none",
      rarity: "common",
      fireResistant: false,
      setRepair: true,
      modelType: "default",
      model: "",
      texture: [""],
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
    selectedTool = name;
    updateEditor();
    send_changes({ file: "tools.json", content: tools });
  }
  function save() {
    const obj = {};
    const toolTextures = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "tool"
    );
    const toolModels = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "tool"
    );
    fs.rmSync(toolModels, { recursive: true, force: true });
    fs.rmSync(toolTextures, { recursive: true, force: true });
    fs.mkdirSync(toolModels);
    fs.mkdirSync(toolTextures);
    Object.keys(tools).forEach((tool) => {
      const name = tools[tool].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const modelPath = pathModule.join(toolModels, `${name}.json`);
      Object.keys(tools[tool]).forEach((property) => {
        if (property == "name") return;
        if (property == "texture" && tools[tool].modelType == "default") {
          const texture = tools[tool][property][0];
          if (texture) {
            const textureType = texture.match(/[^:/]\w+(?=;|,)/)[0];
            const texturePath = pathModule.join(
              toolTextures,
              `${name}.${textureType}`
            );
            const textureData = texture.match(
              /^data:([A-Za-z-+\/]+);base64,(.+)$/
            )[2];
            fs.writeFileSync(texturePath, textureData, "base64");
            fs.writeJSONSync(modelPath, {
              parent: "minecraft:tool/generated",
              textures: {
                layer0: `${projectName.toLowerCase()}:tool/${name}`,
              },
            });
          }
        } else if (
          property == "texture" &&
          tools[tool].modelType == "blockbench"
        ) {
          const model = tools[tool].model;
          const modelData = model.data.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          const textures = tools[tool][property];
          textures.forEach((texture) => {
            const texturePath = pathModule.join(
              toolTextures,
              `${texture.name}`
            );
            const textureData = texture.data.match(
              /^data:([A-Za-z-+\/]+);base64,(.+)$/
            )[2];
            fs.writeFileSync(texturePath, textureData, "base64");
          });
          fs.writeFileSync(modelPath, modelData, "base64");
        }
        obj[name][property] = tools[tool][property];
      });
    });
    fs.writeJSONSync(path, obj);
    tools = obj;
    Object.keys(tools).forEach((tool) => {
      tools[tool].name = tool;
    });
    selectedTool = Object.keys(tools)[0];
    success = "Tools saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteTool() {
    if (!selectedTool) return;
    delete tools[selectedTool];
    tools = tools;
    selectedTool = Object.keys(tools)[0];
    updateEditor();
    send_changes({ file: "tools.json", content: tools });
  }
  async function chooseModel() {
    const response = await ipc.invoke("dialog", [
      "openFile",
      "multiSelections",
    ]);
    if (response) {
      const paths = response.filePaths
        .sort((file) => (file.endsWith(".json") ? -1 : 1))
        .map((file) => ({
          name: file.split("\\")[file.split("\\").length - 1],
          data: `data:${
            file.endsWith(".json") ? `application/json` : `image/png`
          };base64,${fs.readFileSync(file.split("\\").join("/"), "base64")}`,
        }));
      const model = paths.shift();
      tools[selectedTool].model = model;
      tools[selectedTool].texture = paths;
      send_changes({ file: "tools.json", content: tools });
    }
  }
  function setModel(ev) {
    let i = 0;
    const reader = new FileReader();
    reader.onload = function (event) {
      if (i == 0) {
        tools[selectedTool].model = {
          name: files[i].name,
          data: event.target.result,
        };
        tools[selectedTool].texture = [];
      } else
        tools[selectedTool].texture.push({
          name: files[i].name,
          data: event.target.result,
        });
    };
    reader.onloadend = function () {
      i++;
      if (!files[i])
        return send_changes({ file: "tools.json", content: tools });
      reader.readAsDataURL(files[i]);
    };
    const files = [...ev.dataTransfer.files].sort((file) =>
      file.name.endsWith(".json") ? -1 : 1
    );
    reader.readAsDataURL(files[0]);
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      tools[selectedTool].texture = `data:image/png;base64,${texture}`;
      send_changes({ file: "tools.json", content: tools });
    }
  }
  function setTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      tools[selectedTool].texture = event.target.result;
      send_changes({ file: "tools.json", content: tools });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  let editor;
  function setEditor() {
    if (!tools[selectedTool]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      tools[selectedTool].node_data.connected_nodes = [];
      tools[selectedTool].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "tools.json", content: tools });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedTool || !editor) return;
    editor.graph.configure(tools[selectedTool].node_data.graph);
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
  <title>OpenMod - Tools</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected tool:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedTool}
      on:change={updateEditor}
    >
      {#if !Object.keys(tools).length}
        <option disabled value={selectedTool}>No tools</option>
      {/if}
      {#each Object.keys(tools) as tool}
        <option value={tool}>{tool}</option>
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
        <button class="btn btn-error" on:click={deleteTool}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if tools[selectedTool]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={tools[selectedTool].name}
            />
          </div>
          <div>
            <label class="text-lg">Stack Size</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={tools[selectedTool].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Attack Damage</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={tools[selectedTool].attackDamage}
            />
          </div>
          <div>
            <label class="text-lg">Attack Speed (%)</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={tools[selectedTool].attackSpeed}
            />
          </div>
          <div>
            <label class="text-lg">Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].type}
              ><option value="sword">Sword</option>
              <option value="pickaxe">Pickaxe</option>
              <option value="axe">Axe</option>
              <option value="shovel">Shovel</option>
              <option value="hoe">Hoe</option></select
            >
          </div>
          <div>
            <label class="text-lg">Tier</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].tier}
            >
              {#if !Object.keys(tiers).length}
                <option disabled value={tools[selectedTool].tier}
                  >No tiers</option
                >
              {/if}
              {#each Object.keys(tiers) as tier}
                <option value={tier}>{convertToCamelCase(tier)}</option>
              {/each}</select
            >
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].tab}
            >
              <option value="none">None</option>
              {#each Object.keys(tabs) as tab}
                <option value={tab}>{convertToCamelCase(tab)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Rarity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].rarity}
              ><option value="common">Common Rarity</option><option
                value="uncommon">Uncommon Rarity</option
              ><option value="rare">Rare Rarity</option><option value="epic"
                >Epic Rarity</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Can Be Repaired?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].setRepair}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tools[selectedTool].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
          </div>
          {#if tools[selectedTool].modelType == "blockbench"}
            <div class="col-start-1">
              <label class="text-lg">Textures & Model</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={tools[selectedTool].texture[0]?.data ?? ""}
                on:error={fallbackTexture}
                on:click={chooseModel}
                on:drop={setModel}
                on:dragover|preventDefault
              />
            </div>
          {/if}
          {#if tools[selectedTool].modelType == "default"}
            <div class="col-start-1">
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={tools[selectedTool].texture[0]}
                on:error={fallbackTexture}
                on:click={chooseTexture}
                on:drop={setTexture}
                on:dragover|preventDefault
              />
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
<Error {error} />
<Success {success} />
