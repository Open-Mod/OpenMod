<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let biomes = {};
  let blocks = {};
  let trees = {};
  let defaultBlocks = [];
  let projectPath = "";
  let path = "";
  let projectName = "";
  let blocksPath = "";
  let treesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "biomes.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
    biomes = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    defaultBlocks = fs.readJSONSync(
      isDev ? "./static/data/blocks.json" : "./resources/app/data/blocks.json"
    );
    Object.keys(biomes).forEach((biome) => {
      biomes[biome].name = biome;
      biomes[biome].blockAbove = biomes[biome].blockAbove.trim()
        ? biomes[biome].blockAbove
        : Object.keys(blocks)[0] ?? defaultBlocks[0];
      biomes[biome].blockUnder = biomes[biome].blockUnder.trim()
        ? biomes[biome].blockUnder
        : Object.keys(blocks)[0] ?? defaultBlocks[0];
    });
    selectedBiome = Object.keys(biomes)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "biomes.json") return;
        biomes = data.file.data;
      } else if (data.type == "SELECTED") selectedBiome = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "biomes.json", data: biomes });
    };
  });
  let selectedBiome = "";
  let name = "";
  function add() {
    name = `new_biome_${Object.keys(biomes).length + 1}`;
    biomes[name] = {
      name,
      weight: 1,
      type: "overworld",
      blockAbove:
        Object.keys(blocks)[0] ??
        Object.keys(trees)[0] ??
        defaultBlocks[0] ??
        "",
      blockUnder:
        Object.keys(blocks)[0] ??
        Object.keys(trees)[0] ??
        defaultBlocks[0] ??
        "",
      minTemp: "icy",
      maxTemp: "icy",
      minHumidity: "arid",
      maxHumidity: "arid",
      continentalness: ["mushroom_fields"],
      erosion: ["erosion_0"],
      depth: ["surface"],
      weirdness: ["low_slice_normal_descending"],
      json: "{}",
    };
    selectedBiome = name;
    send_changes({ file: "biomes.json", data: biomes });
  }
  function save() {
    const obj = {};
    Object.keys(biomes).forEach((biome) => {
      const name = biomes[biome].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
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
    selectedBiome = biomes[selectedBiome]
      ? selectedBiome
      : Object.keys(biomes)[0];
    success("Biomes saved successfully!");
  }
  function deleteBiome() {
    if (!selectedBiome) return;
    delete biomes[selectedBiome];
    biomes = biomes;
    selectedBiome = Object.keys(biomes)[0];
    send_changes({ file: "biomes.json", data: biomes });
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
          <div>
            <label class="text-lg">Weight</label>
            <input
              type="number"
              min="1"
              class="input w-full"
              bind:value={biomes[selectedBiome].weight}
            />
          </div>
          <div>
            <label class="text-lg">Type</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].type}
            >
              <option value="overworld">Overworld</option>
              <option value="nether">Nether</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Block Above</label>
            <input
              list="blockList"
              type="text"
              class="input w-full"
              bind:value={biomes[selectedBiome].blockAbove}
            />
          </div>
          <div>
            <label class="text-lg">Block Under</label>
            <input
              list="blockList"
              type="text"
              class="input w-full"
              bind:value={biomes[selectedBiome].blockUnder}
            />
          </div>
          <div>
            <label class="text-lg">Minimum Temperature</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].minTemp}
            >
              <option value="icy">Icy</option>
              <option value="cool">Cool</option>
              <option value="neutral">Neutral</option>
              <option value="warm">Warm</option>
              <option value="hot">Hot</option>
              <option value="frozen">Frozen</option>
              <option value="unfrozen">Unfrozen</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Maximum Temperature</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].maxTemp}
            >
              <option value="icy">Icy</option>
              <option value="cool">Cool</option>
              <option value="neutral">Neutral</option>
              <option value="warm">Warm</option>
              <option value="hot">Hot</option>
              <option value="frozen">Frozen</option>
              <option value="unfrozen">Unfrozen</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Minimum Humidity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].minHumidity}
            >
              <option value="arid">Arid</option>
              <option value="dry">Dry</option>
              <option value="neutral">Neutral</option>
              <option value="wet">Wet</option>
              <option value="humid">Humid</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg">Maximum Humidity</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].maxHumidity}
            >
              <option value="arid">Arid</option>
              <option value="dry">Dry</option>
              <option value="neutral">Neutral</option>
              <option value="wet">Wet</option>
              <option value="humid">Humid</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div class="col-start-1">
            <label class="text-lg"
              >Continentalness (hold <a class="text-warning">ctrl</a> to select multiple)</label
            >
            <select
              multiple
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].continentalness}
            >
              <option value="mushroom_fields">Mushroom Fields</option>
              <option value="deep_ocean">Deep Ocean</option>
              <option value="ocean">Ocean</option>
              <option value="coast">Coast</option>
              <option value="near_inland">Near Inland</option>
              <option value="mid_inland">Mid Inland</option>
              <option value="far_inland">Far Inland</option>
              <option value="inland">Inland</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg"
              >Erosion (hold <a class="text-warning">ctrl</a> to select multiple)</label
            >
            <select
              multiple
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].erosion}
            >
              <option value="erosion_0">Erosion 0</option>
              <option value="erosion_1">Erosion 1</option>
              <option value="erosion_2">Erosion 2</option>
              <option value="erosion_3">Erosion 3</option>
              <option value="erosion_4">Erosion 4</option>
              <option value="erosion_5">Erosion 5</option>
              <option value="erosion_6">Erosion 6</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg"
              >Depth (hold <a class="text-warning">ctrl</a> to select multiple)</label
            >
            <select
              multiple
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].depth}
            >
              <option value="surface">Surface</option>
              <option value="underground">Underground</option>
              <option value="floor">Floor</option>
              <option value="full_range">Full Range</option>
            </select>
          </div>
          <div>
            <label class="text-lg"
              >Weirdness (hold <a class="text-warning">ctrl</a> to select multiple)</label
            >
            <select
              multiple
              class="select font-normal text-base w-full"
              bind:value={biomes[selectedBiome].weirdness}
            >
              <option value="low_slice_normal_descending"
                >Low Slice Normal Descending</option
              >
              <option value="low_slice_variant_ascending"
                >Low Slice Variant Ascending</option
              >
              <option value="mid_slice_normal_ascending"
                >Mid Slice Normal Ascending</option
              >
              <option value="mid_slice_normal_descending"
                >Mid Slice Normal Descending</option
              >
              <option value="mid_slice_variant_ascending"
                >Mid Slice Variant Ascending</option
              >
              <option value="mid_slice_variant_descending"
                >Mid Slice Variant Descending</option
              >
              <option value="high_slice_normal_ascending"
                >High Slice Normal Ascending</option
              >
              <option value="high_slice_normal_descending"
                >High Slice Normal Descending</option
              >
              <option value="high_slice_variant_ascending"
                >High Slice Variant Ascending</option
              >
              <option value="high_slice_variant_descending"
                >High Slice Variant Descending</option
              >
              <option value="peak_normal">Peak Normal</option>
              <option value="peak_variant">Peak Variant</option>
              <option value="valley">Valley</option>
              <option value="full_range">Full Range</option>
            </select>
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
<datalist id="blockList">
  {#each Object.keys(blocks) as block}
    <option value="{projectName.toLowerCase()}:{block}" />
  {/each}
  {#each Object.keys(trees) as tree}
    <option value="{projectName.toLowerCase()}:{tree}" />
  {/each}
  {#each defaultBlocks as block}
    <option value={block} />
  {/each}</datalist
>
