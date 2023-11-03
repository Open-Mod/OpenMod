<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let blocks = {};
  let tabs = {};
  let tiers = {};
  let sounds = {};
  let biomes = {};
  let defaultSounds = [];
  let defaultBiomes = [];
  let projectPath = "";
  let path = "";
  let projectName = "";
  let tabsPath = "";
  let tiersPath = "";
  let soundsPath = "";
  let biomesPath = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "blocks.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    tiersPath = pathModule.join(projectPath, "src", "data", "tiers.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
    biomesPath = pathModule.join(projectPath, "src", "data", "biomes.json");
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
    blocks = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    tiers = fs.existsSync(tiersPath) ? fs.readJSONSync(tiersPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    biomes = fs.existsSync(biomesPath) ? fs.readJSONSync(biomesPath) : {};
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "block" && !n.showInContext) || n.showInContext);
    defaultBiomes = fs.readJSONSync(
      isDev ? "./static/data/biomes.json" : "./resources/app/data/biomes.json"
    );
    defaultSounds = fs.readJSONSync(
      isDev ? "./static/data/sounds.json" : "./resources/app/data/sounds.json"
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
          blocks[selectedBlock].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        blocks[selectedBlock].node_data.connected_nodes.push({
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
    Object.keys(blocks).forEach((block) => {
      blocks[block].name = block;
      blocks[block].breakSound = blocks[block].breakSound.trim()
        ? blocks[block].breakSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].walkSound = blocks[block].walkSound.trim()
        ? blocks[block].walkSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].placeSound = blocks[block].placeSound.trim()
        ? blocks[block].placeSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].hitSound = blocks[block].hitSound.trim()
        ? blocks[block].hitSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].openSound = blocks[block].openSound.trim()
        ? blocks[block].openSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].closeSound = blocks[block].closeSound.trim()
        ? blocks[block].closeSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      blocks[block].biomes = blocks[block].biomes.length
        ? blocks[block].biomes
        : [Object.keys(biomes)[0] ?? defaultBiomes[0]];
    });
    selectedBlock = Object.keys(blocks)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "blocks.json") return;
        blocks = data.file.data;
        updateEditor();
      } else if (data.type == "SELECTED") selectedBlock = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "blocks.json", data: blocks });
    };
  });
  let selectedBlock = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_block_${Object.keys(blocks).length + 1}`;
    blocks[name] = {
      name,
      resistance: 0.1,
      explosion_resistance: 0.1,
      fire_resistance: 0.1,
      lightLevel: 1,
      friction: 40,
      jumpFactor: 1,
      speedFactor: 1,
      type: "normal",
      tab: "none",
      mapColor: "none",
      instrument: "none",
      breakSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      walkSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      placeSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      hitSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      openSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      closeSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      biomes: [Object.keys(biomes)[0] ?? defaultBiomes[0]],
      pushReaction: "ignore",
      openedByHand: false,
      dropXp: false,
      minXp: 0,
      maxXp: 0,
      dropItem: false,
      minedBy: "anything",
      minedByTier: "wood",
      ignitedByLava: true,
      isCollidable: true,
      isOre: false,
      oreSize: 1,
      minChunkSize: 1,
      maxChunkSize: 1,
      minHeight: 1,
      maxHeight: 1,
      discardChance: 0,
      genShape: "minecraft:trapezoid",
      modelType: "default",
      stacksTo: 1,
      rarity: "common",
      fuel: false,
      food: false,
      fireResistant: false,
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
    selectedBlock = name;
    updateEditor();
    send_changes({ file: "blocks.json", data: blocks });
  }
  function save() {
    const obj = {};
    Object.keys(blocks).forEach((block) => {
      const name = blocks[block].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(blocks[block]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = blocks[block][property];
      });
    });
    fs.writeJSONSync(path, obj);
    blocks = obj;
    Object.keys(blocks).forEach((block) => {
      blocks[block].name = block;
    });
    selectedBlock = blocks[selectedBlock]
      ? selectedBlock
      : Object.keys(blocks)[0];
    success("Blocks saved successfully!");
  }
  function deleteBlock() {
    if (!selectedBlock) return;
    delete blocks[selectedBlock];
    blocks = blocks;
    selectedBlock = Object.keys(blocks)[0];
    updateEditor();
    send_changes({ file: "blocks.json", data: blocks });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture(property, filters) {
    const response = await ipc.invoke("dialog", "openFile", filters);
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const block = fs.readFileSync(splitted.join("/"), "base64");
      blocks[selectedBlock][property] = {
        name: splitted[splitted.length - 1],
        data: block,
      };
      send_changes({ file: "blocks.json", data: blocks });
    }
  }
  function setTexture(property, ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock][property] = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result
          .replace("data:image/png;base64,", "")
          .replace("data:application/json;base64,", ""),
      };
      send_changes({ file: "blocks.json", data: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  function addEffect() {
    blocks[selectedBlock].effects.push({
      name: "absorption",
      probability: 0,
      duration: 0,
      amplifier: 1,
      ambient: true,
      visible: true,
      showIcon: true,
    });
    blocks[selectedBlock].effects = blocks[selectedBlock].effects;
    send_changes({ file: "blocks.json", data: blocks });
  }
  function deleteEffect(i) {
    blocks[selectedBlock].effects.splice(i, 1);
    blocks[selectedBlock].effects = blocks[selectedBlock].effects;
    send_changes({ file: "blocks.json", data: blocks });
  }
  let editor;
  function setEditor() {
    if (!blocks[selectedBlock]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      blocks[selectedBlock].node_data.connected_nodes = [];
      blocks[selectedBlock].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "blocks.json", data: blocks });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedBlock || !editor) return;
    editor.graph.configure(blocks[selectedBlock].node_data.graph);
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
  <title>OpenMod - Blocks</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Block:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedBlock}
      on:change={updateEditor}
    >
      {#if !Object.keys(blocks).length}
        <option disabled value={selectedBlock}>No blocks</option>
      {/if}
      {#each Object.keys(blocks) as block}
        <option value={block}>{block}</option>
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
        <button class="btn btn-error" on:click={deleteBlock}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if blocks[selectedBlock]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={blocks[selectedBlock].name}
            />
          </div>
          <div>
            <label class="text-lg">Resistance</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={blocks[selectedBlock].resistance}
            />
          </div>
          <div>
            <label class="text-lg">Explosion Resistance</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={blocks[selectedBlock].explosion_resistance}
            />
          </div>
          <div>
            <label class="text-lg">Fire Resistance (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              class="input w-full"
              bind:value={blocks[selectedBlock].fire_resistance}
            />
          </div>
          <div>
            <label class="text-lg">Light Level</label>
            <input
              type="number"
              min="0"
              max="15"
              class="input w-full"
              bind:value={blocks[selectedBlock].lightLevel}
            />
          </div>
          <div>
            <label class="text-lg">Friction (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              class="input w-full"
              bind:value={blocks[selectedBlock].friction}
            />
          </div>
          <div>
            <label class="text-lg">Jump Power</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={blocks[selectedBlock].jumpFactor}
            />
          </div>
          <div>
            <label class="text-lg">Movement Speed</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={blocks[selectedBlock].speedFactor}
            />
          </div>
          <div>
            <label class="text-lg">Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].type}
              ><option value="normal">Normal</option>
              <option value="stairs">Stairs</option>
              <option value="slab">Slab</option>
              <option value="door">Door</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].tab}
            >
              <option value="none">None</option>
              {#each Object.keys(tabs) as tab}
                <option value={tab}>{projectName.toLowerCase()}:{tab}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Map Color</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].mapColor}
            >
              <option value="none">None</option>
              <option value="grass">Grass</option>
              <option value="sand">Sand</option>
              <option value="wool">Wool</option>
              <option value="fire">Fire</option>
              <option value="ice">Ice</option>
              <option value="metal">Metal</option>
              <option value="plant">Plant</option>
              <option value="snow">Snow</option>
              <option value="clay">Clay</option>
              <option value="dirt">Dirt</option>
              <option value="stone">Stone</option>
              <option value="water">Water</option>
              <option value="wood">Wood</option>
              <option value="quartz">Quartz</option>
              <option value="color_orange">Orange</option>
              <option value="color_magenta">Magenta</option>
              <option value="color_light_blue">Light Blue</option>
              <option value="color_yellow">Yellow</option>
              <option value="color_light_green">Light Green</option>
              <option value="color_pink">Pink</option>
              <option value="color_gray">Gray</option>
              <option value="color_light_gray">Light Gray</option>
              <option value="color_cyan">Cyan</option>
              <option value="color_purple">Purple</option>
              <option value="color_blue">Blue</option>
              <option value="color_brown">Brown</option>
              <option value="color_green">Green</option>
              <option value="color_red">Red</option>
              <option value="color_black">Black</option>
              <option value="gold">Gold</option>
              <option value="diamond">Diamond</option>
              <option value="lapis">Lapis</option>
              <option value="emerald">Emerald</option>
              <option value="podzol">Podzol</option>
              <option value="nether">Nether</option>
              <option value="terracotta_white">Terracotta White</option>
              <option value="terracotta_orange">Terracotta Orange</option>
              <option value="terracotta_magenta">Terracotta Magenta</option>
              <option value="terracotta_light_blue"
                >Terracotta Light Blue</option
              > <option value="terracotta_yellow">Terracotta Yellow</option>
              <option value="terracotta_light_green"
                >Terracotta Light Green</option
              > <option value="terracotta_pink">Terracotta Pink</option>
              <option value="terracotta_gray">Terracotta Gray</option>
              <option value="terracotta_light_gray"
                >Terracotta Light Gray</option
              > <option value="terracotta_cyan">Terracotta Cyan</option>
              <option value="terracotta_purple">Terracotta Purple</option>
              <option value="terracotta_blue">Terracotta Blue</option>
              <option value="terracotta_brown">Terracotta Brown</option>
              <option value="terracotta_green">Terracotta Green</option>
              <option value="terracotta_red">Terracotta Red</option>
              <option value="terracotta_black">Terracotta Black</option>
              <option value="crimson_nylium">Crimson Nylium</option>
              <option value="crimson_stem">Crimson Stem</option>
              <option value="crimson_hyphae">Crimson Hyphae</option>
              <option value="warped_nylium">Warped Nylium</option>
              <option value="warped_stem">Warped Stem</option>
              <option value="warped_hyphae">Warped Hyphae</option>
              <option value="warped_wart_block">Warped Wart Block</option>
              <option value="deepslate">Deepslate</option>
              <option value="raw_iron">Raw Iron</option>
              <option value="glow_lichen">Glow Lichen</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Play Instrument</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].instrument}
            >
              <option value="none">None</option>
              <option value="harp">Harp</option>
              <option value="basedrum">Basedrum</option>
              <option value="snare">Snare</option>
              <option value="hat">Hat</option>
              <option value="bass">Bass</option>
              <option value="flute">Flute</option>
              <option value="bell">Bell</option>
              <option value="guitar">Guitar</option>
              <option value="chime">Chime</option>
              <option value="xylophone">Xylophone</option>
              <option value="iron_xylophone">Iron Xylophone</option>
              <option value="cow_bell">Cow Bell</option>
              <option value="didgeridoo">Didgeridoo</option>
              <option value="bit">Bit</option>
              <option value="banjo">Banjo</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Break Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={blocks[selectedBlock].breakSound}
            />
          </div>
          <div>
            <label class="text-lg">Walk Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={blocks[selectedBlock].walkSound}
            />
          </div>
          <div>
            <label class="text-lg">Place Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={blocks[selectedBlock].placeSound}
            />
          </div>
          <div>
            <label class="text-lg">Hit Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={blocks[selectedBlock].hitSound}
            />
          </div>
          {#if blocks[selectedBlock].type == "door"}
            <div>
              <label class="text-lg">Open Sound</label>
              <input
                list="soundList"
                class="input w-full"
                bind:value={blocks[selectedBlock].openSound}
              />
            </div>
            <div>
              <label class="text-lg">Close Sound</label>
              <input
                list="soundList"
                class="input w-full"
                bind:value={blocks[selectedBlock].closeSound}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">When Pushed By Piston</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].pushReaction}
              ><option value="ignore">Do Nothing</option><option value="block"
                >Block</option
              ><option value="push_only">Push Only</option><option
                value="destroy">Destroy</option
              ><option value="normal">Normal</option></select
            >
          </div>
          {#if blocks[selectedBlock].type == "door"}
            <div>
              <label class="text-lg">Can Be Opened By Hand?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].openedByHand}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
          {/if}
          <div>
            <label class="text-lg">Can Drop Experience?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].dropXp}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if blocks[selectedBlock].dropXp}
            <div>
              <label class="text-lg">Minimum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={blocks[selectedBlock].minXp}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={blocks[selectedBlock].maxXp}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Can Drop Item?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].dropItem}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if blocks[selectedBlock].dropItem}
            <div>
              <label class="text-lg">Drops Item If Mined With</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].minedBy}
                ><option value="anything">Anything</option>
                <option value="pickaxe">Pickaxe</option>
                <option value="axe">Axe</option>
                <option value="shovel">Shovel</option>
                <option value="hoe">Hoe</option>
              </select>
            </div>
            {#if blocks[selectedBlock].minedBy != "anything"}
              <div>
                <label class="text-lg">Required Tool Tier</label>
                <select
                  class="select font-normal text-base w-full"
                  bind:value={blocks[selectedBlock].minedByTier}
                >
                  <option value="wood">Wood</option>
                  <option value="stone">Stone</option>
                  <option value="iron">Iron</option>
                  <option value="gold">Gold</option>
                  <option value="diamond">Diamond</option>
                  <option value="netherite">Netherite</option>
                  {#each Object.keys(tiers) as tier}
                    <option value={tier}
                      >{projectName.toLowerCase()}:{tier}</option
                    >
                  {/each}
                </select>
              </div>
            {/if}
          {/if}
          <div>
            <label class="text-lg">Can Be Ignited By Lava?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].ignitedByLava}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Collidable?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].isCollidable}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Can Be Generated As Ore?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].isOre}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
          </div>
          {#if blocks[selectedBlock].modelType == "blockbench"}
            <div class="col-start-1">
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].texture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture", "png")}
                on:drop={setTexture.bind(this, "texture")}
                on:dragover|preventDefault
              />
            </div>
            {#if blocks[selectedBlock].type == "normal"}
              <div>
                <label class="text-lg">Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].model
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "model", "json")}
                  on:drop={setTexture.bind(this, "model")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].model?.name ?? ""}
                </div>
              </div>
            {:else if blocks[selectedBlock].type == "slab"}
              <div>
                <label class="text-lg">Bottom Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].model
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "model", "json")}
                  on:drop={setTexture.bind(this, "model")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].model?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Top Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].topModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "topModel", "json")}
                  on:drop={setTexture.bind(this, "topModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].topModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Full Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].fullModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "fullModel", "json")}
                  on:drop={setTexture.bind(this, "fullModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].fullModel?.name ?? ""}
                </div>
              </div>
            {:else if blocks[selectedBlock].type == "stairs"}
              <div>
                <label class="text-lg">Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].model
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "model", "json")}
                  on:drop={setTexture.bind(this, "model")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].model?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Inner Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].innerModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "innerModel", "json")}
                  on:drop={setTexture.bind(this, "innerModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].innerModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Outer Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].outerModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "outerModel", "json")}
                  on:drop={setTexture.bind(this, "outerModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].outerModel?.name ?? ""}
                </div>
              </div>
            {:else if blocks[selectedBlock].type == "door"}
              <div>
                <label class="text-lg">Bottom Left Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].model
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "model", "json")}
                  on:drop={setTexture.bind(this, "model")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].model?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Bottom Left Open Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].bottomLeftOpenModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(
                    this,
                    "bottomLeftOpenModel",
                    "json"
                  )}
                  on:drop={setTexture.bind(this, "bottomLeftOpenModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].innerModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Bottom Right Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].bottomRightModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(
                    this,
                    "bottomRightModel",
                    "json"
                  )}
                  on:drop={setTexture.bind(this, "bottomRightModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].bottomRightModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Bottom Right Open Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].bottomRightOpenModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(
                    this,
                    "bottomRightOpenModel",
                    "json"
                  )}
                  on:drop={setTexture.bind(this, "bottomRightOpenModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].bottomRightOpenModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Top Left Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].topLeftModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "topLeftModel", "json")}
                  on:drop={setTexture.bind(this, "topLeftModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].topLeftModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Top Left Open Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].topLeftOpenModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(
                    this,
                    "topLeftOpenModel",
                    "json"
                  )}
                  on:drop={setTexture.bind(this, "topLeftOpenModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].topLeftOpenModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Top Right Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].topRightModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(this, "topRightModel", "json")}
                  on:drop={setTexture.bind(this, "topRightModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].topRightModel?.name ?? ""}
                </div>
              </div>
              <div>
                <label class="text-lg">Top Right Open Model</label>
                <div
                  class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                  style="{blocks[selectedBlock].topRightOpenModel
                    ? 'background-color: rgba(0,0,0,0.3)'
                    : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                  on:click={chooseTexture.bind(
                    this,
                    "topRightOpenModel",
                    "json"
                  )}
                  on:drop={setTexture.bind(this, "topRightOpenModel")}
                  on:dragover|preventDefault
                >
                  {blocks[selectedBlock].topRightOpenModel?.name ?? ""}
                </div>
              </div>
            {/if}
            <div>
              <label class="text-lg">Geo</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{blocks[selectedBlock].geo
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "geo", "json")}
                on:drop={setTexture.bind(this, "geo")}
                on:dragover|preventDefault
              >
                {blocks[selectedBlock].geo?.name ?? ""}
              </div>
            </div>
            <div>
              <label class="text-lg">Animations</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{blocks[selectedBlock].animation
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "animation", "json")}
                on:drop={setTexture.bind(this, "animation")}
                on:dragover|preventDefault
              >
                {blocks[selectedBlock].animation?.name ?? ""}
              </div>
            </div>
          {/if}
          {#if blocks[selectedBlock].modelType == "default" && blocks[selectedBlock].type == "normal"}
            <div class="col-start-1">
              <label class="text-lg">Particle Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].particleTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "particleTexture", "png")}
                on:drop={setTexture.bind(this, "particleTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Up Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].upTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "upTexture", "png")}
                on:drop={setTexture.bind(this, "upTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Down Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].downTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "downTexture", "png")}
                on:drop={setTexture.bind(this, "downTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Front Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].frontTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "frontTexture", "png")}
                on:drop={setTexture.bind(this, "frontTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Back Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].backTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "backTexture", "png")}
                on:drop={setTexture.bind(this, "backTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Right Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].rightTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "rightTexture", "png")}
                on:drop={setTexture.bind(this, "rightTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Left Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].leftTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "leftTexture", "png")}
                on:drop={setTexture.bind(this, "leftTexture")}
                on:dragover|preventDefault
              />
            </div>
          {:else if blocks[selectedBlock].modelType == "default" && (blocks[selectedBlock].type == "stairs" || blocks[selectedBlock].type == "slab")}
            <div class="col-start-1">
              <label class="text-lg">Top Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].upTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "upTexture", "png")}
                on:drop={setTexture.bind(this, "upTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Bottom Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].downTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "downTexture", "png")}
                on:drop={setTexture.bind(this, "downTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Side Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].rightTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "rightTexture", "png")}
                on:drop={setTexture.bind(this, "rightTexture")}
                on:dragover|preventDefault
              />
            </div>
          {:else if blocks[selectedBlock].modelType == "default" && blocks[selectedBlock].type == "door"}
            <div class="col-start-1">
              <label class="text-lg">Top Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].upTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "upTexture", "png")}
                on:drop={setTexture.bind(this, "upTexture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Bottom Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${blocks[selectedBlock].downTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "downTexture", "png")}
                on:drop={setTexture.bind(this, "downTexture")}
                on:dragover|preventDefault
              />
            </div>
          {/if}
        </div></Accordion
      >
      <Accordion title="Item">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Stack Size</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={blocks[selectedBlock].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Rarity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].rarity}
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
              bind:value={blocks[selectedBlock].fuel}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if blocks[selectedBlock].fuel}
            <div>
              <label class="text-lg">Burn Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={blocks[selectedBlock].burnTime}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Food?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].food}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
        </div>
      </Accordion>
      {#if blocks[selectedBlock].food}
        <Accordion title="Food">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Is Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Can Be Eaten Fast?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].food_fast}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Is Meat?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].food_meat}
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
                bind:value={blocks[selectedBlock].food_nutrition}
              />
            </div>
            <div>
              <label class="text-lg">Saturation (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={blocks[selectedBlock].food_saturationMod}
              />
            </div>
          </div>
        </Accordion>
        <Accordion title="Effects">
          <div class="flex flex-col gap-3">
            {#each blocks[selectedBlock].effects as effect, index}
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
      {#if blocks[selectedBlock].isOre}
        <Accordion title="Ore">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Vein Size</label>
              <input
                type="number"
                min="1"
                max="64"
                class="input w-full"
                bind:value={blocks[selectedBlock].oreSize}
              />
            </div>
            <div>
              <label class="text-lg">Minimum Amount Per Chunk</label>
              <input
                type="number"
                min="1"
                max="256"
                class="input w-full"
                bind:value={blocks[selectedBlock].minChunkSize}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Amount Per Chunk</label>
              <input
                type="number"
                min="1"
                max="256"
                class="input w-full"
                bind:value={blocks[selectedBlock].maxChunkSize}
              />
            </div>
            <div>
              <label class="text-lg">Minimum Height</label>
              <input
                type="number"
                min="-2048"
                max="2047"
                class="input w-full"
                bind:value={blocks[selectedBlock].minHeight}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Height</label>
              <input
                type="number"
                min="-2048"
                max="2047"
                class="input w-full"
                bind:value={blocks[selectedBlock].maxHeight}
              />
            </div>
            <div>
              <label class="text-lg">Discard On Air Exposure (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={blocks[selectedBlock].discardChance}
              />
            </div>
            <div>
              <label class="text-lg">Generation Shape</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].genShape}
                ><option value="minecraft:uniform">Uniform</option><option
                  value="minecraft:trapezoid">Trapezoid</option
                ></select
              >
            </div>
            <div class="col-span-3">
              <label class="text-lg"
                >Biomes (hold <a class="text-warning">ctrl</a> to select multiple)</label
              >
              <select
                multiple
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].biomes}
              >
                {#each Object.keys(biomes) as biome}
                  <option value={biome}
                    >{projectName.toLowerCase()}:{biome}</option
                  >
                {/each}
                {#each defaultBiomes as biome}
                  <option value={biome.name}>{biome.name}</option>
                {/each}</select
              >
            </div>
          </div>
        </Accordion>
      {/if}
      <Accordion title="Events" style="overflow:hidden;" mount={setEditor}>
        <div id="editor" class="w-full h-[40vh]" />
      </Accordion>
    {/if}
  </div>
</div>
<datalist id="soundList">
  {#each Object.keys(sounds) as sound}
    <option value="{projectName.toLowerCase()}:{sound}" />
  {/each}
  {#each defaultSounds as sound}
    <option value={sound} />
  {/each}
</datalist>
