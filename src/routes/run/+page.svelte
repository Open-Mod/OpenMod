<script>
  import { onMount } from "svelte";

  let running = false;
  let errors = [];
  let output = "";
  let tab = 0;
  let projectPath = "";
  let path = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "gradlew.bat");
    running = ipc.sendSync("isRunning");
    errors = ipc.sendSync("lastError");
    output = ipc.sendSync("lastLog");
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
  function run(t) {
    if (running == t) return;
    running = t;
    if (running == false) return ipc.invoke("stop");
    errors = [];
    output = "";
    ipc.invoke("clearLogs");
    const itemsFile = pathModule.join(projectPath, "src", "data", "items.json");
    const items = fs.existsSync(itemsFile) ? fs.readJSONSync(itemsFile) : {};
    const blocksFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "blocks.json"
    );
    const blocks = fs.existsSync(blocksFile) ? fs.readJSONSync(blocksFile) : {};
    const toolsFile = pathModule.join(projectPath, "src", "data", "tools.json");
    const tools = fs.existsSync(toolsFile) ? fs.readJSONSync(toolsFile) : {};
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
    const loottablesFile = pathModule.join(
      projectPath,
      "src",
      "data",
      "loottables.json"
    );
    const loottables = fs.existsSync(loottablesFile)
      ? fs.readJSONSync(loottablesFile)
      : {};
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
                )}]: Field "Duration (In Seconds)" of effect "${
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
              )}]: Field "Discard On Air Exposure (%)" of block "${block}" must not be empty!`
            );
          else if (property == "worlds" && blocks[block].isOre)
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Worlds to generate in" of block "${block}" must not be empty!`
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
          if (property == "resultCount")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Result Item Count" of recipe "${recipe}" must not be empty!`
            );
          else if (property == "resultItem")
            addError(
              `[${formatDateToHHMMSS(
                new Date()
              )}]: Field "Result Item" of recipe "${recipe}" must not be empty!`
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
    const basePath = isDev
      ? pathModule.join(
          __dirname,
          "..",
          "..",
          "..",
          "..",
          "..",
          "..",
          "Project",
          "Project"
        )
      : pathModule.join(
          __dirname,
          "..",
          "..",
          "..",
          "..",
          "..",
          "..",
          "..",
          "..",
          "Project",
          "Project"
        );
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
