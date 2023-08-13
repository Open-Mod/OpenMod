<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let tiers = {};
  let tools = {};
  let items = {};
  let blocks = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let toolsPath = "";
  let itemsPath = "";
  let blocksPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "tiers.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    tiers = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(tiers).forEach((tier) => {
      tiers[tier].name = tier;
      tiers[tier].repairIngredient = tiers[tier].repairIngredient.trim()
        ? tiers[tier].repairIngredient
        : Object.keys(tools)[0] ??
          Object.keys(items)[0] ??
          Object.keys(blocks)[0] ??
          tiers[tier].repairIngredient;
    });
    selectedTier = Object.keys(tiers)[0] ?? "";
  });
  let selectedTier = "";
  let name = "";
  let error = "";
  let success = "";
  function add() {
    name = `new_tier_${Object.keys(tiers).length + 1}`;
    tiers[name] = {
      name,
      level: 0,
      uses: 1,
      enchantmentValue: 15,
      attackSpeed: 100,
      attackDamageBonus: 0,
      repairIngredient:
        Object.keys(tools)[0] ??
        Object.keys(items)[0] ??
        Object.keys(blocks)[0] ??
        "",
    };
    selectedTier = name;
  }
  function save() {
    const obj = {};
    Object.keys(tiers).forEach((tier) => {
      const name = tiers[tier].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(tiers[tier]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = tiers[tier][property];
      });
    });
    fs.writeJSONSync(path, obj);
    tiers = obj;
    Object.keys(tiers).forEach((tier) => {
      tiers[tier].name = tier;
    });
    selectedTier = Object.keys(tiers)[0];
    success = "Tiers saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteTier() {
    if (!selectedTier) return;
    delete tiers[selectedTier];
    tiers = tiers;
    selectedTier = Object.keys(tiers)[0];
    updateEditor();
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
  <title>OpenMod - Tiers</title>
</svelte:head>
<div class="flex flex-col w-full p-12 gap-3">
  <h1 class="text-3xl font-bold">Selected Tier:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedTier}
    >
      {#if !Object.keys(tiers).length}
        <option disabled value={selectedTier}>No tiers</option>
      {/if}
      {#each Object.keys(tiers) as tier}
        <option value={tier}>{tier}</option>
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
        <button class="btn btn-error" on:click={deleteTier}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto">
    {#if tiers[selectedTier]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={tiers[selectedTier].name}
            />
          </div>
          <div>
            <label class="text-lg">Level</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={tiers[selectedTier].level}
            />
          </div>
          <div>
            <label class="text-lg">Uses</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={tiers[selectedTier].uses}
            />
          </div>
          <div>
            <label class="text-lg">Enchantability</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={tiers[selectedTier].enchantmentValue}
            />
          </div>
          <div>
            <label class="text-lg">Attack Speed (%)</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={tiers[selectedTier].attackSpeed}
            />
          </div>
          <div>
            <label class="text-lg">Attack Damage Bonus (%)</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={tiers[selectedTier].attackDamageBonus}
            />
          </div>
          <div>
            <label class="text-lg">Repair With Item</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tiers[selectedTier].repairIngredient}
            >
              {#if ![...Object.keys(tools), ...Object.keys(items), ...Object.keys(blocks), ...defaultItems].length}
                <option disabled value={tiers[selectedTier].repairIngredient}
                  >No items</option
                >
              {/if}
              {#each Object.keys(tools) as tool}
                <option value={tool}>{convertToCamelCase(tool)}</option>
              {/each}
              {#each Object.keys(items) as item}
                <option value={item}>{convertToCamelCase(item)}</option>
              {/each}
              {#each Object.keys(blocks) as block}
                <option value={block}>{convertToCamelCase(block)}</option>
              {/each}
              {#each defaultItems as item}
                <option value={item}>{item}</option>
              {/each}
            </select>
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
<Error {error} />
<Success {success} />
