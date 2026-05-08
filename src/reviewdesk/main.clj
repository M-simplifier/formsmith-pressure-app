(ns reviewdesk.main
  (:gen-class)
  (:require [reviewdesk.system :as system]))

(defn -main [& _]
  (let [running (system/start)]
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(system/stop running)))
    (println "ReviewDesk listening on http://localhost:8080")
    @(promise)))
