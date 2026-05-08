(ns reviewdesk.system
  (:require [integrant.core :as ig]
            [reviewdesk.db :as db]
            [reviewdesk.http]
            [ring.adapter.jetty :as jetty]))

(def config
  {:reviewdesk/store {}
   :reviewdesk/http {:store (ig/ref :reviewdesk/store)}
   :reviewdesk/server {:handler (ig/ref :reviewdesk/http)
                       :port 8080}})

(defmethod ig/init-key :reviewdesk/store [_ _]
  (db/store))

(defmethod ig/init-key :reviewdesk/server [_ {:keys [handler port]}]
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/halt-key! :reviewdesk/server [_ server]
  (.stop server))

(defn start []
  (ig/init config))

(defn stop [system]
  (ig/halt! system))
