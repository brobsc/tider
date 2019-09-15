# tider for reddit

a tidier reddit experience

## Development

Start dev environment by running first:

``` bash
npm install -g shadow-cljs
npm install
```

Then start it with:

``` bash
clj -A:cljs:dev
```

This will start four things:

* clojure backend server (at port `3000`)
* nREPL server for the clojure backend (at port `6688`)
* shadow-cljs watcher (at port `9630`)
* a CLJS nREPL for the frontend (at port `6699`)

*Note this will also hold the current process with these four "servers", closing it closes the entire dev environment.*

After this has started, and you see the REPL prompt, the dev env is ready to go. Visit `http://localhost:3000` to open the app. The CLJS nREPL won't work until you visit said page in your browser.

Any changes to files located at `tider/app/**/*.cljs` will be automatically picked up by the shadow-cljs watcher, compiled and hot reloaded.

Changes to server files will need to be re-eval'd as usual (i.e. using your IDE/editor). If you want a terminal REPL for the backend, run:

``` bash
clj -A:dev:repl
```
