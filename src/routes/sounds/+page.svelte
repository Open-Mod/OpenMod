<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  import Error from "../../components/Error.svelte";
  import Success from "../../components/Success.svelte";
  let sounds = {};
  let projectPath = "";
  let path = "";
  let projectName = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "sounds.json");
    projectName = fs.readJSONSync(pathModule.join(appPath, "projects.json"))[
      selected
    ].name;
    sounds = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    Object.keys(sounds).forEach((sound) => {
      sounds[sound].name = sound;
    });
    selectedSound = Object.keys(sounds)[0] ?? "";
    window.on_change = (data) => {
      if (data.file.file != "sounds.json") return;
      sounds = data.file.content;
    };
  });
  let selectedSound = "";
  let name = "";
  let error = "";
  let success = "";
  function add() {
    name = `new_sound_${Object.keys(sounds).length + 1}`;
    sounds[name] = {
      name,
      sound: "",
    };
    selectedSound = name;
    send_changes({ file: "sounds.json", content: sounds });
  }
  function save() {
    const obj = {};
    const soundsObj = {};
    const assetsPath = pathModule.join(
      projectPath,
      "src",
      "main",
      "resources",
      "assets",
      projectName.toLowerCase()
    );
    const soundsPath = pathModule.join(assetsPath, "sounds");
    Object.keys(sounds).forEach((sound) => {
      const name = sounds[sound].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      soundsObj[name] = {
        sounds: [`${projectName.toLowerCase()}:${name}`],
      };
      Object.keys(sounds[sound]).forEach((property) => {
        if (property == "name") return;
        if (property == "sound") {
          const Sound = sounds[sound][property].data;
          const soundPath = pathModule.join(soundsPath, `${name}.ogg`);
          const soundData = Sound.match(
            /^data:([A-Za-z-+\/]+);base64,(.+)$/
          )[2];
          fs.writeFileSync(soundPath, soundData, "base64");
        }
        obj[name][property] = sounds[sound][property];
      });
    });
    fs.writeJSONSync(pathModule.join(assetsPath, "sounds.json"), soundsObj);
    fs.writeJSONSync(path, obj);
    sounds = obj;
    Object.keys(sounds).forEach((sound) => {
      sounds[sound].name = sound;
    });
    selectedSound = Object.keys(sounds)[0];
    success = "Sounds saved successfully!";
    setTimeout(() => {
      success = "";
    }, 2000);
  }
  function deleteSound() {
    if (!selectedSound) return;
    delete sounds[selectedSound];
    sounds = sounds;
    selectedSound = Object.keys(sounds)[0];
    updateEditor();
    send_changes({ file: "sounds.json", content: sounds });
  }
  async function chooseSound() {
    const response = await ipc.invoke("dialog", "openFile", "ogg");
    if (response) {
      const splitted = response.filePaths[0].split("\\");
      const sound = fs.readFileSync(splitted.join("/"), "base64");
      sounds[selectedSound].sound = {
        name: splitted[splitted.length - 1],
        data: `data:audio/ogg;base64,${sound}`,
      };
      send_changes({ file: "sounds.json", content: sounds });
    }
  }
  function setSound(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      sounds[selectedSound].sound = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result,
      };
      send_changes({ file: "sounds.json", content: sounds });
    };
    reader.readAsDataURL(ev.dataTransfer.files[0]);
  }
</script>

<svelte:head>
  <title>OpenMod - Sounds</title>
</svelte:head>
<div class="flex flex-col w-full p-12">
  <h1 class="text-2xl font-bold mb-1">Selected Sound:</h1>
  <div class="flex flex-row w-full gap-3">
    <select
      class="select select-bordered font-normal text-base w-full"
      bind:value={selectedSound}
    >
      {#if !Object.keys(sounds).length}
        <option disabled value={selectedSound}>No sounds</option>
      {/if}
      {#each Object.keys(sounds) as sound}
        <option value={sound}>{sound}</option>
      {/each}
    </select>
    <div class="flex gap-1">
      <a class="soundtip soundtip-top" data-tip="Add">
        <button class="btn btn-warning" on:click={add}
          ><i class="fa-solid fa-plus text-lg" /></button
        ></a
      >
      <a class="soundtip soundtip-top" data-tip="Save">
        <button class="btn btn-success" on:click={save}
          ><i class="fa-solid fa-floppy-disk text-lg" /></button
        >
      </a>
      <a class="soundtip soundtip-top" data-tip="Delete">
        <button class="btn btn-error" on:click={deleteSound}
          ><i class="fa-solid fa-trash text-lg" /></button
        >
      </a>
    </div>
  </div>
  <div class="w-full h-full overflow-y-auto mt-3">
    {#if sounds[selectedSound]}
      <Accordion title="General">
        <div class="grid grid-cols-3 gap-3">
          <div>
            <label class="text-lg">Name</label>
            <input
              type="text"
              class="input w-full"
              bind:value={sounds[selectedSound].name}
            />
          </div>
          <div class="col-start-1">
            <label class="text-lg">Sound</label>
            <div
              class="w-48 h-48 cursor-pointer rounded-lg text-ellipsis overflow-hidden text-center px-3"
              style="background:{sounds[selectedSound].sound
                ? 'rgba(0,0,0,0.3)'
                : "url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
              on:click={chooseSound}
              on:drop={setSound}
              on:dragover|preventDefault
            >
              {sounds[selectedSound].sound?.name ?? ""}
            </div>
          </div>
        </div></Accordion
      >
    {/if}
  </div>
</div>
<Error {error} />
<Success {success} />
