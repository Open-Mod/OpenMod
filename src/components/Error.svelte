<script>
  export let error;
</script>

<div
  class="alert alert-error shadow-lg absolute {error.length > 0
    ? 'visible'
    : 'hidden'} animate-fade bottom-20 w-2/4 left-1/4"
>
  <svg
    xmlns="http://www.w3.org/2000/svg"
    class="stroke-current flex-shrink-0 h-9 w-9"
    fill="none"
    viewBox="0 0 20 24"
    ><path
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="2"
      d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"
    /></svg
  >
  <span class="font-bold text-xl">{error}</span>
</div>
