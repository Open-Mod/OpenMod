<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let materials = {};
  let tools = {};
  let items = {};
  let blocks = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let toolsPath = "";
  let itemsPath = "";
  let blocksPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "materials.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    materials = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(materials).forEach((tier) => {
      materials[tier].name = tier;
      materials[tier].repairIngredient = materials[tier].repairIngredient.trim()
        ? materials[tier].repairIngredient
        : Object.keys(tools)[0] ??
          Object.keys(items)[0] ??
          Object.keys(blocks)[0] ??
          materials[tier].repairIngredient;
    });
    selectedMaterial = Object.keys(materials)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "materials.json") return;
      materials = data.file.content;
    };
  });
  let selectedMaterial = "";
  let name = "";
  let error = "";
  let success = "";
  function add() {
    name = `new_tier_${Object.keys(materials).length + 1}`;
    materials[name] = {
      name,
      durabilityForHelmet: 1,
      durabilityForBoots: 1,
      durabilityForChestplate: 1,
      durabilityForLeggings: 1,
      protectionForHelmet: 1,
      protectionForBoots: 1,
      protectionForChestplate: 1,
      protectionForLeggings: 1,
      enchantmentValue: 15,
      toughness: 100,
      knockbackResistance: 0,
      repairIngredient:
        Object.keys(tools)[0] ??
        Object.keys(items)[0] ??
        Object.keys(blocks)[0] ??
        "",
    };
    selectedMaterial = name;
    send_changes({ file: "materials.json", content: materials });
  }
  function save() {
    const obj = {};
    Object.keys(materials).forEach((tier) => {
      const name = materials[tier].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(materials[tier]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = materials[tier][property];
      });
    });
    fs.writeJSONSync(path, obj);
    materials = obj;
    Object.keys(materials).forEach((tier) => {
      materials[tier].name = tier;
    });
    selectedMaterial = Object.keys(materials)[0];
    success = "Materials saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteMaterial() {
    if (!selectedMaterial) return;
    delete materials[selectedMaterial];
    materials = materials;
    selectedMaterial = Object.keys(materials)[0];
    updateEditor();
    send_changes({ file: "materials.json", content: materials });
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
  <title>OpenMod - Materials</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Material:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedMaterial}
    >
      {#if !Object.keys(materials).length}
        <option disabled value={selectedMaterial}>No materials</option>
      {/if}
      {#each Object.keys(materials) as tier}
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
        <button class="btn btn-error" on:click={deleteMaterial}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if materials[selectedMaterial]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={materials[selectedMaterial].name}
            />
          </div>
          <div>
            <label class="text-lg">Durability For Helmet</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].durabilityForHelmet}
            />
          </div>
          <div>
            <label class="text-lg">Durability For Boots</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].durabilityForBoots}
            />
          </div>
          <div>
            <label class="text-lg">Durability For Chestplate</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].durabilityForChestplate}
            />
          </div>
          <div>
            <label class="text-lg">Durability For Leggings</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].durabilityForLeggings}
            />
          </div>
          <div>
            <label class="text-lg">Protection For Helmet</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].protectionForHelmet}
            />
          </div>
          <div>
            <label class="text-lg">Protection For Boots</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].protectionForBoots}
            />
          </div>
          <div>
            <label class="text-lg">Protection For Chestplate</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].protectionForChestplate}
            />
          </div>
          <div>
            <label class="text-lg">Protection For Leggings</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].protectionForLeggings}
            />
          </div>
          <div>
            <label class="text-lg">Enchantability</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={materials[selectedMaterial].enchantmentValue}
            />
          </div>
          <div>
            <label class="text-lg">Toughness (%)</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={materials[selectedMaterial].toughness}
            />
          </div>
          <div>
            <label class="text-lg">Knockback Resistance (%)</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].knockbackResistance}
            />
          </div>
          <div>
            <label class="text-lg">Repair With Item</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={materials[selectedMaterial].repairIngredient}
            >
              {#if ![...Object.keys(tools), ...Object.keys(items), ...Object.keys(blocks), ...defaultItems].length}
                <option
                  disabled
                  value={materials[selectedMaterial].repairIngredient}
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
