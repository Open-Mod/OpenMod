<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let tabs = {};
  let items = {};
  let tools = {};
  let armors = {};
  let blocks = {};
  let trees = {};
  let defaultItems = [];
  let projectPath = "";
  let path = "";
  let itemsPath = "";
  let toolsPath = "";
  let armorsPath = "";
  let blocksPath = "";
  let treesPath = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "tabs.json");
    itemsPath = pathModule.join(projectPath, "src", "data", "items.json");
    toolsPath = pathModule.join(projectPath, "src", "data", "tools.json");
    armorsPath = pathModule.join(projectPath, "src", "data", "armors.json");
    blocksPath = pathModule.join(projectPath, "src", "data", "blocks.json");
    treesPath = pathModule.join(projectPath, "src", "data", "trees.json");
    path = pathModule.join(projectPath, "src", "data", "tabs.json");
    tabs = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    items = fs.existsSync(itemsPath) ? fs.readJSONSync(itemsPath) : {};
    armors = fs.existsSync(armorsPath) ? fs.readJSONSync(armorsPath) : {};
    tools = fs.existsSync(toolsPath) ? fs.readJSONSync(toolsPath) : {};
    blocks = fs.existsSync(blocksPath) ? fs.readJSONSync(blocksPath) : {};
    trees = fs.existsSync(treesPath) ? fs.readJSONSync(treesPath) : {};
    defaultItems = fs.readJSONSync(
      isDev ? "./static/data/items.json" : "./resources/app/data/items.json"
    );
    Object.keys(tabs).forEach((tab) => {
      tabs[tab].name = tab;
      tabs[tab].icon = tabs[tab].icon.trim()
        ? tabs[tab].icon
        : Object.keys(items)[0] ??
          Object.keys(tools)[0] ??
          Object.keys(armors)[0] ??
          Object.keys(blocks)[0] ??
          Object.keys(trees)[0] ??
          defaultItems[0];
    });
    selectedTab = Object.keys(tabs)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "tabs.json") return;
      tabs = data.file.content;
    };
    window.onchange = () => {
      send_changes({ file: "tabs.json", data: tabs });
    };
  });
  let selectedTab = "";
  let name = "";
  function add() {
    name = `new_tab_${Object.keys(tabs).length + 1}`;
    tabs[name] = {
      name,
      title: convertToCamelCase(name),
      titleColor: "#000000",
      icon:
        Object.keys(items)[0] ??
        Object.keys(tools)[0] ??
        Object.keys(armors)[0] ??
        Object.keys(blocks)[0] ??
        Object.keys(trees)[0] ??
        defaultItems[0],
      withSearchbar: true,
      hideTitle: false,
      noScrollbar: false,
      alignedRight: false,
    };
    selectedTab = name;
    send_changes({ file: "tabs.json", data: tabs });
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
    selectedTab = tabs[selectedTab] ? selectedTab : Object.keys(tabs)[0];
    success("Tabs saved successfully!");
  }
  function deleteTab() {
    if (!selectedTab) return;
    delete tabs[selectedTab];
    tabs = tabs;
    selectedTab = Object.keys(tabs)[0];
    updateEditor();
    send_changes({ file: "tabs.json", data: tabs });
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
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Tab:</h1>
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
  <div class="w-full h-full overflow-y-auto mt-3">
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
                class="input w-full"
                bind:value={tabs[selectedTab].title}
              />
            </div>
            <div>
              <label class="text-lg">Label Color</label>
              <input
                type="color"
                class="w-full"
                bind:value={tabs[selectedTab].titleColor}
              />
            </div>
          {/if}
          <div>
            <label class="text-lg">Use Item For Icon</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].icon}
              >{#each Object.keys(items) as item}
                <option value={item}>{convertToCamelCase(item)}</option>
              {/each}
              {#each Object.keys(tools) as tool}
                <option value={tool}>{convertToCamelCase(tool)}</option>
              {/each}
              {#each Object.keys(armors) as armor}
                <option value={armor}>{convertToCamelCase(armor)}</option>
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
            <label class="text-lg">Can Hide Display Name?</label>
            <select
              class="select font-normal text-base w-full"
              bind:value={tabs[selectedTab].hideTitle}
              ><option value={true}>True</option><option value={false}
                >False</option
              ></select
            >
          </div>
          <div>
            <label class="text-lg">Can Hide Scrollbar?</label>
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
