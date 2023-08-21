<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let materials = {};
  let tools = {};
  let items = {};
  let blocks = {};
  let sounds = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let toolsPath = "";
  let itemsPath = "";
  let blocksPath = "";
  let soundsPath = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "materials.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    soundsPath = pathModule.join(projectPath, "src", "data", "sounds.json");
    materials = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    sounds = fs.existsSync(soundsPath) ? fs.readJSONSync(soundsPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(materials).forEach((material) => {
      materials[material].name = material;
      materials[material].repairIngredient = materials[
        material
      ].repairIngredient.trim()
        ? materials[material].repairIngredient
        : Object.keys(tools)[0] ??
          Object.keys(items)[0] ??
          Object.keys(blocks)[0] ??
          materials[material].repairIngredient;
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
        Object.keys(tools)[0] ??
        Object.keys(items)[0] ??
        Object.keys(blocks)[0] ??
        defaultItems[0],
      texture: ["", ""],
    };
    selectedMaterial = name;
    send_changes({ file: "materials.json", content: materials });
  }
  function save() {
    const obj = {};
    const textureArmorModels = pathModule.join(
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
    fs.rmSync(textureArmorModels, { recursive: true, force: true });
    fs.mkdirSync(textureArmorModels);
    Object.keys(materials).forEach((material) => {
      const name = materials[material].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const texture1 = tools[tool].texture[0];
      const texture2 = tools[tool].texture[1];
      if (texture1) {
        const textureType = texture1.match(/[^:/]\w+(?=;|,)/)[0];
        const texturePath = pathModule.join(
          textureArmorModels,
          `${name}.${textureType}`
        );
        const textureData = texture1.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        fs.writeFileSync(texturePath, textureData, "base64");
      }
      if (texture2) {
        const textureType = texture2.match(/[^:/]\w+(?=;|,)/)[0];
        const texturePath = pathModule.join(
          textureArmorModels,
          `${name}.${textureType}`
        );
        const textureData = texture2.match(
          /^data:([A-Za-z-+\/]+);base64,(.+)$/
        )[2];
        fs.writeFileSync(texturePath, textureData, "base64");
      }
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
    selectedMaterial = Object.keys(materials)[0];
    success("Materials saved successfully!");
  }
  function deleteMaterial() {
    if (!selectedMaterial) return;
    delete materials[selectedMaterial];
    materials = materials;
    selectedMaterial = Object.keys(materials)[0];
    send_changes({ file: "materials.json", content: materials });
  }
  function fallbackTexture(ev) {
    ev.target.src = "/images/dropzone.png";
  }
  async function chooseTexture1() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      materials[selectedMaterial].texture = [
        `data:image/png;base64,${texture}`,
      ];
      send_changes({ file: "tools.json", content: tools });
    }
  }
  function setTexture1(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      materials[selectedMaterial].texture[0] = event.target.result;
      send_changes({ file: "tools.json", content: tools });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
  async function chooseTexture2() {
    const response = await ipc.invoke("dialog", "openFile", "png");
    if (response) {
      const texture = fs.readFileSync(
        response.filePaths[0].split("\\").join("/"),
        "base64"
      );
      material[
        selectedMaterial
      ].texture[1] = `data:image/png;base64,${texture}`;
      send_changes({ file: "tools.json", content: tools });
    }
  }
  function setTexture2(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      materials[selectedMaterial].texture = [event.target.result];
      send_changes({ file: "tools.json", content: tools });
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
          <div class="col-start-1">
            <label class="text-lg">Texture #1</label>
            <img
              class="w-48 h-48 cursor-pointer rounded-lg"
              src={materials[selectedMaterial].texture[0]}
              on:error={fallbackTexture}
              on:click={chooseTexture1}
              on:drop={setTexture1}
              on:dragover|preventDefault
            />
          </div>
          <div class="col-start-1">
            <label class="text-lg">Texture #2</label>
            <img
              class="w-48 h-48 cursor-pointer rounded-lg"
              src={materials[selectedMaterial].texture[1]}
              on:error={fallbackTexture}
              on:click={chooseTexture2}
              on:drop={setTexture2}
              on:dragover|preventDefault
            />
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
