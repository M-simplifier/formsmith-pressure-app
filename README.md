# formsmith-pressure-app

`formsmith-pressure-app` is a public, minimal-surface pressure project for
evaluating `formsmith` on a realistic full-stack Clojure/ClojureScript app.

The app is a small review operations board for AI-generated Clojure work. It
uses Ring, Reitit, Integrant, Malli, Reagent, and re-frame so `formsmith` can be
exercised on backend handlers, schemas, system wiring, and CLJS UI/state code.

## Run Locally

```bash
npm install
clojure -M:test
npx shadow-cljs release app
clojure -M:run
```

Open:

```text
http://localhost:8080
```

## Formsmith Gate

This repo consumes the public `formsmith` artifact through a git dependency:

```clojure
{:git/tag "v0.1.0-pre.5"
 :git/sha "4bd1d7228aebf24a0cc7b80c83c84396ea7d1fbc"}
```

```bash
clojure -M:formsmith check src test
clojure -M:formsmith fix --check --aggressive src test
```

The CI gate runs backend tests, the CLJS release build, and `formsmith check`.
