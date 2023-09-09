<script>
  import { onMount } from "svelte";

  export let tip;
  export let page;
  onMount(() => {
    tippy(`#${tip.toLowerCase().replace(/\s/g, "")}`, {
      content: tip,
      theme: "dark",
      placement: "right",
      delay: [0, 0],
      offset: [0, 5],
    });
  });
</script>

<li id={tip.toLowerCase().replace(/\s/g, "")} class="text-2xl">
  <a class="justify-center" href={page}> <slot /></a>
</li>
