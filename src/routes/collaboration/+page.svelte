<script>
  import { onMount } from "svelte";
  import Info from "../../components/Info.svelte";
  let state = 0;
  let projectPath = "";
  let id = "";
  let connid = "";
  onMount(() => {
    if (!selected) {
      error("Please select a project!");
      return (location.href = "/");
    }
    projectPath = pathModule.join(selected, "Project");
    const info = window.getState();
    state = info.state;
    id = info.id;
    connid = info.connid;
  });
  async function setState(value) {
    if (state == value) return;
    if (value == 0) {
      id = disconnect();
    } else if (value == 1) {
      disconnect();
      id = await host();
    } else if (value == 2) {
      if (!connid) return error("Please enter the ID to connect to!");
      disconnect();
      id = await connect(connid);
    }
    state = value;
  }
</script>

<svelte:head>
  <title>OpenMod - Collaboration</title>
</svelte:head>

<div class="flex flex-col w-full p-12 overflow-auto">
  {#if id}
    <h1 class="text-2xl font-bold mb-1">Your ID: {id}</h1>
  {/if}
  <div class="mb-3">
    <label class="text-lg">{state == 0 ? "Connect" : "Connected"} to ID</label>
    {#if state == 2}
      <input class="input w-full bg-neutral" disabled bind:value={connid} />
    {:else if state == 1}
      <input class="input w-full bg-neutral" disabled bind:value={id} />
    {:else}
      <input class="input w-full bg-neutral" bind:value={connid} />
    {/if}
  </div>
  <div class="join gap-[2px] mb-7">
    <a class="tooltip tooltip-top" data-tip="Connect">
      <button
        class="join-item btn {state == 2
          ? 'btn-warning'
          : 'btn-neutral'} text-lg w-fit"
        on:click={setState.bind(this, 2)}><i class="fa-solid fa-link" /></button
      >
    </a>
    <a class="tooltip tooltip-top" data-tip="Host">
      <button
        class="join-item btn {state == 1
          ? 'btn-warning'
          : 'btn-neutral'} text-lg w-fit"
        on:click={setState.bind(this, 1)}><i class="fa-solid fa-wifi" /></button
      ></a
    >
    <a class="tooltip tooltip-top" data-tip="Stop">
      <button
        class="join-item btn {state == 0
          ? 'btn-warning'
          : 'btn-neutral'} text-lg w-fit"
        on:click={setState.bind(this, 0)}><i class="fa-solid fa-stop" /></button
      ></a
    >
  </div>
  <Info
    info="Connecting to an user will overwrite your selected project's files!"
  />
</div>
