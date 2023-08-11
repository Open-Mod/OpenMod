<script>
  export let tip;
  export let page;
  let slot;
</script>

<li class="text-2xl static rounded-lg hover:bg-[hsl(var(--bc)/0.1)]">
  <a
    class="tooltip tooltip-right absolute h-[40px] w-[62px] text-2xl !bg-transparent"
    href={page}
    data-tip={tip}
  />
  <slot />
</li>
