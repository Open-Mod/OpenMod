<script>
  export let name;
</script>

<dialog id={name} class="modal">
  <form method="dialog" class="modal-box">
    <h3 class="font-bold text-2xl mb-3">{name}</h3>
    <slot name="content" />
    <div class="modal-action">
      <slot name="footer" />
    </div>
  </form>
</dialog>
