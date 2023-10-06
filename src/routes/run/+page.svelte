<script>
  import { onMount } from "svelte";
  let running = false;
  let errors = [];
  let output = "";
  let tab = 0;
  let defaultBiomes = [];
  let projectPath = "";
  let path = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "gradlew.bat");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    running = ipc.sendSync("isRunning");
    errors = ipc.sendSync("lastError");
    output = ipc.sendSync("lastLog");
    defaultBiomes = fs.readJSONSync(
      isDev ? "./static/data/biomes.json" : "./resources/app/data/biomes.json"
    );
    ipc.on("err", (ev, data) => {
      errors = data;
    });
    ipc.on("log", (ev, data) => {
      output = data;
    });
    ipc.on("exit", (ev) => {
      running = false;
    });
  });
  function setTab(t) {
    tab = t;
  }
  function parse(json) {
    let data = {};
    try {
      data = JSON.parse(json);
    } catch {}
    return data;
  }
  function run(t) {
    if (running == t) return;
    running = t;
    if (running == false) return ipc.invoke("stop");
    errors = [];
    output = "";
    ipc.invoke("clearLogs");
    const itemsFile = pathModule.join(projectPath, "src", "data", "items.json");
    const items = fs.existsSync(itemsFile) ? fs.readJSONSync(itemsFile) : {};
    const mobsFile = pathModule.join(projectPath, "src", "data", "mobs.json");
    const mobs = fs.existsSync(mobsFile) ? fs.readJSONSync(mobsFile) : {};
    const blocksFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "blocks.json"
    );
    const blocks = fs.existsSync(blocksFile) ? fs.readJSONSync(blocksFile) : {};
    const treesFile = pathModule.join(projectPath, "src", "data", "trees.json");
    const trees = fs.existsSync(treesFile) ? fs.readJSONSync(treesFile) : {};
    const armorsFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "armors.json"
    );
    const armors = fs.existsSync(armorsFile) ? fs.readJSONSync(armorsFile) : {};
    const toolsFile = pathModule.join(projectPath, "src", "data", "tools.json");
    const tools = fs.existsSync(toolsFile) ? fs.readJSONSync(toolsFile) : {};
    const potionsFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "potions.json"
    );
    const potions = fs.existsSync(potionsFile)
      ? fs.readJSONSync(potionsFile)
      : {};
    const materialsFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "materials.json"
    );
    const materials = fs.existsSync(materialsFile)
      ? fs.readJSONSync(materialsFile)
      : {};
    const tiersFile = pathModule.join(projectPath, "src", "data", "tiers.json");
    const tiers = fs.existsSync(tiersFile) ? fs.readJSONSync(tiersFile) : {};
    const tabsFile = pathModule.join(projectPath, "src", "data", "tabs.json");
    const tabs = fs.existsSync(tabsFile) ? fs.readJSONSync(tabsFile) : {};
    const recipesFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "recipes.json"
    );
    const recipes = fs.existsSync(recipesFile)
      ? fs.readJSONSync(recipesFile)
      : {};
    const biomesFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "biomes.json"
    );
    const biomes = fs.existsSync(biomesFile) ? fs.readJSONSync(biomesFile) : {};
    const loottablesFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "loottables.json"
    );
    const loottables = fs.existsSync(loottablesFile)
      ? fs.readJSONSync(loottablesFile)
      : {};
    const soundsFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "sounds.json"
    );
    const sounds = fs.existsSync(soundsFile) ? fs.readJSONSync(soundsFile) : {};
    Object.keys(items).forEach((item) => {
      Object.keys(items[item]).forEach((property) => {
        if (
          items[item][property] == null ||
          String(items[item][property]).trim() == ""
        ) {
          if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of item "${item}" must not be empty!`
            );
          else if (property == "burnTime" && items[item].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of item "${item}" must not be empty!`
            );
          else if (property == "food_nutrition" && items[item].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Nutrition" of item "${item}" must not be empty!`
            );
          else if (property == "food_saturationMod" && items[item].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Saturation" of item "${item}" must not be empty!`
            );
        }
      });
      if (!items[item].food) return;
      items[item].effects.forEach((effect) => {
        Object.keys(effect).forEach((property) => {
          if (
            effect[property] == null ||
            String(effect[property]).trim() == ""
          ) {
            if (property == "probability")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Probability" of effect "${
                  effect.name
                }" from item "${item}" must not be empty!`
              );
            else if (property == "duration")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Duration" of effect "${
                  effect.name
                }" from item "${item}" must not be empty!`
              );
            else if (property == "amplifier")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Level" of effect "${
                  effect.name
                }" from item "${item}" must not be empty!`
              );
          }
        });
      });
    });
    Object.keys(mobs).forEach((mob) => {
      Object.keys(mobs[mob]).forEach((property) => {
        if (
          mobs[mob][property] == null ||
          String(mobs[mob][property]).trim() == ""
        ) {
          if (property == "hitboxWidth")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Hitbox Width" of mob "${mob}" must not be empty!`
            );
          else if (property == "hitboxHeight")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Hitbox Height" of mob "${mob}" must not be empty!`
            );
          else if (property == "sizeRatio")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Baby Size" of mob "${mob}" must not be empty!`
            );
          else if (property == "armor")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Armor" of mob "${mob}" must not be empty!`
            );
          else if (property == "armorToughness")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Armor Toughness" of mob "${mob}" must not be empty!`
            );
          else if (property == "luck")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Luck" of mob "${mob}" must not be empty!`
            );
          else if (property == "attackSpeed")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Speed" of mob "${mob}" must not be empty!`
            );
          else if (property == "attackDamage")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Damage" of mob "${mob}" must not be empty!`
            );
          else if (property == "attackKnockback")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Knockback" of mob "${mob}" must not be empty!`
            );
          else if (property == "flyingSpeed")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Flying Speed" of mob "${mob}" must not be empty!`
            );
          else if (property == "followRange")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Follow Range" of mob "${mob}" must not be empty!`
            );
          else if (property == "jumpStrength")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Jump Strength" of mob "${mob}" must not be empty!`
            );
          else if (property == "knockbackResistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Knockback Resistance" of mob "${mob}" must not be empty!`
            );
          else if (property == "maxHealth")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Max Health" of mob "${mob}" must not be empty!`
            );
          else if (property == "movementSpeed")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Movement Speed" of mob "${mob}" must not be empty!`
            );
          else if (property == "spawnReinforcementsChance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Spawn Reinforcements Chance" of mob "${mob}" must not be empty!`
            );
          else if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of mob "${mob}" must not be empty!`
            );
          else if (property == "bgColor" && mobs[mob].modelType == "default")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Egg Background Color" of mob "${mob}" must not be empty!`
            );
          else if (
            property == "highlightColor" &&
            mobs[mob].modelType == "default"
          )
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Egg Highlight Color" of mob "${mob}" must not be empty!`
            );
          else if (property == "burnTime" && mobs[mob].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of mob "${mob}" must not be empty!`
            );
          else if (property == "food_nutrition" && mobs[mob].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Nutrition" of mob "${mob}" must not be empty!`
            );
          else if (property == "food_saturationMod" && mobs[mob].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Saturation" of mob "${mob}" must not be empty!`
            );
        }
      });
      if (!mobs[mob].food) return;
      mobs[mob].effects.forEach((effect) => {
        Object.keys(effect).forEach((property) => {
          if (
            effect[property] == null ||
            String(effect[property]).trim() == ""
          ) {
            if (property == "probability")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Probability" of effect "${
                  effect.name
                }" from mob "${mob}" must not be empty!`
              );
            else if (property == "duration")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Duration" of effect "${
                  effect.name
                }" from mob "${mob}" must not be empty!`
              );
            else if (property == "amplifier")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Level" of effect "${
                  effect.name
                }" from mob "${mob}" must not be empty!`
              );
          }
        });
      });
    });
    Object.keys(blocks).forEach((block) => {
      Object.keys(blocks[block]).forEach((property) => {
        if (
          blocks[block][property] == null ||
          String(blocks[block][property]).trim() == ""
        ) {
          if (property == "resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Resistance" of block "${block}" must not be empty!`
            );
          else if (property == "explosion_resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Explosion Resistance" of block "${block}" must not be empty!`
            );
          else if (property == "fire_resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Fire Resistance" of block "${block}" must not be empty!`
            );
          else if (property == "lightLevel")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Light Level" of block "${block}" must not be empty!`
            );
          else if (property == "friction")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Friction" of block "${block}" must not be empty!`
            );
          else if (property == "jumpFactor")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Jump Power" of block "${block}" must not be empty!`
            );
          else if (property == "speedFactor")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Movement Speed" of block "${block}" must not be empty!`
            );
          else if (property == "breakSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Break Sound" of block "${block}" must not be empty!`
            );
          else if (property == "walkSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Walk Sound" of block "${block}" must not be empty!`
            );
          else if (property == "placeSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Place Sound" of block "${block}" must not be empty!`
            );
          else if (property == "hitSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Hit Sound" of block "${block}" must not be empty!`
            );
          else if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of block "${block}" must not be empty!`
            );
          else if (property == "burnTime" && blocks[block].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of block "${block}" must not be empty!`
            );
          else if (property == "food_nutrition" && blocks[block].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Nutrition" of block "${block}" must not be empty!`
            );
          else if (property == "food_saturationMod" && blocks[block].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Saturation" of block "${block}" must not be empty!`
            );
          else if (property == "minXp" && blocks[block].dropXp)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Minimum Experience" of block "${block}" must not be empty!`
            );
          else if (property == "maxXp" && blocks[block].dropXp)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Maximum Experience" of block "${block}" must not be empty!`
            );
          else if (property == "oreSize" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Vein Size" of block "${block}" must not be empty!`
            );
          else if (property == "discardChance" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Discard On Air Exposure" of block "${block}" must not be empty!`
            );
          else if (property == "biomes" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Biomes" of block "${block}" must not be empty!`
            );
          else if (property == "minChunkSize" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Minimum Amount Per Chunk" of block "${block}" must not be empty!`
            );
          else if (property == "maxChunkSize" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Maximum Amount Per Chunk" of block "${block}" must not be empty!`
            );
          else if (property == "minHeight" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Minimum Height" of block "${block}" must not be empty!`
            );
          else if (property == "maxHeight" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Minimum Height" of block "${block}" must not be empty!`
            );
        }
      });
      if (!blocks[block].food) return;
      blocks[block].effects.forEach((effect) => {
        Object.keys(effect).forEach((property) => {
          if (
            effect[property] == null ||
            String(effect[property]).trim() == ""
          ) {
            if (property == "probability")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Probability" of effect "${
                  effect.name
                }" from block "${block}" must not be empty!`
              );
            else if (property == "duration")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Duration" of effect "${
                  effect.name
                }" from block "${block}" must not be empty!`
              );
            else if (property == "amplifier")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Level" of effect "${
                  effect.name
                }" from block "${block}" must not be empty!`
              );
          }
        });
      });
    });
    Object.keys(trees).forEach((tree) => {
      Object.keys(trees[tree]).forEach((property) => {
        if (
          trees[tree][property] == null ||
          String(trees[tree][property]).trim() == ""
        ) {
          if (property == "resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Resistance" of tree "${tree}" must not be empty!`
            );
          else if (property == "explosion_resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Explosion Resistance" of tree "${tree}" must not be empty!`
            );
          else if (property == "fire_resistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Fire Resistance" of tree "${tree}" must not be empty!`
            );
          else if (property == "lightLevel")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Light Level" of tree "${tree}" must not be empty!`
            );
          else if (property == "friction")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Friction" of tree "${tree}" must not be empty!`
            );
          else if (property == "jumpFactor")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Jump Power" of tree "${tree}" must not be empty!`
            );
          else if (property == "speedFactor")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Movement Speed" of tree "${tree}" must not be empty!`
            );
          else if (property == "amount")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Amount Per Biome" of tree "${tree}" must not be empty!`
            );
          else if (property == "bodyHeight")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Body Height" of tree "${tree}" must not be empty!`
            );
          else if (property == "bodyRandA")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Body Height Random A" of tree "${tree}" must not be empty!`
            );
          else if (property == "bodyRandB")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Body Height Random B" of tree "${tree}" must not be empty!`
            );
          else if (property == "leavesHeight")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Leaves Height" of tree "${tree}" must not be empty!`
            );
          else if (property == "radius")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Leaves Radius" of tree "${tree}" must not be empty!`
            );
          else if (property == "offset")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Leaves Offset" of tree "${tree}" must not be empty!`
            );
          else if (property == "breakSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Break Sound" of tree "${tree}" must not be empty!`
            );
          else if (property == "walkSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Walk Sound" of tree "${tree}" must not be empty!`
            );
          else if (property == "placeSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Place Sound" of tree "${tree}" must not be empty!`
            );
          else if (property == "hitSound")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Hit Sound" of tree "${tree}" must not be empty!`
            );
          else if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of tree "${tree}" must not be empty!`
            );
          else if (property == "burnTime" && trees[tree].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of tree "${tree}" must not be empty!`
            );
          else if (property == "food_nutrition" && trees[tree].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Nutrition" of tree "${tree}" must not be empty!`
            );
          else if (property == "food_saturationMod" && trees[tree].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Saturation" of tree "${tree}" must not be empty!`
            );
          else if (property == "minXp" && trees[tree].dropXp)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Minimum Experience" of tree "${tree}" must not be empty!`
            );
          else if (property == "maxXp" && trees[tree].dropXp)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Maximum Experience" of tree "${tree}" must not be empty!`
            );
        }
      });
      if (!trees[tree].food) return;
      trees[tree].effects.forEach((effect) => {
        Object.keys(effect).forEach((property) => {
          if (
            effect[property] == null ||
            String(effect[property]).trim() == ""
          ) {
            if (property == "probability")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Probability" of effect "${
                  effect.name
                }" from tree "${tree}" must not be empty!`
              );
            else if (property == "duration")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Duration" of effect "${
                  effect.name
                }" from tree "${tree}" must not be empty!`
              );
            else if (property == "amplifier")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Level" of effect "${
                  effect.name
                }" from tree "${tree}" must not be empty!`
              );
          }
        });
      });
    });
    Object.keys(armors).forEach((armor) => {
      Object.keys(armors[armor]).forEach((property) => {
        if (
          armors[armor][property] == null ||
          String(armors[armor][property]).trim() == ""
        ) {
          if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of armor "${armor}" must not be empty!`
            );
          else if (property == "material")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Material" of armor "${armor}" must not be empty!`
            );
          else if (property == "burnTime" && armors[armor].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of armor "${armor}" must not be empty!`
            );
        }
      });
    });
    Object.keys(tools).forEach((tool) => {
      Object.keys(tools[tool]).forEach((property) => {
        if (
          tools[tool][property] == null ||
          String(tools[tool][property]).trim() == ""
        ) {
          if (property == "stacksTo")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Stack Size" of tool "${tool}" must not be empty!`
            );
          else if (property == "attackDamage")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Damage" of tool "${tool}" must not be empty!`
            );
          else if (property == "attackSpeed")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Speed" of tool "${tool}" must not be empty!`
            );
          else if (property == "tier")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Tier" of tool "${tool}" must not be empty!`
            );
          else if (property == "burnTime" && tools[tool].fuel)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Burn Time" of tool "${tool}" must not be empty!`
            );
          else if (property == "food_nutrition" && tools[tool].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Nutrition" of tool "${tool}" must not be empty!`
            );
          else if (property == "food_saturationMod" && tools[tool].food)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Saturation" of tool "${tool}" must not be empty!`
            );
        }
      });
      if (!tools[tool].food) return;
      tools[tool].effects.forEach((effect) => {
        Object.keys(effect).forEach((property) => {
          if (
            effect[property] == null ||
            String(effect[property]).trim() == ""
          ) {
            if (property == "probability")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Probability" of effect "${
                  effect.name
                }" from tool "${tool}" must not be empty!`
              );
            else if (property == "duration")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Duration" of effect "${
                  effect.name
                }" from tool "${tool}" must not be empty!`
              );
            else if (property == "amplifier")
              addError(
                `[${formatDateToHHMMSS(
                  new Date()
                )}]: Field "Level" of effect "${
                  effect.name
                }" from tool "${tool}" must not be empty!`
              );
          }
        });
      });
    });
    Object.keys(potions).forEach((potion) => {
      Object.keys(potions[potion]).forEach((property) => {
        if (
          potions[potion][property] == null ||
          String(potions[potion][property]).trim() == ""
        ) {
          if (property == "probability")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Probability" of potion "${
                potions[potion].name
              }" of must not be empty!`
            );
          else if (property == "duration")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Duration" of potion "${
                potions[potion].name
              }" of must not be empty!`
            );
          else if (property == "amplifier")
            addError(
              `[${formatDateToHHMMSS(new Date())}]: Field "Level" of potion "${
                potions[potion].name
              }" of must not be empty!`
            );
        }
      });
    });
    Object.keys(materials).forEach((material) => {
      Object.keys(materials[material]).forEach((property) => {
        if (
          materials[material][property] == null ||
          String(materials[material][property]).trim() == ""
        ) {
          if (property == "durabilityForHelmet")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Durability For Helmet" of material "${material}" must not be empty!`
            );
          else if (property == "durabilityForBoots")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Durability For Boots" of material "${material}" must not be empty!`
            );
          else if (property == "durabilityForChestplate")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Durability For Chestplate" of material "${material}" must not be empty!`
            );
          else if (property == "durabilityForLeggings")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Durability For Leggings" of material "${material}" must not be empty!`
            );
          else if (property == "enchantmentValue")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Enchantability" of material "${material}" must not be empty!`
            );
          else if (property == "toughness")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Toughness" of material "${material}" must not be empty!`
            );
          else if (property == "knockbackResistance")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Knockback Resistance" of material "${material}" must not be empty!`
            );
          else if (property == "repairIngredient")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Repair With Item" of material "${material}" must not be empty!`
            );
        }
      });
    });
    Object.keys(tiers).forEach((tier) => {
      Object.keys(tiers[tier]).forEach((property) => {
        if (
          tiers[tier][property] == null ||
          String(tiers[tier][property]).trim() == ""
        ) {
          if (property == "level")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Level" of tier "${tier}" must not be empty!`
            );
          else if (property == "enchantmentValue")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Enchantability" of tier "${tier}" must not be empty!`
            );
          else if (property == "attackSpeed")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Speed" of tier "${tier}" must not be empty!`
            );
          else if (property == "attackDamageBonus")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Attack Damage Bonus" of tier "${tier}" must not be empty!`
            );
          else if (property == "repairIngredient")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Repair With Item" of tier "${tier}" must not be empty!`
            );
        }
      });
    });
    Object.keys(tabs).forEach((tab) => {
      Object.keys(tabs[tab]).forEach((property) => {
        if (
          tabs[tab][property] == null ||
          String(tabs[tab][property]).trim() == ""
        ) {
          if (property == "title")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Display Name" of creative tab "${tab}" must not be empty!`
            );
        }
      });
    });
    Object.keys(recipes).forEach((recipe) => {
      Object.keys(recipes[recipe]).forEach((property) => {
        if (
          recipes[recipe][property] == null ||
          String(recipes[recipe][property]).trim() == ""
        ) {
          if (
            property == "resultCount" &&
            (recipes[recipe].type == "shaped" ||
              recipes[recipe].type == "shapeless")
          )
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Result Item Count" of recipe "${recipe}" must not be empty!`
            );
          else if (
            property == "firstItem" &&
            recipes[recipe].type == "smelting"
          )
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Item" of recipe "${recipe}" must not be empty!`
            );
          else if (property == "resultItem")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Result Item" of recipe "${recipe}" must not be empty!`
            );
          else if (
            property == "experience" &&
            recipes[recipe].type == "smelting"
          )
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Experience" of recipe "${recipe}" must not be empty!`
            );
          else if (
            property == "cookingTime" &&
            recipes[recipe].type == "smelting"
          )
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Cooking Time" of recipe "${recipe}" must not be empty!`
            );
        }
      });
    });
    Object.keys(biomes).forEach((biome) => {
      Object.keys(biomes[biome]).forEach((property) => {
        if (
          biomes[biome][property] == null ||
          String(biomes[biome][property]).trim() == ""
        ) {
          if (property == "json")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Biome JSON" of biome "${biome}" must not be empty!`
            );
          else if (property == "weight")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Weight" of biome "${biome}" must not be empty!`
            );
        }
      });
    });
    Object.keys(loottables).forEach((loottable) => {
      Object.keys(loottables[loottable]).forEach((property) => {
        if (
          loottables[loottable][property] == null ||
          String(loottables[loottable][property]).trim() == ""
        ) {
          if (property == "json")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Loot Table JSON" of loot table "${loottable}" must not be empty!`
            );
        }
      });
    });
    if (errors.length) return (running = false);
    const animationsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "animations"
    );
    const geosPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "geo"
    );
    const itemModelsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "item"
    );
    const blockModelsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "models",
      "block"
    );
    const itemTexturesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "item"
    );
    const mobTexturesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "entity"
    );
    const blockTexturesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
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
    const textureArmorModelsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase(),
      "textures",
      "models",
      "armor"
    );
    const recipesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "recipes"
    );
    const assetsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase()
    );
    const soundsPath = pathModule.join(assetsPath, "sounds");
    const biomesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "worldgen",
      "biome"
    );
    fs.rmdirSync(animationsPath, { force: true, recursive: true });
    fs.rmdirSync(geosPath, { force: true, recursive: true });
    fs.rmdirSync(itemModelsPath, { force: true, recursive: true });
    fs.rmdirSync(blockModelsPath, { force: true, recursive: true });
    fs.rmdirSync(itemTexturesPath, { force: true, recursive: true });
    fs.rmdirSync(mobTexturesPath, { force: true, recursive: true });
    fs.rmdirSync(blockTexturesPath, { force: true, recursive: true });
    fs.rmdirSync(blockstates, { force: true, recursive: true });
    fs.rmdirSync(projectMinecraftData, { force: true, recursive: true });
    fs.rmdirSync(minecraftData, { force: true, recursive: true });
    fs.rmdirSync(minecraftMineable, { force: true, recursive: true });
    fs.rmdirSync(forgeData, { force: true, recursive: true });
    fs.rmdirSync(biomeModifier, { force: true, recursive: true });
    fs.rmdirSync(worldgenConfigured, { force: true, recursive: true });
    fs.rmdirSync(worldgenPlaced, { force: true, recursive: true });
    fs.rmdirSync(recipesPath, { force: true, recursive: true });
    fs.rmdirSync(assetsPath, { force: true, recursive: true });
    fs.rmdirSync(soundsPath, { force: true, recursive: true });
    fs.rmdirSync(biomesPath, { force: true, recursive: true });
    fs.ensureDirSync(animationsPath);
    fs.ensureDirSync(geosPath);
    fs.ensureDirSync(itemModelsPath);
    fs.ensureDirSync(blockModelsPath);
    fs.ensureDirSync(itemTexturesPath);
    fs.ensureDirSync(mobTexturesPath);
    fs.ensureDirSync(blockTexturesPath);
    fs.ensureDirSync(blockstates);
    fs.ensureDirSync(projectMinecraftData);
    fs.ensureDirSync(minecraftData);
    fs.ensureDirSync(minecraftMineable);
    fs.ensureDirSync(forgeData);
    fs.ensureDirSync(biomeModifier);
    fs.ensureDirSync(worldgenConfigured);
    fs.ensureDirSync(worldgenPlaced);
    fs.ensureDirSync(textureArmorModelsPath);
    fs.ensureDirSync(recipesPath);
    fs.ensureDirSync(assetsPath);
    fs.ensureDirSync(soundsPath);
    fs.ensureDirSync(biomesPath);
    Object.keys(items).forEach((item) => {
      const modelPath = pathModule.join(itemModelsPath, `${item}.json`);
      if (items[item].modelType == "default") {
        const texture = items[item].texture?.data;
        if (texture) {
          const texturePath = pathModule.join(itemTexturesPath, `${item}.png`);
          const textureData = texture.replace("data:image/png;base64,", "");
          fs.writeFileSync(texturePath, textureData, "base64");
          fs.writeJSONSync(modelPath, {
            parent: "minecraft:item/generated",
            textures: {
              layer0: `${projectName.toLowerCase()}:item/${item}`,
            },
          });
        }
      } else if (items[item].modelType == "blockbench") {
        const model = items[item].model;
        const modelData = model?.data;
        const geo = items[item].geo;
        const geoPath = pathModule.join(geosPath, `${item}.geo.json`);
        const geoData = geo?.data;
        const animation = items[item].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${item}.animation.json`
        );
        const animationData = animation?.data;
        const texture = items[item].texture;
        const texturePath = pathModule.join(itemTexturesPath, `${item}.png`);
        const textureData = texture?.data;
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      }
    });
    Object.keys(mobs).forEach((mob) => {
      const modelPath = pathModule.join(itemModelsPath, `${mob}.json`);
      const geo = mobs[mob].geo;
      const geoPath = pathModule.join(geosPath, `${mob}.geo.json`);
      const geoData = geo?.data;
      const animation = mobs[mob].animation;
      const animationPath = pathModule.join(
        animationsPath,
        `${mob}.animation.json`
      );
      const animationData = animation?.data;
      const texture = mobs[mob].texture;
      const texturePath = pathModule.join(mobTexturesPath, `${mob}.png`);
      const textureData = texture?.data;
      if (texture) fs.writeFileSync(texturePath, textureData, "base64");
      if (geo) fs.writeFileSync(geoPath, geoData, "base64");
      if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      if (mobs[mob].modelType == "default") {
        fs.writeFileSync(texturePath, textureData, "base64");
        fs.writeJSONSync(modelPath, {
          parent: "minecraft:item/template_spawn_egg",
        });
      } else if (mobs[mob].modelType == "blockbench") {
        const model = items[item].model;
        const modelData = model?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
      }
    });
    Object.keys(armors).forEach((armor) => {
      const modelPath = pathModule.join(itemModelsPath, `${armor}.json`);
      const itemTexture = armors[armor].itemTexture;
      if (itemTexture) {
        const itemTexturePath = pathModule.join(
          itemTexturesPath,
          `${armor}_item.png`
        );
        const itemTextureData = itemTexture?.data;
        if (itemTextureData)
          fs.writeFileSync(itemTexturePath, itemTextureData, "base64");
        fs.writeJSONSync(modelPath, {
          parent: "minecraft:item/generated",
          textures: {
            layer0: `${projectName.toLowerCase()}:item/${armor}_item`,
          },
        });
      }
    });
    Object.keys(blocks).forEach((block) => {
      const itemModelPath = pathModule.join(itemModelsPath, `${block}.json`);
      const modelPath = pathModule.join(blockModelsPath, `${block}.json`);
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
          const particleTexturePath = pathModule.join(
            blockTexturesPath,
            `particle_${block}.png`
          );
          const particleTextureData = particleTexture.data;
          fs.writeFileSync(particleTexturePath, particleTextureData, "base64");
          modelObj.textures.particle = `${projectName.toLowerCase()}:block/particle_${block}`;
        }
        if (upTexture) {
          const upTexturePath = pathModule.join(
            blockTexturesPath,
            `up_${block}.png`
          );
          const upTextureData = upTexture.data;
          fs.writeFileSync(upTexturePath, upTextureData, "base64");
          modelObj.textures.up = `${projectName.toLowerCase()}:block/up_${block}`;
        }
        if (downTexture) {
          const downTexturePath = pathModule.join(
            blockTexturesPath,
            `down_${block}.png`
          );
          const downTextureData = downTexture.data;
          fs.writeFileSync(downTexturePath, downTextureData, "base64");
          modelObj.textures.down = `${projectName.toLowerCase()}:block/down_${block}`;
        }
        if (frontTexture) {
          const frontTexturePath = pathModule.join(
            blockTexturesPath,
            `front_${block}.png`
          );
          const frontTextureData = frontTexture.data;
          fs.writeFileSync(frontTexturePath, frontTextureData, "base64");
          modelObj.textures.north = `${projectName.toLowerCase()}:block/front_${block}`;
        }
        if (backTexture) {
          const backTexturePath = pathModule.join(
            blockTexturesPath,
            `back_${block}.png`
          );
          const backTextureData = backTexture.data;
          fs.writeFileSync(backTexturePath, backTextureData, "base64");
          modelObj.textures.south = `${projectName.toLowerCase()}:block/back_${block}`;
        }
        if (rightTexture) {
          const rightTexturePath = pathModule.join(
            blockTexturesPath,
            `right_${block}.png`
          );
          const rightTextureData = rightTexture.data;
          fs.writeFileSync(rightTexturePath, rightTextureData, "base64");
          modelObj.textures.east = `${projectName.toLowerCase()}:block/right_${block}`;
        }
        if (leftTexture) {
          const leftTexturePath = pathModule.join(
            blockTexturesPath,
            `left_${block}.png`
          );
          const leftTextureData = leftTexture.data;
          fs.writeFileSync(leftTexturePath, leftTextureData, "base64");
          modelObj.textures.west = `${projectName.toLowerCase()}:block/left_${block}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${block}`,
        });
      } else if (
        blocks[block].modelType == "default" &&
        blocks[block].type == "stairs"
      ) {
        const innerModelPath = pathModule.join(
          blockModelsPath,
          `${block}_inner.json`
        );
        const outerModelPath = pathModule.join(
          blockModelsPath,
          `${block}_outer.json`
        );
        const topTexture = blocks[block].upTexture;
        const bottomTexture = blocks[block].downTexture;
        const sideTexture = blocks[block].rightTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/stairs",
          textures: {},
        };
        if (topTexture) {
          const topTexturePath = pathModule.join(
            blockTexturesPath,
            `top_${block}.png`
          );
          const topTextureData = topTexture.data;
          fs.writeFileSync(topTexturePath, topTextureData, "base64");
          modelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
        }
        if (bottomTexture) {
          const bottomTexturePath = pathModule.join(
            blockTexturesPath,
            `bottom_${block}.png`
          );
          const bottomTextureData = bottomTexture.data;
          fs.writeFileSync(bottomTexturePath, bottomTextureData, "base64");
          modelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
        }
        if (sideTexture) {
          const sideTexturePath = pathModule.join(
            blockTexturesPath,
            `side_${block}.png`
          );
          const sideTextureData = sideTexture.data;
          fs.writeFileSync(sideTexturePath, sideTextureData, "base64");
          modelObj.textures.side = `${projectName.toLowerCase()}:block/side_${block}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(innerModelPath, {
          ...modelObj,
          parent: "minecraft:block/inner_stairs",
        });
        fs.writeJSONSync(outerModelPath, {
          ...modelObj,
          parent: "minecraft:block/outer_stairs",
        });
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${block}`,
        });
      } else if (
        blocks[block].modelType == "default" &&
        blocks[block].type == "slab"
      ) {
        const blockModelPath = pathModule.join(
          blockModelsPath,
          `${block}_block.json`
        );
        const topModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top.json`
        );
        const topTexture = blocks[block].upTexture;
        const bottomTexture = blocks[block].downTexture;
        const sideTexture = blocks[block].rightTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/slab",
          textures: {},
        };
        const blockModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/cube",
          textures: {},
        };
        if (topTexture) {
          const topTexturePath = pathModule.join(
            blockTexturesPath,
            `top_${block}.png`
          );
          const topTextureData = topTexture.data;
          fs.writeFileSync(topTexturePath, topTextureData, "base64");
          modelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          blockModelObj.textures.up = `${projectName.toLowerCase()}:block/top_${block}`;
        }
        if (bottomTexture) {
          const bottomTexturePath = pathModule.join(
            blockTexturesPath,
            `bottom_${block}.png`
          );
          const bottomTextureData = bottomTexture.data;
          fs.writeFileSync(bottomTexturePath, bottomTextureData, "base64");
          modelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          blockModelObj.textures.down = `${projectName.toLowerCase()}:block/bottom_${block}`;
        }
        if (sideTexture) {
          const sideTexturePath = pathModule.join(
            blockTexturesPath,
            `side_${block}.png`
          );
          const sideTextureData = sideTexture.data;
          fs.writeFileSync(sideTexturePath, sideTextureData, "base64");
          modelObj.textures.side = `${projectName.toLowerCase()}:block/side_${block}`;
          blockModelObj.textures.particle = `${projectName.toLowerCase()}:block/side_${block}`;
          blockModelObj.textures.north = `${projectName.toLowerCase()}:block/side_${block}`;
          blockModelObj.textures.south = `${projectName.toLowerCase()}:block/side_${block}`;
          blockModelObj.textures.east = `${projectName.toLowerCase()}:block/side_${block}`;
          blockModelObj.textures.west = `${projectName.toLowerCase()}:block/side_${block}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(blockModelPath, blockModelObj);
        fs.writeJSONSync(topModelPath, {
          ...modelObj,
          parent: "minecraft:block/slab_top",
        });
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${block}`,
        });
      } else if (
        blocks[block].modelType == "default" &&
        blocks[block].type == "door"
      ) {
        const bottomLeftOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_left_open.json`
        );
        const bottomRightModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_right.json`
        );
        const bottomRightOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_right_open.json`
        );
        const topLeftModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_left.json`
        );
        const topLeftOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_left_open.json`
        );
        const topRightModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_right.json`
        );
        const topRightOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_right_open.json`
        );
        const topTexture = blocks[block].upTexture;
        const bottomTexture = blocks[block].downTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_bottom_left",
          textures: {},
        };
        const bottomLeftOpenModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_bottom_left_open",
          textures: {},
        };
        const bottomRightModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_bottom_right",
          textures: {},
        };
        const bottomRightOpenModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_bottom_right_open",
          textures: {},
        };
        const topLeftModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_top_left",
          textures: {},
        };
        const topLeftOpenModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_top_left_open",
          textures: {},
        };
        const topRightModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_top_right",
          textures: {},
        };
        const topRightOpenModelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/door_top_right_open",
          textures: {},
        };
        if (topTexture) {
          const topTexturePath = pathModule.join(
            blockTexturesPath,
            `top_${block}.png`
          );
          const topTextureData = topTexture.data;
          fs.writeFileSync(topTexturePath, topTextureData, "base64");
          topLeftModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          topLeftOpenModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          topRightModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          topRightOpenModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          modelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          bottomLeftOpenModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          bottomRightModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
          bottomRightOpenModelObj.textures.top = `${projectName.toLowerCase()}:block/top_${block}`;
        }
        if (bottomTexture) {
          const bottomTexturePath = pathModule.join(
            blockTexturesPath,
            `bottom_${block}.png`
          );
          const bottomTextureData = bottomTexture.data;
          fs.writeFileSync(bottomTexturePath, bottomTextureData, "base64");
          modelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          bottomLeftOpenModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          bottomRightModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          bottomRightOpenModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          topLeftModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          topLeftOpenModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          topRightModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
          topRightOpenModelObj.textures.bottom = `${projectName.toLowerCase()}:block/bottom_${block}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(bottomLeftOpenModelPath, bottomLeftOpenModelObj);
        fs.writeJSONSync(bottomRightModelPath, bottomRightModelObj);
        fs.writeJSONSync(bottomRightOpenModelPath, bottomRightOpenModelObj);
        fs.writeJSONSync(topLeftModelPath, topLeftModelObj);
        fs.writeJSONSync(topLeftOpenModelPath, topLeftOpenModelObj);
        fs.writeJSONSync(topRightModelPath, topRightModelObj);
        fs.writeJSONSync(topRightOpenModelPath, topRightOpenModelObj);
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${block}`,
        });
      } else if (
        blocks[block].modelType == "blockbench" &&
        blocks[block].type == "stairs"
      ) {
        const innerModelPath = pathModule.join(
          blockModelsPath,
          `${block}_inner.json`
        );
        const outerModelPath = pathModule.join(
          blockModelsPath,
          `${block}_outer.json`
        );
        const model = blocks[block].model;
        const innerModel = blocks[block].innerModel;
        const outerModel = blocks[block].outerModel;
        const modelData = model?.data;
        const innerModelData = innerModel?.data;
        const outerModelData = outerModel?.data;
        const geo = blocks[block].geo;
        const geoPath = pathModule.join(geosPath, `${block}.geo.json`);
        const geoData = geo?.data;
        const animation = blocks[block].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${block}.animation.json`
        );
        const animationData = animation?.data;
        const texture = blocks[block].texture;
        const texturePath = pathModule.join(blockTexturesPath, `${block}.png`);
        const textureData = texture?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (innerModel)
          fs.writeFileSync(innerModelPath, innerModelData, "base64");
        if (outerModel)
          fs.writeFileSync(outerModelPath, outerModelData, "base64");
        if (model) fs.writeFileSync(itemModelPath, modelData, "base64");
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      } else if (
        blocks[block].modelType == "blockbench" &&
        blocks[block].type == "slab"
      ) {
        const blockModelPath = pathModule.join(
          blockModelsPath,
          `${block}_block.json`
        );
        const topModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top.json`
        );
        const model = blocks[block].model;
        const blockModel = blocks[block].fullModel;
        const topModel = blocks[block].topModel;
        const modelData = model?.data;
        const blockModelData = blockModel?.data;
        const topModelData = topModel?.data;
        const geo = blocks[block].geo;
        const geoPath = pathModule.join(geosPath, `${block}.geo.json`);
        const geoData = geo?.data;
        const animation = blocks[block].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${block}.animation.json`
        );
        const animationData = animation?.data;
        const texture = blocks[block].texture;
        const texturePath = pathModule.join(blockTexturesPath, `${block}.png`);
        const textureData = texture?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (blockModel)
          fs.writeFileSync(blockModelPath, blockModelData, "base64");
        if (topModel) fs.writeFileSync(topModelPath, topModelData, "base64");
        if (model) fs.writeFileSync(itemModelPath, modelData, "base64");
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      } else if (
        blocks[block].modelType == "blockbench" &&
        blocks[block].type == "door"
      ) {
        const bottomLeftOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_left_open.json`
        );
        const bottomRightModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_right.json`
        );
        const bottomRightOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_bottom_right_open.json`
        );
        const topLeftModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_left.json`
        );
        const topLeftOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_left_open.json`
        );
        const topRightModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_right.json`
        );
        const topRightOpenModelPath = pathModule.join(
          blockModelsPath,
          `${block}_top_right_open.json`
        );
        const model = blocks[block].model;
        const bottomLeftOpenModel = blocks[block].bottomLeftOpenModel;
        const bottomRightModel = blocks[block].bottomRightModel;
        const bottomRightOpenModel = blocks[block].bottomRightOpenModel;
        const topLeftModel = blocks[block].topLeftModel;
        const topLeftOpenModel = blocks[block].topLeftOpenModel;
        const topRightModel = blocks[block].topRightModel;
        const topRightOpenModel = blocks[block].topRightOpenModel;
        const modelData = model?.data;
        const bottomLeftOpenModelData = bottomLeftOpenModel?.data;
        const bottomRightModelData = bottomRightModel?.data;
        const bottomRightOpenModelData = bottomRightOpenModel?.data;
        const topLeftModelData = topLeftModel?.data;
        const topLeftOpenModelData = topLeftOpenModel?.data;
        const topRightModelData = topRightModel?.data;
        const topRightOpenModelData = topRightOpenModel?.data;
        const geo = blocks[block].geo;
        const geoPath = pathModule.join(geosPath, `${block}.geo.json`);
        const geoData = geo?.data;
        const animation = blocks[block].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${block}.animation.json`
        );
        const animationData = animation?.data;
        const texture = blocks[block].texture;
        const texturePath = pathModule.join(blockTexturesPath, `${block}.png`);
        const textureData = texture?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (bottomLeftOpenModel)
          fs.writeFileSync(
            bottomLeftOpenModelPath,
            bottomLeftOpenModelData,
            "base64"
          );
        if (bottomRightModel)
          fs.writeFileSync(
            bottomRightModelPath,
            bottomRightModelData,
            "base64"
          );
        if (bottomRightOpenModel)
          fs.writeFileSync(
            bottomRightOpenModel,
            bottomRightOpenModelData,
            "base64"
          );
        if (topLeftModel)
          fs.writeFileSync(topLeftModelPath, topLeftModelData, "base64");
        if (topLeftOpenModel)
          fs.writeFileSync(
            topLeftOpenModelPath,
            topLeftOpenModelData,
            "base64"
          );
        if (topRightModel)
          fs.writeFileSync(topRightModelPath, toptopRightModelData, "base64");
        if (topRightOpenModel)
          fs.writeFileSync(
            topRightOpenModelPath,
            topRightOpenModelData,
            "base64"
          );
        if (model) fs.writeFileSync(itemModelPath, modelData, "base64");
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      } else if (blocks[block].modelType == "blockbench") {
        const model = blocks[block].model;
        const modelData = model?.data;
        const geo = blocks[block].geo;
        const geoPath = pathModule.join(geosPath, `${block}.geo.json`);
        const geoData = geo?.data;
        const animation = blocks[block].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${block}.animation.json`
        );
        const animationData = animation?.data;
        const texture = blocks[block].texture;
        const texturePath = pathModule.join(blockTexturesPath, `${block}.png`);
        const textureData = texture?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (model) fs.writeFileSync(itemModelPath, modelData, "base64");
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      }
      const statePath = pathModule.join(blockstates, `${block}.json`);
      const mineablePath = pathModule.join(
        minecraftMineable,
        `${blocks[block].minedBy}.json`
      );
      let tierPath;
      if (["netherite", "gold"].includes(blocks[block].minedByTier))
        tierPath = pathModule.join(
          forgeData,
          `needs_${blocks[block].minedByTier}_tool.json`
        );
      else if (
        ["wood", "stone", "iron", "diamond"].includes(blocks[block].minedByTier)
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
              model: `${projectName.toLowerCase()}:block/${block}`,
            },
          },
        });
      } else if (blocks[block].type == "stairs") {
        fs.writeJSONSync(statePath, {
          variants: {
            "facing=east,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 270,
            },
            "facing=east,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
            },
            "facing=east,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 270,
            },
            "facing=east,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
            },
            "facing=east,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
            },
            "facing=east,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
            },
            "facing=east,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=east,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
            },
            "facing=east,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=east,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 180,
            },
            "facing=north,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 180,
            },
            "facing=north,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              y: 270,
            },
            "facing=north,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=north,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=north,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
            },
            "facing=north,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=south,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
            },
            "facing=south,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
            },
            "facing=south,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              y: 90,
            },
            "facing=south,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=south,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=south,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=south,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=south,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              x: 180,
              y: 90,
            },
            "facing=west,half=bottom,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 90,
            },
            "facing=west,half=bottom,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=bottom,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 90,
            },
            "facing=west,half=bottom,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=bottom,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              y: 180,
            },
            "facing=west,half=top,shape=inner_left": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=west,half=top,shape=inner_right": {
              model: `${projectName.toLowerCase()}:block/${block}_inner`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=west,half=top,shape=outer_left": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 180,
            },
            "facing=west,half=top,shape=outer_right": {
              model: `${projectName.toLowerCase()}:block/${block}_outer`,
              uvlock: true,
              x: 180,
              y: 270,
            },
            "facing=west,half=top,shape=straight": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              uvlock: true,
              x: 180,
              y: 180,
            },
          },
        });
      } else if (blocks[block].type == "slab") {
        fs.writeJSONSync(statePath, {
          variants: {
            "type=bottom": {
              model: `${projectName.toLowerCase()}:block/${block}`,
            },
            "type=double": {
              model: `${projectName.toLowerCase()}:block/${block}_block`,
            },
            "type=top": {
              model: `${projectName.toLowerCase()}:block/${block}_top`,
            },
          },
        });
      } else if (blocks[block].type == "door") {
        fs.writeJSONSync(statePath, {
          variants: {
            "facing=east,half=lower,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}`,
            },
            "facing=east,half=lower,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_left_open`,
              y: 90,
            },
            "facing=east,half=lower,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right`,
            },
            "facing=east,half=lower,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right_open`,
              y: 270,
            },
            "facing=east,half=upper,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left`,
            },
            "facing=east,half=upper,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left_open`,
              y: 90,
            },
            "facing=east,half=upper,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right`,
            },
            "facing=east,half=upper,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right_open`,
              y: 270,
            },
            "facing=north,half=lower,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              y: 270,
            },
            "facing=north,half=lower,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_left_open`,
            },
            "facing=north,half=lower,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right`,
              y: 270,
            },
            "facing=north,half=lower,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right_open`,
              y: 180,
            },
            "facing=north,half=upper,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left`,
              y: 270,
            },
            "facing=north,half=upper,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left_open`,
            },
            "facing=north,half=upper,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right`,
              y: 270,
            },
            "facing=north,half=upper,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right_open`,
              y: 180,
            },
            "facing=south,half=lower,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              y: 90,
            },
            "facing=south,half=lower,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_left_open`,
              y: 180,
            },
            "facing=south,half=lower,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right`,
              y: 90,
            },
            "facing=south,half=lower,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right_open`,
            },
            "facing=south,half=upper,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left`,
              y: 90,
            },
            "facing=south,half=upper,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left_open`,
              y: 180,
            },
            "facing=south,half=upper,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right`,
              y: 90,
            },
            "facing=south,half=upper,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right_open`,
            },
            "facing=west,half=lower,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}`,
              y: 180,
            },
            "facing=west,half=lower,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_left_open`,
              y: 270,
            },
            "facing=west,half=lower,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right`,
              y: 180,
            },
            "facing=west,half=lower,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_bottom_right_open`,
              y: 90,
            },
            "facing=west,half=upper,hinge=left,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left`,
              y: 180,
            },
            "facing=west,half=upper,hinge=left,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_left_open`,
              y: 270,
            },
            "facing=west,half=upper,hinge=right,open=false": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right`,
              y: 180,
            },
            "facing=west,half=upper,hinge=right,open=true": {
              model: `${projectName.toLowerCase()}:block/${block}_top_right_open`,
              y: 90,
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
          mineable.values.push(`${projectName.toLowerCase()}:${block}`);
          tier.values.push(`${projectName.toLowerCase()}:${block}`);
          fs.writeJSONSync(mineablePath, mineable);
          fs.writeJSONSync(tierPath, tier);
        } else if (blocks[block].minedBy != "anything") {
          fs.writeJSONSync(mineablePath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${block}`],
          });
          fs.writeJSONSync(tierPath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${block}`],
          });
        }
      }
      if (blocks[block].isOre && blocks[block].biomes.length) {
        const configurePath = pathModule.join(
          worldgenConfigured,
          `${block}.json`
        );
        const placedPath = pathModule.join(worldgenPlaced, `${block}.json`);
        const biomePath = pathModule.join(biomeModifier, `${block}.json`);
        const targets = [];
        const selectedBiomes = blocks[block].biomes.map((b) => {
          if (biomes[b])
            return {
              name: `${projectName.toLowerCase()}:${b}`,
              dimension: biomes[b].type,
            };
          else return defaultBiomes.find((biome) => biome.name == b);
        });
        if (selectedBiomes.find((b) => b.dimension == "overworld"))
          targets.push({
            target: {
              predicate_type: `minecraft:tag_match`,
              tag: `minecraft:stone_ore_replaceables`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${block}`,
            },
          });
        if (selectedBiomes.find((b) => b.dimension == "nether"))
          targets.push({
            target: {
              predicate_type: `minecraft:block_match`,
              block: `minecraft:netherrack`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${block}`,
            },
          });
        if (selectedBiomes.find((b) => b.dimension == "end"))
          targets.push({
            target: {
              predicate_type: `minecraft:block_match`,
              block: `minecraft:end_stone`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${block}`,
            },
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
          feature: `${projectName.toLowerCase()}:${block}`,
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
          features: `${projectName.toLowerCase()}:${block}`,
          step: "underground_ores",
          biomes: selectedBiomes.map((biome) => biome.name),
        });
      }
    });
    Object.keys(trees).forEach((tree) => {
      const itemModelPath = pathModule.join(itemModelsPath, `${tree}.json`);
      const modelPath = pathModule.join(blockModelsPath, `${tree}.json`);
      if (trees[tree].modelType == "default") {
        const crossTexture = trees[tree].crossTexture;
        const modelObj = {
          render_type: "minecraft:cutout",
          parent: "minecraft:block/cross",
          textures: {},
        };
        if (crossTexture) {
          const crossTexturePath = pathModule.join(
            blockTexturesPath,
            `${tree}.png`
          );
          const crossTextureData = crossTexture.data;
          fs.writeFileSync(crossTexturePath, crossTextureData, "base64");
          modelObj.textures.cross = `${projectName.toLowerCase()}:block/${tree}`;
        }
        fs.writeJSONSync(modelPath, modelObj);
        fs.writeJSONSync(itemModelPath, {
          parent: `${projectName.toLowerCase()}:block/${tree}`,
        });
      } else if (trees[tree].modelType == "blockbench") {
        const model = trees[tree].model;
        const modelData = model?.data;
        const geo = trees[tree].geo;
        const geoPath = pathModule.join(geosPath, `${tree}.geo.json`);
        const geoData = geo?.data;
        const animation = trees[tree].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${tree}.animation.json`
        );
        const animationData = animation?.data;
        const texture = trees[tree].texture;
        const texturePath = pathModule.join(blockTexturesPath, `${tree}.png`);
        const textureData = texture?.data;
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (model) fs.writeFileSync(itemModelPath, modelData, "base64");
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      }
      const statePath = pathModule.join(blockstates, `${tree}.json`);
      const mineablePath = pathModule.join(
        minecraftMineable,
        `${trees[tree].minedBy}.json`
      );
      let tierPath;
      if (["netherite", "gold"].includes(trees[tree].minedByTier))
        tierPath = pathModule.join(
          forgeData,
          `needs_${trees[tree].minedByTier}_tool.json`
        );
      else if (
        ["wood", "stone", "iron", "diamond"].includes(trees[tree].minedByTier)
      )
        tierPath = pathModule.join(
          minecraftData,
          `needs_${trees[tree].minedByTier}_tool.json`
        );
      else
        tierPath = pathModule.join(
          projectMinecraftData,
          `needs_${trees[tree].minedByTier}_tool.json`
        );
      fs.writeJSONSync(statePath, {
        variants: {
          "": {
            model: `${projectName.toLowerCase()}:block/${tree}`,
          },
        },
      });
      const configurePath = pathModule.join(worldgenConfigured, `${tree}.json`);
      fs.writeJSONSync(configurePath, {
        type: "minecraft:tree",
        config: {
          minimum_size: {
            type: "minecraft:two_layers_feature_size",
          },
          dirt_provider: {
            type: "minecraft:simple_state_provider",
            state: {
              Name: `minecraft:dirt`,
            },
          },
          trunk_provider: {
            type: "minecraft:simple_state_provider",
            state: {
              Name: trees[tree].bodyBlock.startsWith("minecraft:")
                ? trees[tree].bodyBlock
                : `${projectName.toLowerCase()}:${trees[tree].bodyBlock}`,
            },
          },
          foliage_provider: {
            type: "minecraft:simple_state_provider",
            state: {
              Name: trees[tree].leavesBlock.startsWith("minecraft:")
                ? trees[tree].leavesBlock
                : `${projectName.toLowerCase()}:${trees[tree].leavesBlock}`,
            },
          },
          trunk_placer: {
            type: "minecraft:straight_trunk_placer",
            base_height: trees[tree].bodyHeight,
            height_rand_a: trees[tree].bodyRandA,
            height_rand_b: trees[tree].bodyRandB,
          },
          foliage_placer: {
            type: "minecraft:blob_foliage_placer",
            radius: trees[tree].radius,
            offset: trees[tree].offset,
            height: trees[tree].leavesHeight,
          },
          decorators: [],
        },
      });
      if (trees[tree].dropItem) {
        if (fs.existsSync(mineablePath) && trees[tree].minedBy != "anything") {
          const mineable = fs.readJSONSync(mineablePath);
          const tier = fs.readJSONSync(tierPath);
          mineable.values.push(`${projectName.toLowerCase()}:${tree}`);
          tier.values.push(`${projectName.toLowerCase()}:${tree}`);
          fs.writeJSONSync(mineablePath, mineable);
          fs.writeJSONSync(tierPath, tier);
        } else if (trees[tree].minedBy != "anything") {
          fs.writeJSONSync(mineablePath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${tree}`],
          });
          fs.writeJSONSync(tierPath, {
            replace: false,
            values: [`${projectName.toLowerCase()}:${tree}`],
          });
        }
      }
      if (trees[tree].biomes.length) {
        const placedPath = pathModule.join(worldgenPlaced, `${tree}.json`);
        const biomePath = pathModule.join(biomeModifier, `${tree}.json`);
        const targets = [];
        const selectedBiomes = trees[tree].biomes.map((b) => {
          if (biomes[b])
            return {
              name: `${projectName.toLowerCase()}:${b}`,
              dimension: biomes[b].type,
            };
          else return defaultBiomes.find((biome) => biome.name == b);
        });
        if (selectedBiomes.find((b) => b.dimension == "overworld"))
          targets.push({
            target: {
              predicate_type: `minecraft:tag_match`,
              tag: `minecraft:stone_ore_replaceables`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${tree}`,
            },
          });
        if (selectedBiomes.find((b) => b.dimension == "nether"))
          targets.push({
            target: {
              predicate_type: `minecraft:block_match`,
              tree: `minecraft:netherrack`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${tree}`,
            },
          });
        if (selectedBiomes.find((b) => b.dimension == "end"))
          targets.push({
            target: {
              predicate_type: `minecraft:block_match`,
              tree: `minecraft:end_stone`,
            },
            state: {
              Name: `${projectName.toLowerCase()}:${tree}`,
            },
          });
        fs.writeJSONSync(placedPath, {
          feature: `${projectName.toLowerCase()}:${tree}`,
          placement: [
            {
              type: "minecraft:count",
              count: trees[tree].amount,
            },
            {
              type: "minecraft:in_square",
            },
            {
              type: "minecraft:surface_water_depth_filter",
              max_water_depth: 0,
            },
            {
              type: "minecraft:heightmap",
              heightmap: "OCEAN_FLOOR",
            },
            {
              type: "minecraft:biome",
            },
            {
              type: "minecraft:block_predicate_filter",
              predicate: {
                type: "minecraft:would_survive",
                state: {
                  Name: `${projectName.toLowerCase()}:${tree}`,
                  Properties: {
                    stage: "0",
                  },
                },
              },
            },
          ],
        });
        fs.writeJSONSync(biomePath, {
          type: "forge:add_features",
          features: `${projectName.toLowerCase()}:${tree}`,
          step: "vegetal_decoration",
          biomes: selectedBiomes.map((biome) => biome.name),
        });
      }
    });
    Object.keys(tools).forEach((tool) => {
      const modelPath = pathModule.join(itemModelsPath, `${tool}.json`);
      if (tools[tool].modelType == "default") {
        const texture = tools[tool].texture;
        if (texture) {
          const texturePath = pathModule.join(itemTexturesPath, `${tool}.png`);
          const textureData = texture.data;
          fs.writeFileSync(texturePath, textureData, "base64");
          fs.writeJSONSync(modelPath, {
            parent: "minecraft:item/generated",
            textures: {
              layer0: `${projectName.toLowerCase()}:item/${tool}`,
            },
          });
        }
      } else if (tools[tool].modelType == "blockbench") {
        const model = tools[tool].model;
        const modelData = model?.data;
        const geo = tools[tool].geo;
        const geoPath = pathModule.join(geosPath, `${tool}.geo.json`);
        const geoData = geo?.data;
        const animation = tools[tool].animation;
        const animationPath = pathModule.join(
          toolAnimations,
          `${tool}.animation.json`
        );
        const animationData = animation?.data;
        const texture = tools[tool].texture;
        const texturePath = pathModule.join(itemTexturesPath, `${tool}.png`);
        const textureData = texture?.data;
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (model) fs.writeFileSync(modelPath, modelData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      }
    });
    Object.keys(materials).forEach((material) => {
      const texture1 = materials[material].texture1;
      const texture2 = materials[material].texture2;
      if (materials[material].modelType == "default") {
        if (texture1) {
          const texturePath = pathModule.join(
            textureArmorModelsPath,
            `${material}_layer_1.png`
          );
          const textureData = texture1.data;
          fs.writeFileSync(texturePath, textureData, "base64");
        }
        if (texture2) {
          const texturePath = pathModule.join(
            textureArmorModelsPath,
            `${material}_layer_2.png`
          );
          const textureData = texture2.data;
          fs.writeFileSync(texturePath, textureData, "base64");
        }
      } else if (materials[material].modelType == "blockbench") {
        const geo = materials[material].geo;
        const geoPath = pathModule.join(geosPath, `${material}.geo.json`);
        const geoData = geo?.data;
        const animation = materials[material].animation;
        const animationPath = pathModule.join(
          animationsPath,
          `${material}.animation.json`
        );
        const animationData = animation?.data;
        const texture = materials[material].texture;
        const texturePath = pathModule.join(
          itemTexturesPath,
          `${material}.png`
        );
        const textureData = texture?.data;
        if (texture) fs.writeFileSync(texturePath, textureData, "base64");
        if (geo) fs.writeFileSync(geoPath, geoData, "base64");
        if (animation) fs.writeFileSync(animationPath, animationData, "base64");
      }
    });
    Object.keys(recipes).forEach((recipe) => {
      const recipePath = pathModule.join(recipesPath, `${recipe}.json`);
      const firstItem = recipes[recipe].firstItem == "none" ? " " : "1";
      const secondItem = recipes[recipe].secondItem == "none" ? " " : "2";
      const thirdItem = recipes[recipe].thirdItem == "none" ? " " : "3";
      const fourthItem = recipes[recipe].fourthItem == "none" ? " " : "4";
      const fifthItem = recipes[recipe].fifthItem == "none" ? " " : "5";
      const sixthItem = recipes[recipe].sixthItem == "none" ? " " : "6";
      const seventhItem = recipes[recipe].seventhItem == "none" ? " " : "7";
      const eighthItem = recipes[recipe].eighthItem == "none" ? " " : "8";
      const ninethItem = recipes[recipe].ninethItem == "none" ? " " : "9";
      if (recipes[recipe].type == "shaped") {
        const key = {};
        if (firstItem.trim()) {
          key["1"] = {
            item: `${
              recipes[recipe].firstItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].firstItem}`,
          };
        }
        if (secondItem.trim()) {
          key["2"] = {
            item: `${
              recipes[recipe].secondItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].secondItem}`,
          };
        }
        if (thirdItem.trim()) {
          key["3"] = {
            item: `${
              recipes[recipe].thirdItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].thirdItem}`,
          };
        }
        if (fourthItem.trim()) {
          key["4"] = {
            item: `${
              recipes[recipe].fourthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].fourthItem}`,
          };
        }
        if (fifthItem.trim()) {
          key["5"] = {
            item: `${
              recipes[recipe].fifthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].fifthItem}`,
          };
        }
        if (sixthItem.trim()) {
          key["6"] = {
            item: `${
              recipes[recipe].sixthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].sixthItem}`,
          };
        }
        if (seventhItem.trim()) {
          key["7"] = {
            item: `${
              recipes[recipe].seventhItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].seventhItem}`,
          };
        }
        if (eighthItem.trim()) {
          key["8"] = {
            item: `${
              recipes[recipe].eighthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].eighthItem}`,
          };
        }
        if (ninethItem.trim()) {
          key["9"] = {
            item: `${
              recipes[recipe].ninethItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].ninethItem}`,
          };
        }
        fs.writeJSONSync(recipePath, {
          type: "minecraft:crafting_shaped",
          pattern: [
            `${firstItem}${secondItem}${thirdItem}`,
            `${fourthItem}${fifthItem}${sixthItem}`,
            `${seventhItem}${eighthItem}${ninethItem}`,
          ],
          key,
          result: {
            item: `${
              recipes[recipe].resultItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].resultItem}`,
            count: recipes[recipe].resultCount,
          },
        });
      } else if (recipes[recipe].type == "shapeless") {
        const ingredients = [];
        if (firstItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].firstItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].firstItem}`,
          });
        }
        if (secondItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].secondItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].secondItem}`,
          });
        }
        if (thirdItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].thirdItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].thirdItem}`,
          });
        }
        if (fourthItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].fourthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].fourthItem}`,
          });
        }
        if (fifthItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].fifthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].fifthItem}`,
          });
        }
        if (sixthItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].sixthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].sixthItem}`,
          });
        }
        if (seventhItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].seventhItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].seventhItem}`,
          });
        }
        if (eighthItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].eighthItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].eighthItem}`,
          });
        }
        if (ninethItem.trim()) {
          ingredients.push({
            item: `${
              recipes[recipe].ninethItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].ninethItem}`,
          });
        }
        fs.writeJSONSync(recipePath, {
          type: "minecraft:crafting_shapeless",
          ingredients,
          result: {
            item: `${
              recipes[recipe].resultItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].resultItem}`,
            count: recipes[recipe].resultCount,
          },
        });
      } else if (recipes[recipe].type == "smelting") {
        fs.writeJSONSync(recipePath, {
          type: "minecraft:smelting",
          ingredient: {
            item: `${
              recipes[recipe].firstItem.startsWith("minecraft:")
                ? ""
                : projectName.toLowerCase() + ":"
            }${recipes[recipe].firstItem}`,
          },
          result: `${
            recipes[recipe].resultItem.startsWith("minecraft:")
              ? ""
              : projectName.toLowerCase() + ":"
          }${recipes[recipe].resultItem}`,
          experience: recipes[recipe].experience,
          cookingtime: recipes[recipe].cookingTime * 20,
        });
      }
    });
    const soundsObj = {};
    Object.keys(sounds).forEach((sound) => {
      soundsObj[sound] = {
        sounds: [`${projectName.toLowerCase()}:${sound}`],
      };
      const Sound = sounds[sound].sound?.data;
      const soundPath = pathModule.join(soundsPath, `${sound}.ogg`);
      const soundData = Sound.replace("data:audio/ogg;base64,", "");
      if (Sound) fs.writeFileSync(soundPath, soundData, "base64");
    });
    fs.writeJSONSync(pathModule.join(assetsPath, "sounds.json"), soundsObj);
    Object.keys(loottables).forEach((loottable) => {
      if (
        parse(loottables[loottable].json).type == "minecraft:block" &&
        blocks[loottables[loottable].for].dropItem
      ) {
        const blockloottablePath = pathModule.join(
          blockloottables,
          `${loottables[loottable].for}.json`
        );
        fs.writeFileSync(blockloottablePath, loottables[loottable].json);
      }
    });
    Object.keys(biomes).forEach((biome) => {
      const biomePath = pathModule.join(biomesPath, `${biome}.json`);
      fs.writeFileSync(biomePath, biomes[biome].json);
    });
    const basePath = isDev
      ? pathModule.join(
          __dirname,
          "..",
          "..",
          "..",
          "..",
          "..",
          "..",
          "Project"
        )
      : pathModule.join("./", "Project");
    const pluginPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "plugins",
      "mod"
    );
    const eventsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "project",
      "init",
      "EventInit.java"
    );
    const nodePath = pathModule.join(
      projectPath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "project",
      "util",
      "Node.java"
    );
    const baseEventsPath = pathModule.join(
      basePath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "project",
      "init",
      "EventInit.java"
    );
    const baseNodePath = pathModule.join(
      basePath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "project",
      "util",
      "Node.java"
    );
    const plugins = fs.readdirSync(pluginPath);
    const events = fs.readFileSync(baseEventsPath).toString();
    const node = fs.readFileSync(baseNodePath).toString();
    const ifs = plugins.map((plugin, i) => {
      const pluginName = plugin.replace(".java", "");
      if (i == 0)
        return `if(plugin.equals("${pluginName}")) dev.openmod.plugins.mod.${pluginName}.execute(bus, n, item);`;
      else
        return `else if(plugin.equals("${pluginName}")) dev.openmod.plugins.mod.${pluginName}.execute(bus, n, item);`;
    });
    fs.writeFileSync(
      eventsPath,
      events.replaceAll("//${plugins}", ifs.join(`\n                    `))
    );
    fs.writeFileSync(
      nodePath,
      node.replaceAll(
        "//${plugins}",
        ifs.join("\n                                ")
      )
    );
    ipc.invoke("run");
  }
  function formatDateToHHMMSS(date) {
    const hours = date.getHours();
    const minutes = date.getMinutes();
    const seconds = date.getSeconds();
    const formattedHours = hours.toString().padStart(2, "0");
    const formattedMinutes = minutes.toString().padStart(2, "0");
    const formattedSeconds = seconds.toString().padStart(2, "0");
    return `${formattedHours}:${formattedMinutes}:${formattedSeconds}`;
  }
  function addError(err) {
    errors.push(err);
    errors = errors;
    ipc.invoke("addError", err);
  }
</script>

<svelte:head>
  <title>OpenMod - Run</title>
</svelte:head>

<div class="flex flex-col w-full p-12 gap-3 overflow-auto">
  <div class="join">
    <a class="tooltip tooltip-top" data-tip="Run">
      <button
        class="join-item btn {running
          ? 'btn-warning'
          : 'btn-neutral'} text-lg w-fit"
        on:click={run.bind(this, true)}><i class="fa-solid fa-play" /></button
      >
    </a>
    <a class="tooltip tooltip-top" data-tip="Stop">
      <button
        class="join-item btn {!running
          ? 'btn-warning'
          : 'btn-neutral'} text-lg w-fit"
        on:click={run.bind(this, false)}><i class="fa-solid fa-stop" /></button
      ></a
    >
  </div>
  <div class="flex flex-col w-full h-full pb-12 overflow-auto">
    <div class="tabs bg-neutral w-fit rounded-lg rounded-b-none">
      <a
        class="tab tab-bordered {tab == 0 ? 'tab-active' : ''}"
        on:click={setTab.bind(this, 0)}>Build Output</a
      >
      <a
        class="tab tab-bordered {tab == 1 ? 'tab-active' : ''}"
        on:click={setTab.bind(this, 1)}
        >Build Errors
        <div
          class="badge badge-error text-white w-3 ml-1 {errors.length
            ? ''
            : 'hidden'}"
        >
          {errors.length}
        </div></a
      >
    </div>
    <pre
      class="bg-neutral w-full h-full rounded-lg rounded-tl-none rounded-tr-lg px-4 py-2 font-[Cascadia] overflow-y-auto whitespace-pre-wrap break-words">{tab ==
      0
        ? output
        : errors.join("\n")}</pre>
  </div>
</div>
