(ns reviewdesk.schema
  (:require [malli.core :as m]))

(def status-values
  [:blocked :reviewing :ready])

(def severity-values
  [:low :medium :high])

(def Finding
  [:map
   [:id :string]
   [:rule-id :string]
   [:severity [:enum :low :medium :high]]
   [:message :string]
   [:path :string]])

(def ReviewCard
  [:map
   [:id :string]
   [:title :string]
   [:owner :string]
   [:status [:enum :blocked :reviewing :ready]]
   [:summary :string]
   [:tags [:vector :string]]
   [:findings [:vector Finding]]])

(def CreateCard
  [:map
   [:title :string]
   [:owner :string]
   [:summary :string]
   [:tags {:optional true} [:vector :string]]])

(def StatusPatch
  [:map
   [:status [:enum :blocked :reviewing :ready]]])

(defn valid? [schema value]
  (m/validate schema value))
