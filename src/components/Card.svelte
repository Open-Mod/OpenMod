<div class="card w-full h-fit bg-base-200">
  <div class="card-body p-5">
    <h2 class="card-title"><slot /></h2>
    <slot name="subtitle" />
    <div class="card-actions justify-end">
      <slot name="footer" />
    </div>
  </div>
</div>
