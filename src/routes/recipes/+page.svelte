<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let recipes = {};
  let items = {};
  let armors = {};
  let tools = {};
  let blocks = {};
  let trees = {};
  let mobs = {};
  let potions = {};
  let defaultItems = [];
  let defaultPotions = [];
  let projectPath = "";
  let path = "";
  let projectName = "";
  let itemsPath = "";
  let armorsPath = "";
  let toolsPath = "";
  let blocksPath = "";
  let treesPath = "";
  let mobsPath = "";
  let potionsPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "recipes.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    armorsPath = pathModule.join(projectPath, "src", "data", "armors.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
    mobsPath = pathModule.join(projectPath, "src", "data", "mobs.json");
    potionsPath = pathModule.join(projectPath, "src", "data", "potions.json");
    recipes = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    armors = fs.existsSync(armorsPath) ? fs.readJSONSync(armorsPath) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    mobs = fs.existsSync(mobsPath) ? fs.readJSONSync(mobsPath) : {};
    potions = fs.existsSync(potionsPath) ? fs.readJSONSync(potionsPath) : {};
    defaultItems = fs.readJSONSync(
      isDev ? "./static/data/items.json" : "./resources/app/data/items.json"
    );
    defaultPotions = fs.readJSONSync(
      isDev ? "./static/data/potions.json" : "./resources/app/data/potions.json"
    );
    Object.keys(recipes).forEach((recipe) => {
      recipes[recipe].name = recipe;
    });
    selectedRecipe = Object.keys(recipes)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "recipes.json") return;
        recipes = data.file.data;
      } else if (data.type == "SELECTED") selectedRecipe = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "recipes.json", data: recipes });
    };
  });
  let selectedRecipe = "";
  let name = "";
  function add() {
    name = `new_recipe_${Object.keys(recipes).length + 1}`;
    recipes[name] = {
      name,
      resultCount: 1,
      experience: 0,
      cookingTime: 0,
      type: "shaped",
      resultItem: "none",
      firstItem: "none",
      secondItem: "none",
      thirdItem: "none",
      fourthItem: "none",
      fifthItem: "none",
      sixthItem: "none",
      seventhItem: "none",
      eighthItem: "none",
      ninethItem: "none",
    };
    selectedRecipe = name;
    send_changes({ file: "recipes.json", data: recipes });
  }
  function save() {
    const obj = {};
    Object.keys(recipes).forEach((recipe) => {
      const name = recipes[recipe].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(recipes[recipe]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = recipes[recipe][property];
      });
    });
    fs.writeJSONSync(path, obj);
    recipes = obj;
    Object.keys(recipes).forEach((recipe) => {
      recipes[recipe].name = recipe;
    });
    selectedRecipe = recipes[selectedRecipe]
      ? selectedRecipe
      : Object.keys(recipes)[0];
    success("Recipes saved successfully!");
  }
  function deleteRecipe() {
    if (!selectedRecipe) return;
    delete recipes[selectedRecipe];
    recipes = recipes;
    selectedRecipe = Object.keys(recipes)[0];
    send_changes({ file: "recipes.json", data: recipes });
  }
</script>

<svelte:head>
  <title>OpenMod - Recipes</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Recipe:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedRecipe}
    >
      {#if !Object.keys(recipes).length}
        <option disabled value={selectedRecipe}>No recipes</option>
      {/if}
      {#each Object.keys(recipes) as recipe}
        <option value={recipe}>{recipe}</option>
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
        <button class="btn btn-error" on:click={deleteRecipe}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if recipes[selectedRecipe]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={recipes[selectedRecipe].name}
            />
          </div>
          {#if recipes[selectedRecipe].type == "shaped" || recipes[selectedRecipe].type == "shapeless"}
            <div>
              <label class="text-lg">Result Item Count</label>
              <input
                type="number"
                min="1"
                class="input w-full"
                bind:value={recipes[selectedRecipe].resultCount}
              />
            </div>
            <div>
              <label class="text-lg">Type</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].type}
                ><option value="shaped">Shaped</option>
                <option value="shapeless">Shapeless</option>
                <option value="smelting">Smelting</option>
                <option value="brewing">Brewing</option>
              </select>
            </div>
            <div>
              <label class="text-lg">Result Item</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].resultItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #1</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].firstItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #2</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].secondItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #3</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].thirdItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #4</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].fourthItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #5</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].fifthItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #6</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].sixthItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #7</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].seventhItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #8</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].eighthItem}
              />
            </div>
            <div>
              <label class="text-lg">Item #9</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].ninethItem}
              />
            </div>
          {:else if recipes[selectedRecipe].type == "smelting"}
            <div>
              <label class="text-lg">Experience</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={recipes[selectedRecipe].experience}
              />
            </div>
            <div>
              <label class="text-lg">Cooking Time (In Seconds)</label>
              <input
                type="number"
                min="0"
                class="input w-full"
                bind:value={recipes[selectedRecipe].cookingTime}
              />
            </div>
            <div>
              <label class="text-lg">Type</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].type}
                ><option value="shaped">Shaped</option>
                <option value="shapeless">Shapeless</option>
                <option value="smelting">Smelting</option>
                <option value="brewing">Brewing</option>
              </select>
            </div>
            <div>
              <label class="text-lg">Result Item</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].resultItem}
              />
            </div>
            <div>
              <label class="text-lg">Item</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].firstItem}
              />
            </div>
          {:else if recipes[selectedRecipe].type == "brewing"}
            <div>
              <label class="text-lg">Type</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].type}
                ><option value="shaped">Shaped</option>
                <option value="shapeless">Shapeless</option>
                <option value="smelting">Smelting</option>
                <option value="brewing">Brewing</option>
              </select>
            </div>
            <div>
              <label class="text-lg">Result Potion</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].resultItem}
                ><option value="">None</option>
                {#each Object.keys(potions) as potion}
                  <option value={potion}
                    >{projectName.toLowerCase()}:{potion}</option
                  >
                {/each}
                {#each defaultPotions as potion}
                  <option value={potion}>{potion}</option>
                {/each}
              </select>
            </div>
            <div>
              <label class="text-lg">Ingredient Item</label>
              <input
                list="itemList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].secondItem}
              />
            </div>
            <div>
              <label class="text-lg">Input Potion</label>
              <input
                list="potionList"
                class="input w-full"
                bind:value={recipes[selectedRecipe].firstItem}
              />
            </div>
          {/if}
        </div></Accordion
      >
    {/if}
  </div>
</div>
<datalist id="itemList">
  {#each Object.keys(items) as item}
    <option value="{projectName.toLowerCase()}:{item}" />
  {/each}
  {#each Object.keys(armors) as armor}
    <option value="{projectName.toLowerCase()}:{armor}" />
  {/each}
  {#each Object.keys(tools) as tool}
    <option value="{projectName.toLowerCase()}:{tool}" />
  {/each}
  {#each Object.keys(potions) as potion}
    <option value="{projectName.toLowerCase()}:{potion}" />
  {/each}
  {#each Object.keys(blocks) as block}
    <option value="{projectName.toLowerCase()}:{block}" />
  {/each}
  {#each Object.keys(trees) as tree}
    <option value="{projectName.toLowerCase()}:{tree}" />
  {/each}
  {#each Object.keys(mobs) as mob}
    <option value="{projectName.toLowerCase()}:{mob}" />
  {/each}
  {#each defaultItems as item}
    <option value={item} />
  {/each}
</datalist>
<datalist id="potionList">
  {#each Object.keys(potions) as potion}
    <option value="{projectName.toLowerCase()}:{potion}" />
  {/each}
  {#each defaultPotions as potion}
    <option value={potion} />
  {/each}
</datalist>
