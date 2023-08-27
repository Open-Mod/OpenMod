<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let biomes = {};
  let blocks = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let blocksPath = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "biomes.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    biomes = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    defaultItems = fs.readJSONSync("./src/data/items.json");
    Object.keys(biomes).forEach((biome) => {
      biomes[biome].name = biome;
    });
    selectedBiome = Object.keys(biomes)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "biomes.json") return;
      biomes = data.file.content;
    };
  });
  let selectedBiome = "";
  let name = "";
  function add() {
    name = `new_biome_${Object.keys(biomes).length + 1}`;
    biomes[name] = {
      name,
      for:
        Object.keys(
          Object.keys(blocks).filter((block) => blocks[block].dropItem)
        )[0] ??
        defaultItems[0] ??
        "",
      json: "{}",
    };
    selectedBiome = name;
    send_changes({ file: "biomes.json", content: biomes });
  }
  function save() {
    const obj = {};
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
    fs.rmSync(biomesPath, { recursive: true, force: true });
    fs.mkdirSync(biomesPath);
    Object.keys(biomes).forEach((biome) => {
      const name = biomes[biome].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      const biomePath = pathModule.join(biomesPath, `${name}.json`);
      fs.writeFileSync(biomePath, biomes[biome].json);
      Object.keys(biomes[biome]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = biomes[biome][property];
      });
    });
    fs.writeJSONSync(path, obj);
    biomes = obj;
    Object.keys(biomes).forEach((biome) => {
      biomes[biome].name = biome;
    });
    selectedBiome = Object.keys(biomes)[0];
    success("Biomes saved successfully!");
  }
  function deleteBiome() {
    if (!selectedBiome) return;
    delete biomes[selectedBiome];
    biomes = biomes;
    selectedBiome = Object.keys(biomes)[0];
    updateEditor();
    send_changes({ file: "biomes.json", content: biomes });
  }
</script>

<svelte:head>
  <title>OpenMod - Biomes</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Biome:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedBiome}
    >
      {#if !Object.keys(biomes).length}
        <option disabled value={selectedBiome}>No biomes</option>
      {/if}
      {#each Object.keys(biomes) as biome}
        <option value={biome}>{biome}</option>
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
        <button class="btn btn-error" on:click={deleteBiome}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if biomes[selectedBiome]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={biomes[selectedBiome].name}
            />
          </div>
          <div class="col-start-1 col-span-3">
            <label class="text-lg"
              >Biome JSON (use <a
                class="text-warning"
                href="https://misode.github.io/worldgen/biome/"
                target="_blank">https://misode.github.io/worldgen/biome/</a
              >)</label
            >
            <textarea
              class="textarea w-full h-[40vh] resize-none"
              bind:value={biomes[selectedBiome].json}
            />
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
