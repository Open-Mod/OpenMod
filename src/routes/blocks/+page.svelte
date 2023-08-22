<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let blocks = {};
  let tabs = {};
  let tiers = {};
  let sounds = {};
  let defaultBiomes = [];
  let projectPath = "";
  let path = "";
  let tabsPath = "";
  let tiersPath = "";
  let soundsPath = "";
  let nodesPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "blocks.json");
    tabsPath = pathModule.join(projectPath, "src", "data", "tabs.json");
    tiersPath = pathModule.join(projectPath, "src", "data", "tiers.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
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
    blocks = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tabs = fs.existsSync(tabsPath) ? fs.readJSONSync(tabsPath) : {};
    tiers = fs.existsSync(tiersPath) ? fs.readJSONSync(tiersPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    defaultBiomes = fs.readJSONSync("./src/data/biomes.json");
    nodes = fs
      .readdirSync(nodesPath)
      .map((n) => fs.readJSONSync(pathModule.join(nodesPath, n)))
      .filter((n) => (n.for == "block" && !n.showInContext) || n.showInContext);
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
    });
    selectedBlock = Object.keys(blocks)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "blocks.json") return;
      blocks = data.file.content;
    };
  });
  let selectedBlock = "";
  let name = "";
  let nodes = [];
  function add() {
    name = `new_block_${Object.keys(blocks).length + 1}`;
    blocks[name] = {
      name,
      resistance: 1,
      explosion_resistance: 1,
      fire_resistance: 1,
      lightLevel: 1,
      friction: 40,
      jumpFactor: 100,
      speedFactor: 100,
      type: "normal",
      tab: "none",
      mapColor: "none",
      instrument: "none",
      breakSound: "",
      walkSound: "",
      placeSound: "",
      hitSound: "",
      pushReaction: "ignore",
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
      worlds: ["overworld"],
      modelType: "default",
      model: "",
      texture: [],
      particleTexture: "",
      upTexture: "",
      downTexture: "",
      frontTexture: "",
      backTexture: "",
      rightTexture: "",
      leftTexture: "",
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
    send_changes({ file: "blocks.json", content: blocks });
  }
  function save() {
    const obj = {};
    const blockTextures = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "block"
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
    const blockModels = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "block"
    );
    const blockstates = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "blockstates"
    );
    const projectMinecraftData = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "tags",
      "blocks"
    );
    const minecraftData = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      "minecraft",
      "tags",
      "blocks"
    );
    const minecraftMineable = pathModule.join(minecraftData, "mineable");
    const forgeData = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      "forge",
      "tags",
      "blocks"
    );
    const biomeModifier = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "forge",
      "biome_modifier"
    );
    const worldgenConfigured = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "worldgen",
      "configured_feature"
    );
    const worldgenPlaced = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "worldgen",
      "placed_feature"
    );
    fs.rmSync(blockTextures, { recursive: true, force: true });
    fs.rmSync(blockModels, { recursive: true, force: true });
    fs.rmSync(blockstates, { recursive: true, force: true });
    fs.rmSync(minecraftData, { recursive: true, force: true });
    fs.rmSync(forgeData, { recursive: true, force: true });
    fs.rmSync(biomeModifier, { recursive: true, force: true });
    fs.rmSync(worldgenConfigured, { recursive: true, force: true });
    fs.rmSync(worldgenPlaced, { recursive: true, force: true });
    fs.mkdirSync(blockTextures);
    fs.mkdirSync(blockModels);
    fs.mkdirSync(blockstates);
    fs.mkdirSync(minecraftData);
    fs.mkdirSync(minecraftMineable);
    fs.mkdirSync(forgeData);
    fs.mkdirSync(biomeModifier);
    fs.mkdirSync(worldgenConfigured);
    fs.mkdirSync(worldgenPlaced);
    Object.keys(blocks).forEach((block) => {
      const oldModel = pathModule.join(itemModels, `${block}.json`);
      if (fs.existsSync(oldModel)) fs.rmSync(oldModel);
      const name = blocks[block].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const itemModelPath = pathModule.join(itemModels, `${name}.json`);
      const modelPath = pathModule.join(blockModels, `${name}.json`);
      if (
        blocks[block].modelType == "default" &&
        blocks[block].type == "normal"
      ) {
        const particleTexture = blocks[block].particleTexture;
        const upTexture = blocks[block].upTexture;
        const downTexture = blocks[block].downTexture;
        const frontTexture = blocks[block].frontTexture;
        const backTexture = blocks[block].backTexture;
        const rightTexture = blocks[block].rightTexture;
        const leftTexture = blocks[block].leftTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/cube",
          textures: {},
        };
        if (particleTexture) {
          const particleTextureType =
            particleTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const particleTexturePath = pathModule.join(
            blockTextures,
            `particle_${name}.${particleTextureType}`
          );
          const particleTextureData = particleTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(particleTexturePath, particleTextureData, "base64");
          modelObj.textures.particle = `${projectName.toLowerCase()}:block/particle_${name}`;
        }
        if (upTexture) {
          const upTextureType = upTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const upTexturePath = pathModule.join(
            blockTextures,
            `up_${name}.${upTextureType}`
          );
          const upTextureData = upTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(upTexturePath, upTextureData, "base64");
          modelObj.textures.up = `${projectName.toLowerCase()}:block/up_${name}`;
        }
        if (downTexture) {
          const downTextureType = downTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const downTexturePath = pathModule.join(
            blockTextures,
            `down_${name}.${downTextureType}`
          );
          const downTextureData = downTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(downTexturePath, downTextureData, "base64");
          modelObj.textures.down = `${projectName.toLowerCase()}:block/down_${name}`;
        }
        if (frontTexture) {
          const frontTextureType = frontTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const frontTexturePath = pathModule.join(
            blockTextures,
            `front_${name}.${frontTextureType}`
          );
          const frontTextureData = frontTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(frontTexturePath, frontTextureData, "base64");
          modelObj.textures.north = `${projectName.toLowerCase()}:block/front_${name}`;
        }
        if (backTexture) {
          const backTextureType = backTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const backTexturePath = pathModule.join(
            blockTextures,
            `back_${name}.${backTextureType}`
          );
          const backTextureData = backTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(backTexturePath, backTextureData, "base64");
          modelObj.textures.south = `${projectName.toLowerCase()}:block/back_${name}`;
        }
        if (rightTexture) {
          const rightTextureType = rightTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const rightTexturePath = pathModule.join(
            blockTextures,
            `right_${name}.${rightTextureType}`
          );
          const rightTextureData = rightTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(rightTexturePath, rightTextureData, "base64");
          modelObj.textures.east = `${projectName.toLowerCase()}:block/right_${name}`;
        }
        if (leftTexture) {
          const leftTextureType = leftTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const leftTexturePath = pathModule.join(
            blockTextures,
            `left_${name}.${leftTextureType}`
          );
          const leftTextureData = leftTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(leftTexturePath, leftTextureData, "base64");
          modelObj.textures.west = `${projectName.toLowerCase()}:block/left_${name}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${name}`,
        });
      } else if (
        blocks[block].modelType == "default" &&
        blocks[block].type == "stairs"
      ) {
        const innerModelPath = pathModule.join(
          blockModels,
          `${name}_inner.json`
        );
        const outerModelPath = pathModule.join(
          blockModels,
          `${name}_outer.json`
        );
        const particleTexture = blocks[block].particleTexture;
        const topTexture = blocks[block].upTexture;
        const bottomTexture = blocks[block].downTexture;
        const sideTexture = blocks[block].rightTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/stairs",
          textures: {},
        };
        if (particleTexture) {
          const particleTextureType =
            particleTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const particleTexturePath = pathModule.join(
            blockTextures,
            `particle_${name}.${particleTextureType}`
          );
          const particleTextureData = particleTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(particleTexturePath, particleTextureData, "base64");
          modelObj.textures.particle = `${projectName.toLowerCase()}:block/particle_${name}`;
        }
        if (topTexture) {
          const topTextureType = topTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const topTexturePath = pathModule.join(
            blockTextures,
            `top_${name}.${topTextureType}`
          );
          const topTextureData = topTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(topTexturePath, topTextureData, "base64");
          modelObj.textures.top = `${projectName.toLowerCase()}:block/top_${name}`;
        }
        if (bottomTexture) {
          const bottomTextureType = bottomTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const bottomTexturePath = pathModule.join(
            blockTextures,
            `bottom_${name}.${bottomTextureType}`
          );
          const bottomTextureData = bottomTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(bottomTexturePath, bottomTextureData, "base64");
          modelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${name}`;
        }
        if (sideTexture) {
          const sideTextureType = sideTexture.match(/[^:/]\w+(?=;|,)/)[0];
          const sideTexturePath = pathModule.join(
            blockTextures,
            `side_${name}.${sideTextureType}`
          );
          const sideTextureData = sideTexture.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(sideTexturePath, sideTextureData, "base64");
          modelObj.textures.side = `${projectName.toLowerCase()}:block/side_${name}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(
          innerModelPath,
          Object.assign({ parent: "minecraft:block/inner_stairs" }, modelObj)
        );
        fs.writeJSONSync(
          outerModelPath,
          Object.assign({ parent: "minecraft:block/outer_stairs" }, modelObj)
        );
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${name}`,
        });
      } else if (blocks[block].modelType == "blockbench") {
        const model = blocks[block].model;
        const modelData = model.data.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        const textures = blocks[block].texture;
        textures.forEach((texture) => {
          const texturePath = pathModule.join(blockTextures, `${texture.name}`);
          const textureData = texture.data.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(texturePath, textureData, "base64");
        });
        fs.writeFileSync(modelPath, modelData, "base64");
        fs.writeFileSync(itemModelPath, modelData, "base64");
      }
      const statePath = pathModule.join(blockstates, `${name}.json`);
      const mineablePath = pathModule.join(
        minecraftMineable,
        `${blocks[block].minedBy}.json`
      );
      let tierPath;
      if (["netherite", "gold"].includes(blocks[selectedBlock].minedByTier))
        tierPath = pathModule.join(
          forgeData,
          `needs_${blocks[block].minedByTier}_tool.json`
        );
      else if (
        ["wood", "stone", "iron", "diamond"].includes(
          blocks[selectedBlock].minedByTier
        )
      )
        tierPath = pathModule.join(
          minecraftData,
          `needs_${blocks[block].minedByTier}_tool.json`
        );
      else
        tierPath = pathModule.join(
          projectMinecraftData,
          `needs_${blocks[block].minedByTier}_tool.json`
        );
      if (blocks[block].type == "normal") {
        fs.writeJSONSync(statePath, {
          variants: {
            "": {
              model: `${projectName.toLowerCase()}:block/${name}`,
            },
          },
        });
      } else if (blocks[block].type == "stairs") {
        fs.writeJSONSync(statePath, {
          variants: {
            "facing=east,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 270,
            },
            "facing=east,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
            },
            "facing=east,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 270,
            },
            "facing=east,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
            },
            "facing=east,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
            },
            "facing=east,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
            },
            "facing=east,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=east,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
            },
            "facing=east,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=east,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 180,
            },
            "facing=north,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 180,
            },
            "facing=north,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=north,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=north,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=south,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
            },
            "facing=south,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
            },
            "facing=south,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=south,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=south,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=south,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=south,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=west,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 90,
            },
            "facing=west,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 90,
            },
            "facing=west,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=west,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${name}_inner`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=west,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=west,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${name}_outer`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=west,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${name}`,
              uvlock: true,
              x: 180,
              y: 180,
            },
          },
        });
      }
      if (blocks[block].dropItem) {
        if (
          fs.existsSync(mineablePath) &&
          blocks[block].minedBy != "anything"
        ) {
          const mineable = fs.readJSONSync(mineablePath);
          const tier = fs.readJSONSync(tierPath);
          mineable.values.push(`${projectName.toLowerCase()}:${name}`);
          tier.values.push(`${projectName.toLowerCase()}:${name}`);
          fs.writeJSONSync(mineablePath, mineable);
          fs.writeJSONSync(tierPath, tier);
        } else if (blocks[block].minedBy != "anything") {
          fs.writeJSONSync(mineablePath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${name}`],
          });
          fs.writeJSONSync(tierPath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${name}`],
          });
        }
      }
      if (blocks[block].isOre && blocks[block].worlds.length) {
        const configurePath = pathModule.join(
          worldgenConfigured,
          `${name}.json`
        );
        const placedPath = pathModule.join(worldgenPlaced, `${name}.json`);
        const biomePath = pathModule.join(biomeModifier, `${name}.json`);
        const targets = [];
        const biomes = [];
        blocks[block].worlds.forEach((world) => {
          let oreType = "";
          if (world == "overworld")
            oreType = {
              tag: "tag_match",
              ore: "stone_ore_replaceables",
              biomes: defaultBiomes
                .filter((biome) => biome.dimension == world)
                .map((biome) => biome.name),
            };
          else if (world == "nether")
            oreType = {
              tag: "block_match",
              ore: "netherrack",
              biomes: defaultBiomes
                .filter((biome) => biome.dimension == world)
                .map((biome) => biome.name),
            };
          else if (world == "end")
            oreType = {
              tag: "block_match",
              ore: "end_stone",
              biomes: defaultBiomes
                .filter((biome) => biome.dimension == world)
                .map((biome) => biome.name),
            };
          targets.push({
            target: {
              predicate_type: `minecraft:${oreType.tag}`,
              tag: `minecraft:${oreType.ore}`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${name}`,
            },
          });
          biomes.push(oreType.biomes);
        });
        fs.writeJSONSync(configurePath, {
          type: "minecraft:ore",
          config: {
            size: blocks[block].oreSize,
            discard_chance_on_air_exposure: blocks[block].discardChance / 100,
            targets,
          },
        });
        fs.writeJSONSync(placedPath, {
          feature: `${projectName.toLowerCase()}:${name}`,
          placement: [
            {
              type: "minecraft:count",
              count: {
                type: "minecraft:uniform",
                value: {
                  min_inclusive: blocks[block].minChunkSize,
                  max_inclusive: blocks[block].maxChunkSize,
                },
              },
            },
            {
              type: "minecraft:in_square",
            },
            {
              type: "minecraft:height_range",
              height: {
                type: blocks[block].genShape,
                min_inclusive: {
                  absolute: blocks[block].minHeight,
                },
                max_inclusive: {
                  absolute: blocks[block].maxHeight,
                },
              },
            },
            {
              type: "minecraft:biome",
            },
          ],
        });
        fs.writeJSONSync(biomePath, {
          type: "forge:add_features",
          features: `${projectName.toLowerCase()}:${name}`,
          step: "underground_ores",
          biomes: biomes.flat(),
        });
      }
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
    selectedBlock = Object.keys(blocks)[0];
    success("Blocks saved successfully!");
  }
  function deleteBlock() {
    if (!selectedBlock) return;
    delete blocks[selectedBlock];
    blocks = blocks;
    selectedBlock = Object.keys(blocks)[0];
    updateEditor();
    send_changes({ file: "blocks.json", content: blocks });
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
      blocks[selectedBlock].model = model;
      blocks[selectedBlock].texture = paths;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setModel(ev) {
    let i = 0;
    const reader = new FileReader();
    reader.onload = function (event) {
      if (i == 0) {
        blocks[selectedBlock].model = {
          name: files[i].name,
          data: event.target.result,
        };
        blocks[selectedBlock].texture = [];
      } else
        blocks[selectedBlock].texture.push({
          name: files[i].name,
          data: event.target.result,
        });
    };
    reader.onloadend = function () {
      i++;
      if (!files[i])
        return send_changes({ file: "blocks.json", content: blocks });
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
  async function chooseParticleTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[
        selectedBlock
      ].particleTexture = `data:image/png;base64,${texture}`;
    }
  }
  function setParticleTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].particleTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseUpTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].upTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setUpTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].upTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseDownTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].downTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setDownTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].downTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseFrontTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].frontTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setFrontTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].frontTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseBackTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].backTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setBackTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].backTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseRightTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].rightTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setRightTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].rightTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseLeftTexture() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      blocks[selectedBlock].leftTexture = `data:image/png;base64,${texture}`;
      send_changes({ file: "blocks.json", content: blocks });
    }
  }
  function setLeftTexture(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      blocks[selectedBlock].leftTexture = event.target.result;
      send_changes({ file: "blocks.json", content: blocks });
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
    send_changes({ file: "blocks.json", content: blocks });
  }
  function deleteEffect(i) {
    blocks[selectedBlock].effects.splice(i, 1);
    blocks[selectedBlock].effects = blocks[selectedBlock].effects;
    send_changes({ file: "blocks.json", content: blocks });
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
      send_changes({ file: "blocks.json", content: blocks });
    };
    updateEditor();
    window.onresize = () => {
      editor.graphcanvas.resize();
    };
  }
  function updateEditor() {
    if (!selectedBlock || !editor) return;
    editor.graph.configure(blocks[selectedBlock].node_data.graph);
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
            <label class="text-lg">Resistance (%)</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={blocks[selectedBlock].resistance}
            />
          </div>
          <div>
            <label class="text-lg">Explosion Resistance (%)</label>
            <input
              type="number"
              min="1"
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
            <label class="text-lg">Jump Power (%)</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={blocks[selectedBlock].jumpFactor}
            />
          </div>
          <div>
            <label class="text-lg">Movement Speed (%)</label>
            <input
              type="number"
              min="0"
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
                <option value={tab}>{convertToCamelCase(tab)}</option>
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
            <select
              class="select font-normal text-base w-full"
              bind:value={blocks[selectedBlock].breakSound}
            >
              {#if !Object.keys(sounds)}
                <option disabled value={blocks[selectedBlock].breakSound}
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
              bind:value={blocks[selectedBlock].walkSound}
            >
              {#if !Object.keys(sounds)}
                <option disabled value={blocks[selectedBlock].walkSound}
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
              bind:value={blocks[selectedBlock].placeSound}
            >
              {#if !Object.keys(sounds)}
                <option disabled value={blocks[selectedBlock].placeSound}
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
              bind:value={blocks[selectedBlock].hitSound}
            >
              {#if !Object.keys(sounds)}
                <option disabled value={blocks[selectedBlock].hitSound}
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
              bind:value={blocks[selectedBlock].pushReaction}
              ><option value="ignore">Do Nothing</option><option value="block"
                >Block</option
              ><option value="push_only">Push Only</option><option
                value="destroy">Destroy</option
              ><option value="normal">Normal</option></select
            >
          </div>
          <div>
            <label class="text-lg">Drops Experience?</label>
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
            <label class="text-lg">Drops Item?</label>
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
            <label class="text-lg">Generate As Ore?</label>
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
              <label class="text-lg">Textures & Model</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].texture[0]?.data ?? ""}
                on:error={fallbackTexture}
                on:click={chooseModel}
                on:drop={setModel}
                on:dragover|preventDefault
              />
            </div>
          {/if}
          {#if blocks[selectedBlock].modelType == "default" && blocks[selectedBlock].type == "normal"}
            <div class="col-start-1">
              <label class="text-lg">Particle Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].particleTexture}
                on:error={fallbackTexture}
                on:click={chooseParticleTexture}
                on:drop={setParticleTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Up Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].upTexture}
                on:error={fallbackTexture}
                on:click={chooseUpTexture}
                on:drop={setUpTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Down Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].downTexture}
                on:error={fallbackTexture}
                on:click={chooseDownTexture}
                on:drop={setDownTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Front Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].frontTexture}
                on:error={fallbackTexture}
                on:click={chooseFrontTexture}
                on:drop={setFrontTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Back Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].backTexture}
                on:error={fallbackTexture}
                on:click={chooseBackTexture}
                on:drop={setBackTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Right Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].rightTexture}
                on:error={fallbackTexture}
                on:click={chooseRightTexture}
                on:drop={setRightTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Left Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].leftTexture}
                on:error={fallbackTexture}
                on:click={chooseLeftTexture}
                on:drop={setLeftTexture}
                on:dragover|preventDefault
              />
            </div>
          {:else if blocks[selectedBlock].modelType == "default" && blocks[selectedBlock].type == "stairs"}
            <div class="col-start-1">
              <label class="text-lg">Particle Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].particleTexture}
                on:error={fallbackTexture}
                on:click={chooseParticleTexture}
                on:drop={setParticleTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Top Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].upTexture}
                on:error={fallbackTexture}
                on:click={chooseUpTexture}
                on:drop={setUpTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Bottom Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].downTexture}
                on:error={fallbackTexture}
                on:click={chooseDownTexture}
                on:drop={setDownTexture}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Side Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={blocks[selectedBlock].rightTexture}
                on:error={fallbackTexture}
                on:click={chooseRightTexture}
                on:drop={setRightTexture}
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
              <label class="text-lg">Always Eatable?</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].food_alwaysEat}
                ><option value={true}>True</option><option value={false}
                  >False</option
                ></select
              >
            </div>
            <div>
              <label class="text-lg">Eat Fast?</label>
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
                class="input w-full"
                bind:value={blocks[selectedBlock].minChunkSize}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Amount Per Chunk</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={blocks[selectedBlock].maxChunkSize}
              />
            </div>
            <div>
              <label class="text-lg">Minimum Height</label>
              <input
                type="number"
                class="input w-full"
                bind:value={blocks[selectedBlock].minHeight}
              />
            </div>
            <div>
              <label class="text-lg">Maximum Height</label>
              <input
                type="number"
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
                >Worlds to generate in (hold <a class="text-warning">ctrl</a> to
                select multiple)</label
              >
              <select
                multiple
                class="select font-normal text-base w-full"
                bind:value={blocks[selectedBlock].worlds}
                ><option value="overworld">Overworld</option><option
                  value="nether">The Nether</option
                ><option value="end">The End</option></select
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
