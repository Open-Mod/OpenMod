<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let loottables = {};
  let blocks = {};
  let trees = {};
  let mobs = {};
  let projectPath = "";
  let path = "";
  let projectName = "";
  let blocksPath = "";
  let treesPath = "";
  let mobsPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "loottables.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
    mobsPath = pathModule.join(projectPath, "src", "data", "mobs.json");
    loottables = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    mobs = fs.existsSync(mobsPath) ? fs.readJSONSync(mobsPath) : {};
    Object.keys(loottables).forEach((loottable) => {
      loottables[loottable].name = loottable;
    });
    selectedLoottable = Object.keys(loottables)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "loottables.json") return;
        loottables = data.file.data;
      } else if (data.type == "SELECTED") selectedLoottable = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "loottables.json", data: loottables });
    };
  });
  let selectedLoottable = "";
  let name = "";
  function add() {
    name = `new_loottable_${Object.keys(loottables).length + 1}`;
    loottables[name] = {
      name,
      for:
        Object.keys(blocks).filter((block) => blocks[block].dropItem)[0] ??
        Object.keys(mobs)[0] ??
        "",
      json: "{}",
    };
    selectedLoottable = name;
    send_changes({ file: "loottables.json", data: loottables });
  }
  function save() {
    const obj = {};
    Object.keys(loottables).forEach((loottable) => {
      const name = loottables[loottable].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(loottables[loottable]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = loottables[loottable][property];
      });
    });
    fs.writeJSONSync(path, obj);
    loottables = obj;
    Object.keys(loottables).forEach((loottable) => {
      loottables[loottable].name = loottable;
      loottables[loottable].for = loottables[loottable].for.trim()
        ? loottables[loottable].for
        : Object.keys(blocks).filter((block) => blocks[block].dropItem)[0] ??
          Object.keys(mobs)[0] ??
          loottables[loottable].for;
    });
    selectedLoottable = loottables[selectedLoottable]
      ? selectedLoottable
      : Object.keys(loottables)[0];
    success("Loottables saved successfully!");
  }
  function deleteLoottable() {
    if (!selectedLoottable) return;
    delete loottables[selectedLoottable];
    loottables = loottables;
    selectedLoottable = Object.keys(loottables)[0];
    send_changes({ file: "loottables.json", data: loottables });
  }
  function setDefault() {
    if (parse(loottables[selectedLoottable].json).type == "minecraft:block") {
      loottables[selectedLoottable].for = Object.keys(blocks)[0];
    } else if (
      parse(loottables[selectedLoottable].json).type == "minecraft:entity"
    ) {
      loottables[selectedLoottable].for = Object.keys(mobs)[0];
    }
  }
  function parse(json) {
    let data = {};
    try {
      data = JSON.parse(json);
    } catch {}
    return data;
  }
</script>

<svelte:head>
  <title>OpenMod - Loot Tables</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Loot Table:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedLoottable}
    >
      {#if !Object.keys(loottables).length}
        <option disabled value={selectedLoottable}>No loot tables</option>
      {/if}
      {#each Object.keys(loottables) as loottable}
        <option value={loottable}>{loottable}</option>
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
        <button class="btn btn-error" on:click={deleteLoottable}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if loottables[selectedLoottable]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={loottables[selectedLoottable].name}
            />
          </div>
          {#if parse(loottables[selectedLoottable].json).type == "minecraft:block"}
            <div>
              <label class="text-lg">For Block</label>
              <input
                list="blockList"
                type="text"
                class="input w-full"
                bind:value={loottables[selectedLoottable].for}
              />
            </div>
          {:else if parse(loottables[selectedLoottable].json).type == "minecraft:entity"}
            <div>
              <label class="text-lg">For Mob</label>
              <input
                list="mobList"
                type="text"
                class="input w-full"
                bind:value={loottables[selectedLoottable].for}
              />
            </div>
          {/if}
          <div class="col-start-1 col-span-3">
            <label class="text-lg"
              >Loot Table JSON (use <a
                class="text-warning"
                href="https://misode.github.io/loot-table/"
                target="_blank">https://misode.github.io/loot-table/</a
              >)</label
            >
            <textarea
              class="textarea w-full h-[40vh] resize-none"
              bind:value={loottables[selectedLoottable].json}
              on:input={setDefault}
            />
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
<datalist id="blockList">
  {#each Object.keys(blocks).filter((block) => blocks[block].dropItem) as block}
    <option value={block}>{projectName.toLowerCase()}:{block}</option>
  {/each}
  {#each Object.keys(trees).filter((tree) => trees[tree].dropItem) as tree}
    <option value={tree}>{projectName.toLowerCase()}:{tree}</option>
  {/each}
</datalist>
<datalist id="mobList">
  {#each Object.keys(mobs) as mob}
    <option value="{projectName.toLowerCase()}:{mob}" />
  {/each}
</datalist>
