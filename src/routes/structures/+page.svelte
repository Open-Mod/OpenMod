<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let structures = {};
  let projectPath = "";
  let path = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "structures.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    structures = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    Object.keys(structures).forEach((structure) => {
      structures[structure].name = structure;
    });
    selectedStructure = Object.keys(structures)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "structures.json") return;
        structures = data.file.data;
      } else if (data.type == "SELECTED") selectedStructure = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "structures.json", data: structures });
    };
  });
  let selectedStructure = "";
  let name = "";
  function add() {
    name = `new_structure_${Object.keys(structures).length + 1}`;
    structures[name] = {
      name,
      structureJson: "{}",
      structureSetJson: "{}",
      templatePoolJson: "{}",
    };
    selectedStructure = name;
    send_changes({ file: "structures.json", data: structures });
  }
  function save() {
    const obj = {};
    Object.keys(structures).forEach((structure) => {
      const name = structures[structure].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(structures[structure]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = structures[structure][property];
      });
    });
    fs.writeJSONSync(path, obj);
    structures = obj;
    Object.keys(structures).forEach((structure) => {
      structures[structure].name = structure;
    });
    selectedStructure = structures[selectedStructure]
      ? selectedStructure
      : Object.keys(structures)[0];
    success("Structures saved successfully!");
  }
  function deleteStructure() {
    if (!selectedStructure) return;
    delete structures[selectedStructure];
    structures = structures;
    selectedStructure = Object.keys(structures)[0];
    send_changes({ file: "structures.json", data: structures });
  }
</script>

<svelte:head>
  <title>OpenMod - Structures</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Structure:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedStructure}
    >
      {#if !Object.keys(structures).length}
        <option disabled value={selectedStructure}>No structures</option>
      {/if}
      {#each Object.keys(structures) as structure}
        <option value={structure}>{structure}</option>
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
        <button class="btn btn-error" on:click={deleteStructure}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if structures[selectedStructure]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={structures[selectedStructure].name}
            />
          </div>
          <div class="col-start-1 col-span-3">
            <label class="text-lg"
              >Structure JSON (use <a
                class="text-warning"
                href="https://misode.github.io/worldgen/structure/"
                target="_blank">https://misode.github.io/worldgen/structure/</a
              >)</label
            >
            <textarea
              class="textarea w-full h-[40vh] resize-none"
              bind:value={structures[selectedStructure].structureJson}
            />
          </div>
          <div class="col-start-1 col-span-3">
            <label class="text-lg"
              >Structure Set JSON (use <a
                class="text-warning"
                href="https://misode.github.io/worldgen/structure-set/"
                target="_blank"
                >https://misode.github.io/worldgen/structure-set/</a
              >)</label
            >
            <textarea
              class="textarea w-full h-[40vh] resize-none"
              bind:value={structures[selectedStructure].structureSetJson}
            />
          </div>
          <div class="col-start-1 col-span-3">
            <label class="text-lg"
              >Template Pool JSON (use <a
                class="text-warning"
                href="https://misode.github.io/worldgen/template-pool/"
                target="_blank"
                >https://misode.github.io/worldgen/template-pool/</a
              >)</label
            >
            <textarea
              class="textarea w-full h-[40vh] resize-none"
              bind:value={structures[selectedStructure].templatePoolJson}
            />
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
