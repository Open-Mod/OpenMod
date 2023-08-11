<script>
  import "../app.css";
  import Menu from "../components/Menu.svelte";
  import MenuItem from "../components/MenuItem.svelte";
</script>

<div class="flex w-screen h-screen" data-theme="dark">
  <Menu>
    <MenuItem tip="Home" page="/">
      <i class="fa-solid fa-house text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Items" page="/items">
      <i class="fa-solid fa-apple-whole text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Blocks" page="/blocks">
      <i class="fa-solid fa-cube text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Armor" page="/armor">
      <i class="fa-solid fa-shield-halved text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Tools" page="/tools">
      <i class="fa-solid fa-screwdriver-wrench text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Tool Tiers" page="/tiers">
      <i class="fa-solid fa-ranking-star text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Potions" page="/potions">
      <i class="fa-solid fa-flask text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Creative Tabs" page="/tabs">
      <i class="fa-solid fa-layer-group text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Loot Tables" page="/loottables">
      <i class="fa-solid fa-gift text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Recpies" page="/recipes">
      <i class="fa-solid fa-table-cells text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Biomes" page="/biomes">
      <i class="fa-solid fa-mountain-sun text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Trees" page="/trees">
      <i class="fa-solid fa-tree text-center inline-block" />
    </MenuItem>
    <MenuItem tip="Run" page="/run">
      <i class="fa-solid fa-play text-center inline-block" />
    </MenuItem>
  </Menu>
  <slot />
</div>
