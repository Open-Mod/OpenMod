<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let recipes = {};
  let items = {};
  let blocks = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let itemsPath = "";
  let blocksPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "recipes.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    recipes = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(recipes).forEach((recipe) => {
      recipes[recipe].name = recipe;
    });
    selectedRecipe = Object.keys(recipes)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "recipes.json") return;
      recipes = data.file.content;
    };
  });
  let selectedRecipe = "";
  let name = "";
  function add() {
    name = `new_recipe_${Object.keys(recipes).length + 1}`;
    recipes[name] = {
      name,
      type: "shaped",
      resultCount: 1,
      experience: 0,
      cookingTime: 0,
      resultItem: "",
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
    send_changes({ file: "recipes.json", content: recipes });
  }
  function save() {
    const obj = {};
    const recipesPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "recipes"
    );
    fs.rmSync(recipesPath, { recursive: true, force: true });
    fs.mkdirSync(recipesPath);
    Object.keys(recipes).forEach((recipe) => {
      const name = recipes[recipe].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const recipePath = pathModule.join(recipesPath, `${name}.json`);
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
    selectedRecipe = Object.keys(recipes)[0];
    success("Recipes saved successfully!");
  }
  function deleteRecipe() {
    if (!selectedRecipe) return;
    delete recipes[selectedRecipe];
    recipes = recipes;
    selectedRecipe = Object.keys(recipes)[0];
    updateEditor();
    send_changes({ file: "recipes.json", content: recipes });
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
          <div>
            <label class="text-lg">Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={recipes[selectedRecipe].type}
              ><option value="shaped">Shaped</option>
              <option value="shapeless">Shapeless</option>
              <option value="smelting">Smelting</option>
            </select>
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
              <label class="text-lg">Result Item</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].resultItem}
                ><option value="">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #1</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].firstItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #2</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].secondItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #3</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].thirdItem}
              >
                <option value="none">None</option>
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
            <div>
              <label class="text-lg">Item #4</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].fourthItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #5</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].fifthItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #6</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].sixthItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #7</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].seventhItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #8</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].eighthItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item #9</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].ninethItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
              <label class="text-lg">Result Item</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].resultItem}
                ><option value="">None</option
                >{#each Object.keys(items) as item}
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
            <div>
              <label class="text-lg">Item</label>
              <select
                class="select font-normal text-base w-full"
                bind:value={recipes[selectedRecipe].firstItem}
                ><option value="none">None</option
                >{#each Object.keys(items) as item}
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
          {/if}
        </div></Accordion
      >
    {/if}
  </div>
</div>
