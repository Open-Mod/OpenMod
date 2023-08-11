<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let tabs = {};
  let items = {};
  let blocks = {};
  let tools = {};
  let projectPath = "";
  let path = "";
  let itemsPath = "";
  let blocksPath = "";
  let toolsPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "tabs.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    path = pathModule.join(projectPath, "src", "data", "tabs.json");
    tabs = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    Object.keys(tabs).forEach((tab) => {
      tabs[tab].name = tab;
      tabs[tab].icon = tabs[tab].icon.trim()
        ? tabs[tab].icon
        : Object.keys(items)[0] ??
          Object.keys(blocks)[0] ??
          Object.keys(tools)[0] ??
          tabs[tab].icon;
    });
    selectedTab = Object.keys(tabs)[0] ?? "";
  });
  let selectedTab = "";
  let name = "";
  let error = "";
  let success = "";
  function add() {
    name = `new_tab_${Object.keys(tabs).length + 1}`;
    tabs[name] = {
      name,
      title: convertToCamelCase(name),
      icon:
        Object.keys(items)[0] ??
        Object.keys(blocks)[0] ??
        Object.keys(tools)[0] ??
        "",
      withSearchbar: true,
      hideTitle: false,
      noScrollbar: false,
      alignedRight: false,
    };
    selectedTab = name;
  }
  function save() {
    const obj = {};
    Object.keys(tabs).forEach((tab) => {
      const name = tabs[tab].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(tabs[tab]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = tabs[tab][property];
      });
    });
    fs.writeJSONSync(path, obj);
    tabs = obj;
    Object.keys(tabs).forEach((tab) => {
      tabs[tab].name = tab;
    });
    selectedTab = Object.keys(tabs)[0];
    success = "Tabs saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteTab() {
    if (!selectedTab) return;
    delete tabs[selectedTab];
    tabs = tabs;
    selectedTab = Object.keys(tabs)[0];
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
  <title>OpenMod - Tabs</title>
</svelte:head>
<div class="flex flex-col w-full p-12 gap-3">
  <h1 class="text-3xl font-bold">Selected Tab:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedTab}
    >
      {#if !Object.keys(tabs).length}
        <option disabled value={selectedTab}>No tabs</option>
      {/if}
      {#each Object.keys(tabs) as tab}
        <option value={tab}>{tab}</option>
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
        <button class="btn btn-error" on:click={deleteTab}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto">
    {#if tabs[selectedTab]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={tabs[selectedTab].name}
            />
          </div>
          {#if !tabs[selectedTab].hideTitle}
            <div>
              <label class="text-lg">Display Name</label>
              <input
                type="text"
                class="input w-full"
                bind:value={tabs[selectedTab].title}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Use item for icon</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].icon}
              >{#each Object.keys(items) as item}
                <option value={item}>{convertToCamelCase(item)}</option>
              {/each}
              {#each Object.keys(blocks) as block}
                <option value={block}>{convertToCamelCase(block)}</option>
              {/each}
              {#each Object.keys(tools) as tool}
                <option value={tool}>{convertToCamelCase(tool)}</option>
              {/each}
            </select>
          </div>
          <div>
            <label class="text-lg">Has Searchbar?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].withSearchbar}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Is Aligned To Right?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].alignedRight}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Hide Display Name?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].hideTitle}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Hide Scrollbar?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].noScrollbar}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
<Error {error} />
<Success {success} />
