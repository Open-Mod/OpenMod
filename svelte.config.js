import adapter from "@sveltejs/adapter-static";
import { vitePreprocess } from "@sveltejs/kit/vite";
//import node from "@sveltejs/adapter-node";

//const dev = process.env.NODE_ENV == "development"

export default {
  kit: {
    //adapter: node()
    adapter: adapter(),
  },

  preprocess: vitePreprocess(),
};