<script>
  import { onMount } from "svelte";
  import Accordion from "../../components/Accordion.svelte";
  let sounds = {};
  let projectPath = "";
  let path = "";
  onMount(() => {
    if (!selected) {
      alert("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    path = pathModule.join(projectPath, "src", "data", "sounds.json");
    sounds = fs.existsSync(path) ? fs.readJSONSync(path) : {};
    Object.keys(sounds).forEach((sound) => {
      sounds[sound].name = sound;
    });
    selectedSound = Object.keys(sounds)[0] ?? "";
    window.on_change = (data) => {
      if (data.type == "CHANGE") {
        if (data.file.file == "sounds.json") return;
        sounds = data.file.data;
      } else if (data.type == "SELECTED") selectedSound = data.selected;
    };
    window.onchange = () => {
      send_changes({ file: "sounds.json", data: sounds });
    };
  });
  let selectedSound = "";
  let name = "";
  function add() {
    name = `new_sound_${Object.keys(sounds).length + 1}`;
    sounds[name] = {
      name,
      sound: "",
    };
    selectedSound = name;
    send_changes({ file: "sounds.json", data: sounds });
  }
  function save() {
    const obj = {};
    Object.keys(sounds).forEach((sound) => {
      const name = sounds[sound].name
        .replace(/\s/g, "-")
        .replace(/./g, (char) => (/^[a-zA-Z0-9._-]+$/i.test(char) ? char : ""))
        .toLowerCase();
      obj[name] = {};
      Object.keys(sounds[sound]).forEach((property) => {
        if (property == "name") return;
        obj[name][property] = sounds[sound][property];
      });
    });
    fs.writeJSONSync(path, obj);
    sounds = obj;
    Object.keys(sounds).forEach((sound) => {
      sounds[sound].name = sound;
    });
    selectedSound = Object.keys(sounds)[0];
    success("Sounds saved successfully!");
  }
  function deleteSound() {
    if (!selectedSound) return;
    delete sounds[selectedSound];
    sounds = sounds;
    selectedSound = sounds[selectedSound]
      ? selectedSound
      : Object.keys(sounds)[0];
    send_changes({ file: "sounds.json", data: sounds });
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
      send_changes({ file: "sounds.json", data: sounds });
    }
  }
  function setSound(ev) {
    const reader = new FileReader();
    reader.onload = function (event) {
      sounds[selectedSound].sound = {
        name: ev.dataTransfer.files[0].name,
        data: event.target.result,
      };
      send_changes({ file: "sounds.json", data: sounds });
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
              style="{sounds[selectedSound].sound
                ? 'background-color: rgba(0,0,0,0.3)'
                : "background-image: url('/images/dropzone.png')"}; background-size:contain; line-height: 11rem"
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
