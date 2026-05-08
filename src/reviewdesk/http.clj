(ns reviewdesk.http
  (:require [integrant.core :as ig]
            [malli.core :as m]
            [reitit.ring :as ring]
            [reviewdesk.db :as db]
            [reviewdesk.schema :as schema]
            [ring.middleware.json :refer [wrap-json-body wrap-json-response]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.util.response :as response]))

(defn- json [status body]
  (-> (response/response body)
      (response/status status)))

(defn- invalid [message]
  (json 422 {:error message}))

(defn- parse-tags [value]
  (cond
    (vector? value) value
    (string? value) (->> (clojure.string/split value #",")
                         (map clojure.string/trim)
                         (remove empty?)
                         vec)
    :else []))

(defn- create-card! [store request]
  (let [body (update (:body request) :tags parse-tags)]
    (if (m/validate schema/CreateCard body)
      (do
        (swap! store db/create-card body)
        (json 201 (db/dashboard @store)))
      (invalid "Card payload does not match the review card schema."))))

(defn- patch-status! [store request]
  (let [id (get-in request [:path-params :id])
        body (:body request)]
    (if (m/validate schema/StatusPatch body)
      (do
        (swap! store db/update-status id (:status body))
        (json 200 (db/dashboard @store)))
      (invalid "Status patch must contain a known status."))))

(defn routes [store]
  [["/api/dashboard"
    {:get (fn [_] (json 200 (db/dashboard @store)))}]
   ["/api/cards"
    {:post (fn [request] (create-card! store request))}]
   ["/api/cards/:id/status"
    {:patch (fn [request] (patch-status! store request))}]])

(defn handler [store]
  (-> (ring/ring-handler
       (ring/router (routes store))
       (ring/routes
        (ring/create-resource-handler {:path "/"})
        (ring/create-default-handler)))
      (wrap-json-body {:keywords? true})
      wrap-json-response
      (wrap-resource "public")))

(defmethod ig/init-key :reviewdesk/http [_ {:keys [store]}]
  (handler store))
