<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let trees = {};
  let tabs = {};
  let tiers = {};
  let sounds = {};
  let biomes = {};
  let blocks = {};
  let defaultBiomes = [];
  let defaultBlocks = [];
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let tiersPath = "";
  let soundsPath = "";
  let biomesPath = "";
  let blocksPath = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "trees.json");
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    tiersPath = pathModule.join(projectPath, "src", "data", "tiers.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
    biomesPath = pathModule.join(projectPath, "src", "data", "biomes.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
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
    trees = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    tiers = fs.existsSync(tiersPath) ? fs.readJSONSync(tiersPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    biomes = fs.existsSync(biomesPath) ? fs.readJSONSync(biomesPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    defaultBlocks = fs.readJSONSync(
      isDev ? "./static/data/blocks.json" : "./resources/app/data/blocks.json"
    );
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "tree" && !n.showInContext) || n.showInContext);
    defaultBiomes = fs.readJSONSync(
      isDev ? "./static/data/biomes.json" : "./resources/app/data/biomes.json"
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
          trees[selectedTree].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        trees[selectedTree].node_data.connected_nodes.push({
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
    Object.keys(trees).forEach((tree) => {
      trees[tree].name = tree;
      trees[tree].leavesBlock = trees[tree].leavesBlock.trim()
        ? trees[tree].leavesBlock
        : Object.keys(blocks)[0] ?? defaultBlocks[0] ?? trees[tree].leavesBlock;
      trees[tree].bodyBlock = trees[tree].bodyBlock.trim()
        ? trees[tree].bodyBlock
        : Object.keys(blocks)[0] ?? defaultBlocks[0] ?? trees[tree].bodyBlock;
      trees[tree].breakSound = trees[tree].breakSound.trim()
        ? trees[tree].breakSound
        : Object.keys(sounds)[0] ?? trees[tree].breakSound;
      trees[tree].walkSound = trees[tree].walkSound.trim()
        ? trees[tree].walkSound
        : Object.keys(sounds)[0] ?? trees[tree].walkSound;
      trees[tree].placeSound = trees[tree].placeSound.trim()
        ? trees[tree].placeSound
        : Object.keys(sounds)[0] ?? trees[tree].placeSound;
      trees[tree].hitSound = trees[tree].hitSound.trim()
        ? trees[tree].hitSound
        : Object.keys(sounds)[0] ?? trees[tree].hitSound;
      trees[tree].biomes = trees[tree].biomes.length
        ? trees[tree].biomes
        : [Object.keys(biomes)[0] ?? defaultBiomes[0]];
    });
    selectedTree = Object.keys(trees)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "trees.json") return;
      trees = data.file.content;
      updateEditor();
    };
    window.onchange = () => {
      send_changes({ file: "trees.json", data: trees });
    };
  });
  let selectedTree = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_tree_${Object.keys(trees).length + 1}`;
    trees[name] = {
      name,
      resistance: 0.1,
      explosion_resistance: 0.1,
      fire_resistance: 0.1,
      lightLevel: 1,
      friction: 40,
      jumpFactor: 1,
      speedFactor: 1,
      amount: 1,
      bodyHeight: 7,
      bodyRandA: 0,
      bodyRandB: 0,
      leavesHeight: 4,
      radius: 2,
      offset: 0,
      tab: "none",
      leavesBlock: Object.keys(blocks)[0] ?? defaultBlocks[0],
      bodyBlock: Object.keys(blocks)[0] ?? defaultBlocks[0],
      mapColor: "none",
      instrument: "none",
      breakSound: Object.keys(sounds)[0] ?? "",
      walkSound: Object.keys(sounds)[0] ?? "",
      placeSound: Object.keys(sounds)[0] ?? "",
      hitSound: Object.keys(sounds)[0] ?? "",
      biomes: [Object.keys(biomes)[0] ?? defaultBiomes[0]],
      pushReaction: "ignore",
      dropXp: false,
      minXp: 0,
      maxXp: 0,
      dropItem: false,
      minedBy: "anything",
      minedByTier: "wood",
      ignitedByLava: true,
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
    selectedTree = name;
    updateEditor();
    send_changes({ file: "trees.json", data: trees });
  }
  function save() {
    const obj = {};
    Object.keys(trees).forEach((tree) => {
      const name = trees[tree].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(trees[tree]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = trees[tree][property];
      });
    });
    fs.writeJSONSync(path, obj);
    trees = obj;
    Object.keys(trees).forEach((tree) => {
      trees[tree].name = tree;
    });
    selectedTree = trees[selectedTree] ? selectedTree : Object.keys(trees)[0];
    success("Trees saved successfully!");
  }
  function deleteTree() {
    if (!selectedTree) return;
    delete trees[selectedTree];
    trees = trees;
    selectedTree = Object.keys(trees)[0];
    updateEditor();
    send_changes({ file: "trees.json", data: trees });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture(property, filters) {
    const response = await ipc.invoke("dialog", "openFile", filters);
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const tree = fs.readFileSync(splitted.join("/"), "base64");
      trees[selectedTree][property] = {
        name: splitted[splitted.length - 1],
        data: tree,
      };
      send_changes({ file: "trees.json", data: trees });
    }
  }
  function setTexture(property, ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      trees[selectedTree][property] = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result
          .replace("data:image/png;base64,", "")
          .replace("data:application/json;base64,", ""),
      };
      send_changes({ file: "trees.json", data: trees });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  function addEffect() {
    trees[selectedTree].effects.push({
      name: "absorption",
      probability: 0,
      duration: 0,
      amplifier: 1,
      ambient: true,
      visible: true,
      showIcon: true,
    });
    trees[selectedTree].effects = trees[selectedTree].effects;
    send_changes({ file: "trees.json", data: trees });
  }
  function deleteEffect(i) {
    trees[selectedTree].effects.splice(i, 1);
    trees[selectedTree].effects = trees[selectedTree].effects;
    send_changes({ file: "trees.json", data: trees });
  }
  let editor;
  function setEditor() {
    if (!trees[selectedTree]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      trees[selectedTree].node_data.connected_nodes = [];
      trees[selectedTree].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "trees.json", data: trees });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedTree || !editor) return;
    editor.graph.configure(trees[selectedTree].node_data.graph);
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
  <title>OpenMod - Trees</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Tree:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedTree}
      on:change={updateEditor}
    >
      {#if !Object.keys(trees).length}
        <option disabled value={selectedTree}>No trees</option>
      {/if}
      {#each Object.keys(trees) as tree}
        <option value={tree}>{tree}</option>
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
        <button class="btn btn-error" on:click={deleteTree}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if trees[selectedTree]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={trees[selectedTree].name}
            />
          </div>
          <div>
            <label class="text-lg">Resistance</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={trees[selectedTree].resistance}
            />
          </div>
          <div>
            <label class="text-lg">Explosion Resistance</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={trees[selectedTree].explosion_resistance}
            />
          </div>
          <div>
            <label class="text-lg">Fire Resistance (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              class="input w-full"
              bind:value={trees[selectedTree].fire_resistance}
            />
          </div>
          <div>
            <label class="text-lg">Light Level</label>
            <input
              type="number"
              min="0"
              max="15"
              class="input w-full"
              bind:value={trees[selectedTree].lightLevel}
            />
          </div>
          <div>
            <label class="text-lg">Friction (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              class="input w-full"
              bind:value={trees[selectedTree].friction}
            />
          </div>
          <div>
            <label class="text-lg">Jump Power</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={trees[selectedTree].jumpFactor}
            />
          </div>
          <div>
            <label class="text-lg">Movement Speed</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={trees[selectedTree].speedFactor}
            />
          </div>
          <div>
            <label class="text-lg">Amount Per Biome</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].amount}
            />
          </div>
          <div>
            <label class="text-lg">Body Height</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].bodyHeight}
            />
          </div>
          <div>
            <label class="text-lg">Body Height Random A</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].bodyRandA}
            />
          </div>
          <div>
            <label class="text-lg">Body Height Random B</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].bodyRandB}
            />
          </div>
          <div>
            <label class="text-lg">Leaves Height</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].leavesHeight}
            />
          </div>
          <div>
            <label class="text-lg">Leaves Radius</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].radius}
            />
          </div>
          <div>
            <label class="text-lg">Leaves Offset</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={trees[selectedTree].offset}
            />
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].tab}
            >
              <option value="none">None</option>
              {#each Object.keys(tabs) as tab}
                <option value={tab}>{convertToCamelCase(tab)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Leaves Block</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].leavesBlock}
            >
              {#each Object.keys(blocks) as block}
                <option value={block}>{convertToCamelCase(block)}</option>
              {/each}
              {#each defaultBlocks as block}
                <option value={block}>{block}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Body Block</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].bodyBlock}
            >
              {#each Object.keys(blocks) as block}
                <option value={block}>{convertToCamelCase(block)}</option>
              {/each}
              {#each defaultBlocks as block}
                <option value={block}>{block}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Map Color</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].mapColor}
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
              <option value="warped_wart_tree">Warped Wart Tree</option>
              <option value="deepslate">Deepslate</option>
              <option value="raw_iron">Raw Iron</option>
              <option value="glow_lichen">Glow Lichen</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Play Instrument</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].instrument}
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
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].breakSound}
            >
              {#if !Object.keys(sounds).length}
                <option disabled value={trees[selectedTree].breakSound}
                  >No sounds</option
                >
              {/if}
              {#each Object.keys(sounds) as sound}
                <option value={sound}>{convertToCamelCase(sound)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Walk Sound</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].walkSound}
            >
              {#if !Object.keys(sounds).length}
                <option disabled value={trees[selectedTree].walkSound}
                  >No sounds</option
                >
              {/if}
              {#each Object.keys(sounds) as sound}
                <option value={sound}>{convertToCamelCase(sound)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Place Sound</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].placeSound}
            >
              {#if !Object.keys(sounds).length}
                <option disabled value={trees[selectedTree].placeSound}
                  >No sounds</option
                >
              {/if}
              {#each Object.keys(sounds) as sound}
                <option value={sound}>{convertToCamelCase(sound)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Hit Sound</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].hitSound}
            >
              {#if !Object.keys(sounds).length}
                <option disabled value={trees[selectedTree].hitSound}
                  >No sounds</option
                >
              {/if}
              {#each Object.keys(sounds) as sound}
                <option value={sound}>{convertToCamelCase(sound)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">When Pushed By Piston</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].pushReaction}
              ><option value="ignore">Do Nothing</option><option value="tree"
                >Tree</option
              ><option value="push_only">Push Only</option><option
                value="destroy">Destroy</option
              ><option value="normal">Normal</option></select
            >
          </div>
          <div>
            <label class="text-lg">Can Drop Experience?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].dropXp}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if trees[selectedTree].dropXp}
            <div>
              <label class="text-lg">Minimum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={trees[selectedTree].minXp}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={trees[selectedTree].maxXp}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Can Drop Item?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].dropItem}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if trees[selectedTree].dropItem}
            <div>
              <label class="text-lg">Drops Item If Mined With</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={trees[selectedTree].minedBy}
                ><option value="anything">Anything</option>
                <option value="pickaxe">Pickaxe</option>
                <option value="axe">Axe</option>
                <option value="shovel">Shovel</option>
                <option value="hoe">Hoe</option>
              </select>
            </div>
            {#if trees[selectedTree].minedBy != "anything"}
              <div>
                <label class="text-lg">Required Tool Tier</label>
                <select
                  class="select font-normal text-base w-full"
                  bind:value={trees[selectedTree].minedByTier}
                >
                  <option value="wood">Wood</option>
                  <option value="stone">Stone</option>
                  <option value="iron">Iron</option>
                  <option value="gold">Gold</option>
                  <option value="diamond">Diamond</option>
                  <option value="netherite">Netherite</option>
                  {#each Object.keys(tiers) as tier}
                    <option value={tier}>{convertToCamelCase(tier)}</option>
                  {/each}
                </select>
              </div>
            {/if}
          {/if}
          <div>
            <label class="text-lg">Can Be Ignited By Lava?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].ignitedByLava}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
          </div>
          <div class="col-span-3">
            <label class="text-lg"
              >Biomes (hold <a class="text-warning">ctrl</a> to select multiple)</label
            >
            <select
              multiple
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].biomes}
            >
              {#each Object.keys(biomes) as biome}
                <option value={biome}>{convertToCamelCase(biome)}</option>
              {/each}
              {#each defaultBiomes as biome}
                <option value={biome.name}>{biome.name}</option>
              {/each}</select
            >
          </div>
          {#if trees[selectedTree].modelType == "blockbench"}
            <div class="col-start-1">
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${trees[selectedTree].texture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture", "png")}
                on:drop={setTexture.bind(this, "texture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Model</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{trees[selectedTree].model
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "model", "json")}
                on:drop={setTexture.bind(this, "model")}
                on:dragover|preventDefault
              >
                {trees[selectedTree].model?.name ?? ""}
              </div>
            </div>
            <div>
              <label class="text-lg">Geo</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{trees[selectedTree].geo
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "geo", "json")}
                on:drop={setTexture.bind(this, "geo")}
                on:dragover|preventDefault
              >
                {trees[selectedTree].geo?.name ?? ""}
              </div>
            </div>
            <div>
              <label class="text-lg">Animations</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{trees[selectedTree].animation
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "animation", "json")}
                on:drop={setTexture.bind(this, "animation")}
                on:dragover|preventDefault
              >
                {trees[selectedTree].animation?.name ?? ""}
              </div>
            </div>
          {/if}
          {#if trees[selectedTree].modelType == "default"}
            <div>
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${trees[selectedTree].crossTexture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "crossTexture", "png")}
                on:drop={setTexture.bind(this, "crossTexture")}
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
              bind:value={trees[selectedTree].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Rarity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].rarity}
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
              bind:value={trees[selectedTree].fuel}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if trees[selectedTree].fuel}
            <div>
              <label class="text-lg">Burn Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={trees[selectedTree].burnTime}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Food?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].food}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={trees[selectedTree].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
        </div>
      </Accordion>
      {#if trees[selectedTree].food}
        <Accordion title="Food">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Is Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={trees[selectedTree].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Can Be Eaten Fast?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={trees[selectedTree].food_fast}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Is Meat?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={trees[selectedTree].food_meat}
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
                bind:value={trees[selectedTree].food_nutrition}
              />
            </div>
            <div>
              <label class="text-lg">Saturation (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={trees[selectedTree].food_saturationMod}
              />
            </div>
          </div>
        </Accordion>
        <Accordion title="Effects">
          <div class="flex flex-col gap-3">
            {#each trees[selectedTree].effects as effect, index}
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
