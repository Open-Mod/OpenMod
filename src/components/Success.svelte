<script>
  export let success;
</script>

<div
  class="alert alert-success shadow-lg absolute {success.length > 0
    ? 'visible'
    : 'hidden'} animate-fade bottom-20 z-[1000] w-2/4 left-1/4"
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
      d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
    /></svg
  >
  <span class="font-bold text-xl">{success}</span>
</div>
