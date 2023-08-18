<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let items = {};
  let tabs = {};
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let nodesPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "items.json");
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
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    items = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "item" && !n.showInContext) || n.showInContext);
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
          items[selectedItem].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        items[selectedItem].node_data.connected_nodes.push({
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
    Object.keys(items).forEach((item) => {
      items[item].name = item;
    });
    selectedItem = Object.keys(items)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "items.json") return;
      items = data.file.content;
    };
  });
  let selectedItem = "";
  let name = "";
  let error = "";
  let success = "";
  let nodes = [];
  function add() {
    name = `new_item_${Object.keys(items).length + 1}`;
    items[name] = {
      name,
      stacksTo: 1,
      tab: "none",
      rarity: "common",
      fuel: false,
      food: false,
      fireResistant: false,
      modelType: "default",
      model: "",
      texture: [""],
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
    selectedItem = name;
    updateEditor();
    send_changes({ file: "items.json", content: items });
  }
  function save() {
    const obj = {};
    const itemTextures = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "item"
    );
    const itemModels = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "item"
    );
    const old = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    Object.keys(old).forEach((item) => {
      if (old[item].modelType == "default") {
        old[item].texture.forEach((texture) => {
          const textureType = texture.match(/[^:/]\w+(?=;|,)/)[0];
          fs.rmSync(pathModule.join(itemTextures, `${item}.${textureType}`));
        });
      } else if (old[item].modelType == "blockbench") {
        old[item].texture.forEach((texture) => {
          fs.rmSync(pathModule.join(itemTextures, `${texture.name}`));
        });
      }
    });
    Object.keys(items).forEach((item) => {
      const oldModel = pathModule.join(itemModels, `${item}.json`);
      if (fs.existsSync(oldModel)) fs.rmSync(oldModel);
      const name = items[item].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const modelPath = pathModule.join(itemModels, `${name}.json`);
      Object.keys(items[item]).forEach((property) => {
        if (property == "name") return;
        if (property == "texture" && items[item].modelType == "default") {
          const texture = items[item][property][0];
          if (texture) {
            const textureType = texture.match(/[^:/]\w+(?=;|,)/)[0];
            const texturePath = pathModule.join(
              itemTextures,
              `${name}.${textureType}`
            );
            const textureData = texture.match(
              /^data:([A-Za-z-+\/]+);base64,(.+)$/
            )[2];
            fs.writeFileSync(texturePath, textureData, "base64");
            fs.writeJSONSync(modelPath, {
              parent: "minecraft:item/generated",
              textures: {
                layer0: `${projectName.toLowerCase()}:item/${name}`,
              },
            });
          }
        } else if (
          property == "texture" &&
          items[item].modelType == "blockbench"
        ) {
          const model = items[item].model;
          const modelData = model.data.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          const textures = items[item][property];
          textures.forEach((texture) => {
            const texturePath = pathModule.join(
              itemTextures,
              `${texture.name}`
            );
            const textureData = texture.data.match(
              /^data:([A-Za-z-+\/]+);base64,(.+)$/
            )[2];
            fs.writeFileSync(texturePath, textureData, "base64");
          });
          fs.writeFileSync(modelPath, modelData, "base64");
        }
        obj[name][property] = items[item][property];
      });
    });
    fs.writeJSONSync(path, obj);
    items = obj;
    Object.keys(items).forEach((item) => {
      items[item].name = item;
    });
    selectedItem = Object.keys(items)[0];
    success = "Items saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteItem() {
    if (!selectedItem) return;
    delete items[selectedItem];
    items = items;
    selectedItem = Object.keys(items)[0];
    updateEditor();
    send_changes({ file: "items.json", content: items });
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
      items[selectedItem].model = model;
      items[selectedItem].texture = paths;
      send_changes({ file: "items.json", content: items });
    }
  }
  function setModel(ev) {
    let i = 0;
    const reader = new FileReader();
    reader.onload = function (event) {
      if (i == 0) {
        items[selectedItem].model = {
          name: files[i].name,
          data: event.target.result,
        };
        items[selectedItem].texture = [];
      } else
        items[selectedItem].texture.push({
          name: files[i].name,
          data: event.target.result,
        });
    };
    reader.onloadend = function () {
      i++;
      if (!files[i])
        return send_changes({ file: "items.json", content: items });
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
      items[selectedItem].texture = [`data:image/png;base64,${texture}`];
      send_changes({ file: "items.json", content: items });
    }
  }
  function setTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      items[selectedItem].texture = [event.target.result];
      send_changes({ file: "items.json", content: items });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  function addEffect() {
    items[selectedItem].effects.push({
      name: "absorption",
      probability: 0,
      duration: 0,
      amplifier: 1,
      ambient: true,
      visible: true,
      showIcon: true,
    });
    items[selectedItem].effects = items[selectedItem].effects;
    send_changes({ file: "items.json", content: items });
  }
  function deleteEffect(i) {
    items[selectedItem].effects.splice(i, 1);
    items[selectedItem].effects = items[selectedItem].effects;
    send_changes({ file: "items.json", content: items });
  }
  let editor;
  function setEditor() {
    if (!items[selectedItem]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      items[selectedItem].node_data.connected_nodes = [];
      items[selectedItem].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "items.json", content: items });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedItem || !editor) return;
    editor.graph.configure(items[selectedItem].node_data.graph);
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
  <title>OpenMod - Items</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Item:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedItem}
      on:change={updateEditor}
    >
      {#if !Object.keys(items).length}
        <option disabled value={selectedItem}>No items</option>
      {/if}
      {#each Object.keys(items) as item}
        <option value={item}>{item}</option>
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
        <button class="btn btn-error" on:click={deleteItem}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if items[selectedItem]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={items[selectedItem].name}
            />
          </div>
          <div>
            <label class="text-lg">Stack Size</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={items[selectedItem].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={items[selectedItem].tab}
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
              bind:value={items[selectedItem].rarity}
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
              bind:value={items[selectedItem].fuel}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if items[selectedItem].fuel}
            <div>
              <label class="text-lg">Burn Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={items[selectedItem].burnTime}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Food?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={items[selectedItem].food}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={items[selectedItem].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={items[selectedItem].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
          </div>
          {#if items[selectedItem].modelType == "blockbench"}
            <div class="col-start-1">
              <label class="text-lg">Textures & Model</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={items[selectedItem].texture[0]?.data ?? ""}
                on:error={fallbackTexture}
                on:click={chooseModel}
                on:drop={setModel}
                on:dragover|preventDefault
              />
            </div>
          {/if}
          {#if items[selectedItem].modelType == "default"}
            <div class="col-start-1">
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={items[selectedItem].texture[0]}
                on:error={fallbackTexture}
                on:click={chooseTexture}
                on:drop={setTexture}
                on:dragover|preventDefault
              />
            </div>
          {/if}
        </div>
      </Accordion>
      {#if items[selectedItem].food}
        <Accordion title="Food">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={items[selectedItem].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Eat Fast?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={items[selectedItem].food_fast}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Is Meat?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={items[selectedItem].food_meat}
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
                bind:value={items[selectedItem].food_nutrition}
              />
            </div>
            <div>
              <label class="text-lg">Saturation (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={items[selectedItem].food_saturationMod}
              />
            </div>
          </div>
        </Accordion>
        <Accordion title="Effects">
          <div class="flex flex-col gap-3">
            {#each items[selectedItem].effects as effect, index}
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
                  <label class="text-lg">Show Icon?</label>
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
<Error {error} />
<Success {success} />
