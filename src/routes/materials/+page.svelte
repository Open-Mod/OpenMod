<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let materials = {};
  let armors = {};
  let tools = {};
  let items = {};
  let blocks = {};
  let trees = {};
  let sounds = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let armorsPath = "";
  let toolsPath = "";
  let itemsPath = "";
  let blocksPath = "";
  let treesPath = "";
  let soundsPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "materials.json");
    armorsPath = pathModule.join(projectPath, "src", "data", "armors.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    materials = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    armors = fs.existsSync(armorsPath) ? fs.readJSONSync(armorsPath) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(materials).forEach((material) => {
      materials[material].name = material;
      materials[material].repairIngredient = materials[
        material
      ].repairIngredient.trim()
        ? materials[material].repairIngredient
        : Object.keys(armors)[0] ??
          Object.keys(tools)[0] ??
          Object.keys(items)[0] ??
          Object.keys(blocks)[0] ??
          Object.keys(trees)[0] ??
          defaultItems[0];
    });
    selectedMaterial = Object.keys(materials)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "materials.json") return;
      materials = data.file.content;
    };
  });
  let selectedMaterial = "";
  let name = "";
  function add() {
    name = `new_material_${Object.keys(materials).length + 1}`;
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
      equipSound: "default",
      repairIngredient:
        Object.keys(armors)[0] ??
        Object.keys(tools)[0] ??
        Object.keys(items)[0] ??
        Object.keys(blocks)[0] ??
        Object.keys(trees)[0] ??
        defaultItems[0],
    };
    selectedMaterial = name;
    send_changes({ file: "materials.json", data: materials });
  }
  function save() {
    const obj = {};
    Object.keys(materials).forEach((material) => {
      const name = materials[material].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(materials[material]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = materials[material][property];
      });
    });
    fs.writeJSONSync(path, obj);
    materials = obj;
    Object.keys(materials).forEach((material) => {
      materials[material].name = material;
    });
    selectedMaterial = materials[selectedMaterial]
      ? selectedMaterial
      : Object.keys(materials)[0];
    success("Materials saved successfully!");
  }
  function deleteMaterial() {
    if (!selectedMaterial) return;
    delete materials[selectedMaterial];
    materials = materials;
    selectedMaterial = Object.keys(materials)[0];
    send_changes({ file: "materials.json", data: materials });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture(property, filters) {
    const response = await ipc.invoke("dialog", "openFile", filters);
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const item = fs.readFileSync(splitted.join("/"), "base64");
      materials[selectedMaterial][property] = {
        name: splitted[splitted.length - 1],
        data: item,
      };
      send_changes({ file: "materials.json", data: materials });
    }
  }
  function setTexture(property, ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      materials[selectedMaterial][property] = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result
          .replace("data:image/png;base64,", "")
          .replace("data:application/json;base64,", ""),
      };
      send_changes({ file: "materials.json", data: materials });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
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
      {#each Object.keys(materials) as material}
        <option value={material}>{material}</option>
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
            <label class="text-lg">Toughness</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={materials[selectedMaterial].toughness}
            />
          </div>
          <div>
            <label class="text-lg">Knockback Resistance</label>
            <input
              type="number"
              min="0"
              class="input w-full"
              bind:value={materials[selectedMaterial].knockbackResistance}
            />
          </div>
          <div>
            <label class="text-lg">Equip Sound</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={materials[selectedMaterial].equipSound}
            >
              <option value="default">Default</option>
              {#each Object.keys(sounds) as sound}
                <option value={sound}>{convertToCamelCase(sound)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Repair With Item</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={materials[selectedMaterial].repairIngredient}
            >
              {#each Object.keys(armors) as armor}
                <option value={armor}>{convertToCamelCase(armor)}</option>
              {/each}
              {#each Object.keys(tools) as tool}
                <option value={tool}>{convertToCamelCase(tool)}</option>
              {/each}
              {#each Object.keys(items) as item}
                <option value={item}>{convertToCamelCase(item)}</option>
              {/each}
              {#each Object.keys(blocks) as block}
                <option value={block}>{convertToCamelCase(block)}</option>
              {/each}
              {#each Object.keys(trees) as tree}
                <option value={tree}>{convertToCamelCase(tree)}</option>
              {/each}
              {#each defaultItems as item}
                <option value={item}>{item}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Model Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={materials[selectedMaterial].modelType}
            >
              <option value="default">Default</option>
              <option value="blockbench">Blockbench</option>
            </select>
          </div>
          {#if materials[selectedMaterial].modelType == "blockbench"}
            <div>
              <label class="text-lg">Texture</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={`data:image/png;base64,${materials[selectedMaterial].texture?.data}`}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture", "png")}
                on:drop={setTexture.bind(this, "texture")}
                on:dragover|preventDefault
              />
            </div>
            <div>
              <label class="text-lg">Geo</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{materials[selectedMaterial].geo
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "geo", "json")}
                on:drop={setTexture.bind(this, "geo")}
                on:dragover|preventDefault
              >
                {materials[selectedMaterial].geo?.name ?? ""}
              </div>
            </div>
            <div>
              <label class="text-lg">Idle Animation</label>
              <div
                class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
                style="{materials[selectedMaterial].animation
                  ? 'background-color: rgba(0,0,0,0.3)'
                  : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
                on:click={chooseTexture.bind(this, "animation", "json")}
                on:drop={setTexture.bind(this, "animation")}
                on:dragover|preventDefault
              >
                {materials[selectedMaterial].animation?.name ?? ""}
              </div>
            </div>
          {:else}
            <div class="col-start-1">
              <label class="text-lg">Texture #1</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={materials[selectedMaterial].texture1}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture1", "png")}
                on:drop={setTexture.bind(this, "texture1")}
                on:dragover|preventDefault
              />
            </div>
            <div class="col-start-1">
              <label class="text-lg">Texture #2</label>
              <img
                class="w-48 h-48 cursor-pointer rounded-lg"
                src={materials[selectedMaterial].texture2}
                on:error={fallbackTexture}
                on:click={chooseTexture.bind(this, "texture2", "png")}
                on:drop={setTexture.bind(this, "texture2")}
                on:dragover|preventDefault
              />
            </div>
          {/if}
        </div></Accordion
      >
    {/if}
  </div>
</div>
