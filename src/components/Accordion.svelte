<script>
  import { onMount } from "svelte";
  export let title;
  export let style;
  export let mount;
  onMount(() => mount?.());
</script>

<div class="collapse collapse-arrow bg-base-200 mb-3">
  <input
    type="radio"
    name="accordion"
    on:change={(ev) => {
      document.getElementsByName("accordion").forEach((el) => {
        el.parentElement.children[2].style.height = "";
        el.parentElement.children[2].style.display = "none";
      });
      ev.target.parentElement.children[2].style.height = "fit-content";
      ev.target.parentElement.children[2].style.display = "";
    }}
  />
  <div class="collapse-title text-xl font-medium">
    {title}
  </div>
  <div class="collapse-content" {style}>
    <slot />
  </div>
</div>
