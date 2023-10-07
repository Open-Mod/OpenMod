<script>
  import { onMount } from "svelte";
  import Card from "../../components/Card.svelte";
  let projects = [];
  let projectsPath = "";
  let projectPath = "";
  let pluginsPath = "";
  let pluginName = "";
  let installedPlugins = [];
  let plugins = [];
  let filteredPlugins = [];
  let modPlugins = [];
  onMount(async () => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectsPath = pathModule.join(appPath, "projects.json");
    projects = fs.readJSONSync(projectsPath);
    projectPath = pathModule.join(selected, "Project");
    pluginsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "plugins"
    );
    installedPlugins = projects[selected].plugins.filter((p) =>
      fs.existsSync(pathModule.join(pluginsPath, "ui", p.file))
    );
    modPlugins = await fetch(
      `https://api.github.com/repos/Open-Mod/Plugins/contents/mod`
    ).then((res) => res.json());
    plugins = await Promise.all(
      (
        await fetch(
          `https://api.github.com/repos/Open-Mod/Plugins/contents/ui`
        ).then((res) => res.json())
      ).map(async (p) => {
        const res = await fetch(p.download_url).then((res) => res.json());
        return {
          name: res.name,
          sha: p.sha,
          modSha: modPlugins.find((pl) => pl.name == `${res.plugin}.java`).sha,
          file: p.name,
          data: res,
        };
      })
    );
    filteredPlugins = plugins;
  });
  async function download(p) {
    if (
      installedPlugins.find(
        (pl) =>
          pl.name == p.name &&
          pl.data.category == p.data.category &&
          pl.data.for == p.data.for &&
          pl.sha == p.sha &&
          pl.modSha == p.modSha
      )
    )
      return;
    const uiPath = pathModule.join(pluginsPath, "ui", p.file);
    const modPath = pathModule.join(
      pluginsPath,
      "mod",
      `${p.data.plugin}.java`
    );
    const i = projects[selected].plugins.findIndex(
      (pl) =>
        pl.name == p.name &&
        pl.data.category == p.data.category &&
        pl.data.for == p.data.for &&
        pl.sha == p.sha &&
        pl.modSha == p.modSha
    );
    const oi = projects[selected].plugins.findIndex(
      (pl) =>
        pl.name == p.name &&
        pl.data.category == p.data.category &&
        pl.data.for == p.data.for
    );
    if (oi > i) projects[selected].plugins.splice(oi, 1);
    if (i > -1) {
      projects[selected].plugins[i] = p;
    } else {
      projects[selected].plugins.push(p);
    }
    installedPlugins = projects[selected].plugins;
    const data = await fetch(
      modPlugins.find((pl) => pl.name == `${p.data.plugin}.java`).download_url
    ).then((res) => res.text());
    fs.writeJSONSync(uiPath, p.data);
    fs.writeFileSync(modPath, data);
    fs.writeJSONSync(projectsPath, projects);
  }
  function search(ev) {
    filteredPlugins = plugins.filter(
      (p) =>
        p.name.toLowerCase().includes(ev.target.value.toLowerCase()) ||
        (p.data.for ?? p.data.category).includes(
          ev.target.value.toLowerCase()
        ) ||
        `${p.name.toLowerCase()} ${p.data.for ?? p.data.category}`.includes(
          ev.target.value.toLowerCase()
        )
    );
  }
</script>

<svelte:head>
  <title>OpenMod - Plugins</title>
</svelte:head>

<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Plugin Name:</h1>
  <div class="flex flex-row w-full">
    <input
      class="input input-bordered font-normal text-base w-full"
      placeholder="Search"
      bind:value={pluginName}
      on:input={search}
    />
  </div>
  <div class="w-full h-full overflow-y-auto mt-3 grid grid-cols-3 gap-3">
    {#each filteredPlugins as plugin}
      <Card>
        {plugin.name}
        <span slot="subtitle"
          >{plugin.data.for
            ? plugin.data.for[0].toUpperCase() + plugin.data.for.slice(1)
            : plugin.data.category[0].toUpperCase() +
              plugin.data.category.slice(1)}</span
        >
        <button
          class="btn {installedPlugins.find(
            (pl) =>
              pl.name == plugin.name &&
              pl.data.category == plugin.data.category &&
              pl.data.for == plugin.data.for &&
              pl.sha == plugin.sha &&
              pl.modSha == plugin.modSha
          )
            ? 'btn-neutral'
            : 'btn-warning'}"
          slot="footer"
          on:click={download.bind(this, plugin)}
          >{installedPlugins.find(
            (pl) =>
              pl.name == plugin.name &&
              pl.data.category == plugin.data.category &&
              pl.data.for == plugin.data.for &&
              pl.sha == plugin.sha &&
              pl.modSha == plugin.modSha
          )
            ? "Installed"
            : "Install"}</button
        >
      </Card>
    {/each}
  </div>
</div>
