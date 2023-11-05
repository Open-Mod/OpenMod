<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let mobs = {};
  let tabs = {};
  let sounds = {};
  let items = {};
  let tools = {};
  let armors = {};
  let blocks = {};
  let trees = {};
  let defaultItems = [];
  let defaultSounds = [];
  let projectPath = "";
  let path = "";
  let projectName = "";
  let tabsPath = "";
  let soundsPath = "";
  let itemsPath = "";
  let toolsPath = "";
  let armorsPath = "";
  let blocksPath = "";
  let treesPath = "";
  let nodesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "mobs.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
    mobs = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    armorsPath = pathModule.join(projectPath, "src", "data", "armors.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
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
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    armors = fs.existsSync(armorsPath) ? fs.readJSONSync(armorsPath) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    defaultItems = fs.readJSONSync(
      isDev ? "./static/data/items.json" : "./resources/app/data/items.json"
    );
    defaultSounds = fs.readJSONSync(
      isDev ? "./static/data/sounds.json" : "./resources/app/data/sounds.json"
    );
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "mob" && !n.showInContext) || n.showInContext);
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
          mobs[selectedMob].node_data.connected_nodes.find(
            (n) => n.id == this.id
          )
        )
          return;
        mobs[selectedMob].node_data.connected_nodes.push({
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
    Object.keys(mobs).forEach((mob) => {
      mobs[mob].name = mob;
      mobs[mob].foodItem = mobs[mob].foodItem.trim()
        ? mobs[mob].foodItem
        : Object.keys(items)[0] ??
          Object.keys(tools)[0] ??
          Object.keys(armors)[0] ??
          Object.keys(blocks)[0] ??
          Object.keys(trees)[0] ??
          Object.keys(mobs)[0] ??
          defaultItems[0];
      mobs[mob].ridingItem = mobs[mob].ridingItem?.trim()
        ? mobs[mob].ridingItem
        : Object.keys(items)[0] ??
          Object.keys(tools)[0] ??
          Object.keys(armors)[0] ??
          Object.keys(blocks)[0] ??
          Object.keys(trees)[0] ??
          Object.keys(mobs)[0] ??
          defaultItems[0];
      mobs[mob].footstepSound = mobs[mob].footstepSound.trim()
        ? mobs[mob].footstepSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      mobs[mob].ambientSound = mobs[mob].ambientSound.trim()
        ? mobs[mob].ambientSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      mobs[mob].hurtSound = mobs[mob].hurtSound.trim()
        ? mobs[mob].hurtSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
      mobs[mob].deathSound = mobs[mob].deathSound.trim()
        ? mobs[mob].deathSound
        : Object.keys(sounds)[0] ?? defaultSounds[0];
    });
    selectedMob = Object.keys(mobs)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "mobs.json") return;
        mobs = data.file.data;
        updateEditor();
      } else if (data.type == "SELECTED") selectedMob = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "mobs.json", data: mobs });
    };
  });
  let selectedMob = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_mob_${Object.keys(mobs).length + 1}`;
    mobs[name] = {
      name,
      hitboxWidth: 1,
      hitboxHeight: 1,
      sizeRatio: 50,
      armor: 0,
      armorToughness: 0,
      luck: 0,
      attackSpeed: 0,
      attackDamage: 0,
      attackKnockback: 0,
      flyingSpeed: 0,
      followRange: 0,
      jumpStrength: 0,
      knockbackResistance: 0,
      maxHealth: 20,
      movementSpeed: 0,
      spawnReinforcementsChance: 0,
      stacksTo: 1,
      bgColor: "#000000",
      highlightColor: "#000000",
      tab: "none",
      controller: "walking",
      ridingItem: "none",
      foodItem:
        Object.keys(items)[0] ??
        Object.keys(tools)[0] ??
        Object.keys(armors)[0] ??
        Object.keys(blocks)[0] ??
        Object.keys(trees)[0] ??
        Object.keys(mobs)[0] ??
        defaultItems[0],
      footstepSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      ambientSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      hurtSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      deathSound: Object.keys(sounds)[0] ?? defaultSounds[0],
      rarity: "common",
      peaceful: false,
      rideable: true,
      requiresSaddle: true,
      breed: true,
      dropXp: false,
      minXp: 0,
      maxXp: 0,
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
    selectedMob = name;
    updateEditor();
    send_changes({ file: "mobs.json", data: mobs });
  }
  function save() {
    const obj = {};
    Object.keys(mobs).forEach((mob) => {
      const name = mobs[mob].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(mobs[mob]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = mobs[mob][property];
      });
    });
    fs.writeJSONSync(path, obj);
    mobs = obj;
    Object.keys(mobs).forEach((mob) => {
      mobs[mob].name = mob;
    });
    selectedMob = mobs[selectedMob] ? selectedMob : Object.keys(mobs)[0];
    success("Mobs saved successfully!");
  }
  function deleteMob() {
    if (!selectedMob) return;
    delete mobs[selectedMob];
    mobs = mobs;
    selectedMob = Object.keys(mobs)[0];
    updateEditor();
    send_changes({ file: "mobs.json", data: mobs });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture(property, filters) {
    const response = await ipc.invoke("dialog", "openFile", filters);
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const mob = fs.readFileSync(splitted.join("/"), "base64");
      mobs[selectedMob][property] = {
        name: splitted[splitted.length - 1],
        data: mob,
      };
      send_changes({ file: "mobs.json", data: mobs });
    }
  }
  function setTexture(property, ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      mobs[selectedMob][property] = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result
          .replace("data:image/png;base64,", "")
          .replace("data:application/json;base64,", ""),
      };
      send_changes({ file: "mobs.json", data: mobs });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  function addEffect() {
    mobs[selectedMob].effects.push({
      name: "absorption",
      probability: 0,
      duration: 0,
      amplifier: 1,
      ambient: true,
      visible: true,
      showIcon: true,
    });
    mobs[selectedMob].effects = mobs[selectedMob].effects;
    send_changes({ file: "mobs.json", data: mobs });
  }
  function deleteEffect(i) {
    mobs[selectedMob].effects.splice(i, 1);
    mobs[selectedMob].effects = mobs[selectedMob].effects;
    send_changes({ file: "mobs.json", data: mobs });
  }
  let editor;
  function setEditor() {
    if (!mobs[selectedMob]) return;
    editor = new LiteGraph.Editor("editor", {
      miniwindow: false,
    });
    const graph = editor.graph;
    graph.on_change = () => {
      mobs[selectedMob].node_data.connected_nodes = [];
      mobs[selectedMob].node_data.graph = graph.serialize();
      graph.runStep(1);
      send_changes({ file: "mobs.json", data: mobs });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedMob || !editor) return;
    editor.graph.configure(mobs[selectedMob].node_data.graph);
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
  <title>OpenMod - Mobs</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Mob:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedMob}
      on:change={updateEditor}
    >
      {#if !Object.keys(mobs).length}
        <option disabled value={selectedMob}>No mobs</option>
      {/if}
      {#each Object.keys(mobs) as mob}
        <option value={mob}>{mob}</option>
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
        <button class="btn btn-error" on:click={deleteMob}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if mobs[selectedMob]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={mobs[selectedMob].name}
            />
          </div>
          <div>
            <label class="text-lg">Hitbox Width</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={mobs[selectedMob].hitboxWidth}
            />
          </div>
          <div>
            <label class="text-lg">Hitbox Height</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={mobs[selectedMob].hitboxHeight}
            />
          </div>
          <div>
            <label class="text-lg">Baby Size (%)</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={mobs[selectedMob].sizeRatio}
            />
          </div>
          <div>
            <label class="text-lg">Armor</label>
            <input
              type="number"
              class="input w-full"
              bind:value={mobs[selectedMob].armor}
            />
          </div>
          <div>
            <label class="text-lg">Armor Toughness</label>
            <input
              type="number"
              class="input w-full"
              bind:value={mobs[selectedMob].armorToughness}
            />
          </div>
          <div>
            <label class="text-lg">Luck</label>
            <input
              type="number"
              class="input w-full"
              bind:value={mobs[selectedMob].luck}
            />
          </div>
          <div>
            <label class="text-lg">Attack Speed</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].attackSpeed}
            />
          </div>
          <div>
            <label class="text-lg">Attack Damage</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].attackDamage}
            />
          </div>
          <div>
            <label class="text-lg">Attack Knockback</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].attackKnockback}
            />
          </div>
          <div>
            <label class="text-lg">Flying Speed</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].flyingSpeed}
            />
          </div>
          <div>
            <label class="text-lg">Follow Range</label>
            <input
              type="number"
              class="input w-full"
              bind:value={mobs[selectedMob].followRange}
            />
          </div>
          <div>
            <label class="text-lg">Jump Strength</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].jumpStrength}
            />
          </div>
          <div>
            <label class="text-lg">Knockback Resistance</label>
            <input
              type="number"
              min="0.1"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].knockbackResistance}
            />
          </div>
          <div>
            <label class="text-lg">Max Health</label>
            <input
              type="number"
              class="input w-full"
              bind:value={mobs[selectedMob].maxHealth}
            />
          </div>
          <div>
            <label class="text-lg">Movement Speed</label>
            <input
              type="number"
              min="0"
              step="0.1"
              class="input w-full"
              bind:value={mobs[selectedMob].movementSpeed}
            />
          </div>
          <div>
            <label class="text-lg">Spawn Reinforcements Chance (%)</label>
            <input
              type="number"
              min="0"
              max="100"
              class="input w-full"
              bind:value={mobs[selectedMob].spawnReinforcementsChance}
            />
          </div>
          <div>
            <label class="text-lg">Stack Size</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={mobs[selectedMob].stacksTo}
            />
          </div>
          <div>
            <label class="text-lg">Egg Background Color</label>
            <input
              type="color"
              class="w-full"
              bind:value={mobs[selectedMob].bgColor}
            />
          </div>
          <div>
            <label class="text-lg">Egg Highlight Color</label>
            <input
              type="color"
              class="w-full"
              bind:value={mobs[selectedMob].highlightColor}
            />
          </div>
          <div>
            <label class="text-lg">Creative Tab</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].tab}
            >
              <option value="none">None</option>
              {#each Object.keys(tabs) as tab}
                <option value={tab}>{projectName.toLowerCase()}:{tab}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Movement Controller</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].controller}
            >
              <option value="walking">Walking</option>
              <option value="flying">Flying</option>
            </select>
          </div>
          {#if mobs[selectedMob].rideable}
            <div>
              <label class="text-lg">Riding Item</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={mobs[selectedMob].ridingItem}
              >
                <option value="none">None</option>
                {#each Object.keys(items) as item}
                  <option value={item}
                    >{projectName.toLowerCase()}:{item}</option
                  >
                {/each}
                {#each Object.keys(tools) as tool}
                  <option value={tool}
                    >{projectName.toLowerCase()}:{tool}</option
                  >
                {/each}
                {#each Object.keys(armors) as armor}
                  <option value={armor}
                    >{projectName.toLowerCase()}:{armor}</option
                  >
                {/each}
                {#each Object.keys(blocks) as block}
                  <option value={block}
                    >{projectName.toLowerCase()}:{block}</option
                  >
                {/each}
                {#each Object.keys(trees) as tree}
                  <option value={tree}
                    >{projectName.toLowerCase()}:{tree}</option
                  >
                {/each}
                {#each Object.keys(mobs) as mob}
                  <option value={mob}>{projectName.toLowerCase()}:{mob}</option>
                {/each}
                {#each defaultItems as item}
                  <option value={item}>{item}</option>
                {/each}</select
              >
            </div>
          {/if}
          <div>
            <label class="text-lg">Food Item</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].foodItem}
              >{#each Object.keys(items) as item}
                <option value={item}>{projectName.toLowerCase()}:{item}</option>
              {/each}
              {#each Object.keys(tools) as tool}
                <option value={tool}>{projectName.toLowerCase()}:{tool}</option>
              {/each}
              {#each Object.keys(armors) as armor}
                <option value={armor}
                  >{projectName.toLowerCase()}:{armor}</option
                >
              {/each}
              {#each Object.keys(blocks) as block}
                <option value={block}
                  >{projectName.toLowerCase()}:{block}</option
                >
              {/each}
              {#each Object.keys(trees) as tree}
                <option value={tree}>{projectName.toLowerCase()}:{tree}</option>
              {/each}
              {#each Object.keys(mobs) as mob}
                <option value={mob}>{projectName.toLowerCase()}:{mob}</option>
              {/each}
              {#each defaultItems as item}
                <option value={item}>{item}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Walk Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={mobs[selectedMob].footstepSound}
            />
          </div>
          <div>
            <label class="text-lg">Ambient Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={mobs[selectedMob].ambientSound}
            />
          </div>
          <div>
            <label class="text-lg">Hit Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={mobs[selectedMob].hurtSound}
            />
          </div>
          <div>
            <label class="text-lg">Death Sound</label>
            <input
              list="soundList"
              class="input w-full"
              bind:value={mobs[selectedMob].deathSound}
            />
          </div>
          <div>
            <label class="text-lg">Rarity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].rarity}
              ><option value="common">Common Rarity</option><option
                value="uncommon">Uncommon Rarity</option
              ><option value="rare">Rare Rarity</option><option value="epic"
                >Epic Rarity</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Should Despawn In Peaceful Difficulty?</label
            >
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].peaceful}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Rideable?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].rideable}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if mobs[selectedMob].rideable}
            <div>
              <label class="text-lg">Requires Saddle?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={mobs[selectedMob].requiresSaddle}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
          {/if}
          <div>
            <label class="text-lg">Can Breed?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].breed}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Can Drop Experience?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].dropXp}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if mobs[selectedMob].dropXp}
            <div>
              <label class="text-lg">Minimum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={mobs[selectedMob].minXp}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Experience</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={mobs[selectedMob].maxXp}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Fuel?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].fuel}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          {#if mobs[selectedMob].fuel}
            <div>
              <label class="text-lg">Burn Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={mobs[selectedMob].burnTime}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Is Food?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].food}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Fire Resistant?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={mobs[selectedMob].fireResistant}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div class="col-start-1">
            <label class="text-lg">Texture</label>
            <img
              class="w-48 h-48 cursor-pointer rounded-lg"
              src={`data:image/png;base64,${mobs[selectedMob].texture?.data}`}
              on:error={fallbackTexture}
              on:click={chooseTexture.bind(this, "texture", "png")}
              on:drop={setTexture.bind(this, "texture")}
              on:dragover|preventDefault
            />
          </div>
          {#if mobs[selectedMob].rideable && mobs[selectedMob].requiresSaddle}
            <div>
              <label class="text-lg">Saddled Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${mobs[selectedMob].saddled?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "saddled", "png")}
                on:drop={setTexture.bind(this, "saddled")}
                on:dragover|preventDefault
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Geo</label>
            <div
              class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
              style="{mobs[selectedMob].geo
                ? 'background-color: rgba(0,0,0,0.3)'
                : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
              on:click={chooseTexture.bind(this, "geo", "json")}
              on:drop={setTexture.bind(this, "geo")}
              on:dragover|preventDefault
            >
              {mobs[selectedMob].geo?.name ?? ""}
            </div>
          </div>
          <div>
            <label class="text-lg">Animations</label>
            <div
              class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
              style="{mobs[selectedMob].animation
                ? 'background-color: rgba(0,0,0,0.3)'
                : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
              on:click={chooseTexture.bind(this, "animation", "json")}
              on:drop={setTexture.bind(this, "animation")}
              on:dragover|preventDefault
            >
              {mobs[selectedMob].animation?.name ?? ""}
            </div>
          </div>
        </div>
      </Accordion>
      {#if mobs[selectedMob].food}
        <Accordion title="Food">
          <div class="grid grid-cols-3 gap-3">
            <div>
              <label class="text-lg">Is Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={mobs[selectedMob].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Can Be Eaten Fast?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={mobs[selectedMob].food_fast}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Is Meat?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={mobs[selectedMob].food_meat}
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
                bind:value={mobs[selectedMob].food_nutrition}
              />
            </div>
            <div>
              <label class="text-lg">Saturation (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                class="input w-full"
                bind:value={mobs[selectedMob].food_saturationMod}
              />
            </div>
          </div>
        </Accordion>
        <Accordion title="Effects">
          <div class="flex flex-col gap-3">
            {#each mobs[selectedMob].effects as effect, index}
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
<datalist id="soundList">
  {#each Object.keys(sounds) as sound}
    <option value="{projectName.toLowerCase()}:{sound}" />
  {/each}
  {#each defaultSounds as sound}
    <option value={sound} />
  {/each}
</datalist>
