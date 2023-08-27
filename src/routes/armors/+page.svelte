<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let armors = {};
  let tabs = {};
  let materials = {};
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let materialsPath = "";
  let nodesPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "armors.json");
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
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
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
      model: "",
      texture: [],
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
    send_changes({ file: "armors.json", content: armors });
  }
  function save() {
    const obj = {};
    const armorAnimations = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "animations"
    );
    const armorGeos = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "geo"
    );
    const armorModels = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "item"
    );
    const armorTextures = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "item"
    );
    const old = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    Object.keys(old).forEach((armor) => {
      const itemTexturePath = pathModule.join(
        armorTextures,
        `${armor}_item.png`
      );
      const texturePath = pathModule.join(armorTextures, `${armor}.png`);
      const animationPath = pathModule.join(
        armorAnimations,
        `${armor}.animation.json`
      );
      const geoPath = pathModule.join(armorGeos, `${armor}.geo.json`);
      if (fs.existsSync(itemTexturePath)) fs.rmSync(itemTexturePath);
      if (fs.existsSync(texturePath)) fs.rmSync(texturePath);
      if (fs.existsSync(animationPath)) fs.rmSync(animationPath);
      if (fs.existsSync(geoPath)) fs.rmSync(geoPath);
    });
    Object.keys(armors).forEach((armor) => {
      const oldModel = pathModule.join(armorModels, `${armor}.json`);
      if (fs.existsSync(oldModel)) fs.rmSync(oldModel);
      const name = armors[armor].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const modelPath = pathModule.join(armorModels, `${name}.json`);
      const itemTexture = armors[armor].itemTexture;
      if (itemTexture) {
        const itemTexturePath = pathModule.join(
          armorTextures,
          `${name}_item.png`
        );
        const itemTextureData = itemTexture.data.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        fs.writeFileSync(itemTexturePath, itemTextureData, "base64");
      }
      if (armors[armor].modelType == "default") {
        fs.writeJSONSync(modelPath, {
          parent: "minecraft:item/generated",
          textures: {
            layer0: `${projectName.toLowerCase()}:item/${name}_item`,
          },
        });
      } else if (armors[armor].modelType == "blockbench") {
        fs.writeJSONSync(modelPath, {
          parent: "minecraft:item/generated",
          textures: {
            layer0: `${projectName.toLowerCase()}:item/${name}_item`,
          },
        });
        const model = armors[armor].model[0];
        const geoPath = pathModule.join(armorGeos, `${name}.geo.json`);
        const modelData = model.data.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        const animationPath = pathModule.join(
          armorAnimations,
          `${name}.animation.json`
        );
        const textures = armors[armor].texture;
        const texturePath = pathModule.join(armorTextures, `${name}.png`);
        const textureData = textures[1].data.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        fs.writeFileSync(texturePath, textureData, "base64");
        fs.writeFileSync(geoPath, modelData, "base64");
        fs.writeJSONSync(animationPath, {
          format_version: "1.8.0",
          animations: {
            idle: {
              loop: true,
            },
          },
          geckolib_format_version: 2,
        });
      }
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
    selectedArmor = Object.keys(armors)[0];
    success("Armors saved successfully!");
  }
  function deleteArmor() {
    if (!selectedArmor) return;
    delete armors[selectedArmor];
    armors = armors;
    selectedArmor = Object.keys(armors)[0];
    updateEditor();
    send_changes({ file: "armors.json", content: armors });
  }
  async function chooseModel() {
    const response = await ipc.invoke("dialog", [
      "openFile",
      "multiSelections"
    ],["json", "png"]);
    if (response) {
      const paths = response.filePaths
        .sort((file) => (file.endsWith(".json") ? -1 : 1))
        .map((file) => ({
          name: file.split("\\")[file.split("\\").length - 1],
          data: `data:${
            file.endsWith(".json") ? `application/json` : `image/png`
          };base64,${fs.readFileSync(file.split("\\").join("/"), "base64")}`,
        }));
      armors[selectedArmor].model = [
        paths.find((path) => path.name.endsWith(".geo.json")),
      ];
      armors[selectedArmor].texture = paths
        .filter((path) => !path.name.endsWith(".json"))
        .sort((path) => (path.name.startsWith("item") ? -1 : 1));
      send_changes({ file: "armors.json", content: armors });
    }
  }
  function setModel(ev) {
    let i = 0;
    const reader = new FileReader();
    reader.onload = function (event) {
      if (i == 0) {
        armors[selectedArmor].model = [
          {
            name: files[i].name,
            data: event.target.result,
          },
        ];
        armors[selectedArmor].texture = [];
      } else if (i == 1 && files[i].name.endsWith(".json")) {
        armors[selectedArmor].model.push({
          name: files[i].name,
          data: event.target.result,
        });
      } else
        armors[selectedArmor].texture.push({
          name: files[i].name,
          data: event.target.result,
        });
    };
    reader.onloadend = function () {
      i++;
      if (!files[i])
        return send_changes({ file: "armors.json", content: armors });
      reader.readAsDataURL(files[i]);
    };
    const files = [...ev.dataTransfer.files].sort((file) =>
      file.name.endsWith(".json") ? -1 : 1
    );
    reader.readAsDataURL(files[0]);
  }
  async function chooseItemTexture() {
    const response = await ipc.invoke("dialog", ["openFile"], ["png"]);
    if (response) {
      const file = response.filePaths[0];
      armors[selectedArmor].itemTexture = {
        name: file.split("\\")[file.split("\\").length - 1],
        data: `data:${
          file.endsWith(".json") ? `application/json` : `image/png`
        };base64,${fs.readFileSync(file.split("\\").join("/"), "base64")}`,
      };
      send_changes({ file: "armors.json", content: armors });
    }
  }
  function setItemTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      armors[selectedArmor].itemTexture = {
        name: files[i].name,
        data: event.target.result,
      };
    };
    reader.onloadend = function () {
      return send_changes({ file: "armors.json", content: armors });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
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
      send_changes({ file: "armors.json", content: armors });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedArmor || !editor) return;
    editor.graph.configure(armors[selectedArmor].node_data.graph);
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
                <option value={material}>{convertToCamelCase(material)}</option>
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
                <option value={tab}>{convertToCamelCase(tab)}</option>
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
              src={armors[selectedArmor].itemTexture?.data ?? ""}
              on:error={fallbackTexture}
              on:click={chooseItemTexture}
              on:drop={setItemTexture}
              on:dragover|preventDefault
            />
          </div>
          {#if armors[selectedArmor].modelType == "blockbench"}
            <div class="col-start-1">
              <label class="text-lg">Textures & Model</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={armors[selectedArmor].texture[1]?.data ?? ""}
                on:error={fallbackTexture}
                on:click={chooseModel}
                on:drop={setModel}
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
