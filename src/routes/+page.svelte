<script>
  import { onMount } from "svelte";
  import Modal from "../components/Modal.svelte";
  import Error from "../components/Error.svelte";
  import Success from "../components/Success.svelte";
  let projects = {};
  let projectName = "";
  let projectPath = "";
  let error = "";
  let success = "";
  let current = "";
  let percent = 0;
  onMount(() => {
    current = selected;
    const path = pathModule.join(appPath, "projects.json");
    projects = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    ipc.on("updateStart", () => {
      location.href = "#loading";
    });
    ipc.on("updateProgress", (ev, args) => {
      percent = args;
    });
    ipc.on("updateStop", () => {
      location.href = "#";
    });
  });
  async function add() {
    if (!projectName.trim()) {
      error = "Project name must not be empty!";
      return setTimeout(() => {
        error = "";
      }, 2000);
    }
    const name = projectName
      .trim()
      .replace(/\s/g, "_")
      .replace(/./g, (char) => (/[^a-zA-Z\d_]/g.test(char) ? "" : char));
    if (!projectPath) {
      error = "Project folder must not be empty!";
      return setTimeout(() => {
        error = "";
      }, 2000);
    }
    if (projects[projectPath]) {
      error = "Project already exists!";
      return setTimeout(() => {
        error = "";
      }, 2000);
    }
    window["Add Project"].close();
    await ipc.invoke("createProject", projectPath);
    projects[projectPath] = {
      name,
      createdAt: new Date(),
    };
    const project = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "java",
      "dev",
      "openmod",
      "project",
      "Project.java"
    );
    const projectData = fs.readFileSync(project).toString();
    const properties = pathModule.join(
      projectPath,
      "Project",
      "gradle.properties"
    );
    const propertiesData = fs.readFileSync(properties).toString();
    const itemModels = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "models",
      "item"
    );
    const itemTextures = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "textures",
      "item"
    );
    const toolModels = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "models",
      "tool"
    );
    const toolTextures = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "textures",
      "tool"
    );
    const blockModels = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "models",
      "block"
    );
    const blockTextures = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "textures",
      "block"
    );
    const blockstates = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "assets",
      name.toLowerCase(),
      "blockstates"
    );
    const blockloottables = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      name.toLowerCase(),
      "loot_tables",
      "blocks"
    );
    const recipes = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      name.toLowerCase(),
      "recipes"
    );
    const projectMinecraftData = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "tags",
      "blocks"
    );
    const minecraftData = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      "minecraft",
      "tags",
      "blocks",
      "mineable"
    );
    const forgeData = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      "forge",
      "tags",
      "blocks"
    );
    const biomeModifier = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "forge",
      "biome_modifier"
    );
    const worldgenConfigured = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "worldgen",
      "configured_feature"
    );
    const worldgenPlaced = pathModule.join(
      projectPath,
      "Project",
      "src",
      "main",
      "resources",
      "data",
      projectName.toLowerCase(),
      "worldgen",
      "placed_feature"
    );
    fs.writeFileSync(
      project,
      projectData.replace("${project}", projectName.trim().toLowerCase())
    );
    fs.writeFileSync(
      properties,
      propertiesData
        .replace("${project}", name.toLowerCase())
        .replace("${Project}", name.replace(/_/g, " "))
    );
    fs.ensureDirSync(itemModels);
    fs.ensureDirSync(itemTextures);
    fs.ensureDirSync(toolModels);
    fs.ensureDirSync(toolTextures);
    fs.ensureDirSync(blockModels);
    fs.ensureDirSync(blockTextures);
    fs.ensureDirSync(blockstates);
    fs.ensureDirSync(blockloottables);
    fs.ensureDirSync(recipes);
    fs.ensureDirSync(projectMinecraftData);
    fs.ensureDirSync(minecraftData);
    fs.ensureDirSync(forgeData);
    fs.ensureDirSync(biomeModifier);
    fs.ensureDirSync(worldgenConfigured);
    fs.ensureDirSync(worldgenPlaced);
    fs.writeJSONSync(pathModule.join(appPath, "projects.json"), projects);
    success = "Project created successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteProject(project) {
    delete projects[project];
    projects = projects;
    if (Object.keys(projects).length) {
      const projectPaths = Object.keys(projects);
      const project = projectPaths[projectPaths.length - 1];
      ipc.invoke("select", project);
      current = project;
    } else {
      current = "";
      ipc.invoke("select", "");
    }
    fs.writeJSONSync(pathModule.join(appPath, "projects.json"), projects);
    success = "Project deleted successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  async function choosePath() {
    const response = await ipc.invoke("dialog", "openDirectory");
    if (response) {
      projectPath = response.filePaths[0].split("\\").join("/");
    }
  }
  function selectProject(project) {
    ipc.invoke("select", project);
    current = project;
    ipc.invoke("select", current);
  }
</script>

<svelte:head>
  <title>OpenMod - Home</title>
</svelte:head>

<div class="flex flex-col w-full p-12 gap-3">
  <div class="overflow-x-auto">
    <table class="table static">
      <thead>
        <tr>
          <th class="text-lg">Project Name</th>
          <th class="text-lg">Date Created</th>
          <th class="text-right"
            ><a
              class="tooltip tooltip-top absolute translate-y-[-50%] translate-x-[-100%]"
              data-tip="Add"
            >
              <button
                class="btn btn-warning btn-sm"
                on:click={() => window["Add Project"].showModal()}
                ><i class="fa-solid fa-plus" /></button
              ></a
            ></th
          >
        </tr>
      </thead>
      <tbody>
        {#each Object.keys(projects) as project}
          <tr>
            <td class="text-base">{projects[project].name}</td>
            <td class="text-base"
              >{new Date(projects[project].createdAt)
                .toLocaleString()
                .split(",")[0]}</td
            >
            <td class="text-right">
              <a class="tooltip tooltip-top" data-tip="Select">
                <button
                  class="btn {current == project
                    ? 'btn-neutral'
                    : 'btn-success'} btn-sm"
                  on:click={() => selectProject(project)}
                  ><i class="fa-solid fa-check" /></button
                >
              </a><a class="tooltip tooltip-top" data-tip="Delete">
                <button
                  class="btn btn-error btn-sm ml-1"
                  on:click={() => deleteProject(project)}
                  ><i class="fa-solid fa-trash" /></button
                >
              </a></td
            >
          </tr>
        {/each}
      </tbody>
    </table>
  </div>
</div>
<Modal name="Add Project">
  <div slot="content">
    <input
      type="text"
      placeholder="Name"
      class="input input-bordered w-full"
      bind:value={projectName}
    />
    <div class="join w-full mt-3">
      <input
        type="text"
        placeholder="Folder"
        class="join-item input input-bordered w-full"
        readonly
        bind:value={projectPath}
      /><a class="tooltip tooltip-top" data-tip="Open Folder">
        <button
          class="join-item btn btn-warning"
          on:click|preventDefault={choosePath}
          ><i class="fa-solid fa-folder-open" /></button
        ></a
      >
    </div>
  </div>
  <div slot="footer">
    <button class="btn btn-warning" on:click|preventDefault={add}>Done</button>
    <button class="btn btn-error">Cancel</button>
  </div>
</Modal>
<div class="modal bg-opacity-60" id="loading">
  <div class="radial-progress text-2xl" style="--value:{percent}; --size:8rem;">
    {percent}%
  </div>
</div>
<Error {error} />
<Success {success} />
