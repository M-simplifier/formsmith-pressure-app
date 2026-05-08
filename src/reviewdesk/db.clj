(ns reviewdesk.db
  (:require [clojure.string :as str]))

(def initial-cards
  [{:id "packet-100"
    :title "Normalize review packet API"
    :owner "Mina"
    :status :reviewing
    :summary "Replace hand-written shape checks with Malli schemas before the next generated patch."
    :tags ["api" "malli" "agent"]
    :findings [{:id "finding-1"
                :rule-id "if/not-condition"
                :severity :medium
                :message "Negated condition can be canonicalized."
                :path "src/reviewdesk/http.clj"}]}
   {:id "packet-101"
    :title "Tighten dashboard state"
    :owner "Ren"
    :status :blocked
    :summary "The CLJS board needs predictable status transitions before more agent edits land."
    :tags ["cljs" "re-frame" "ui"]
    :findings [{:id "finding-2"
                :rule-id "let/nested-let"
                :severity :low
                :message "Nested let is still present in generated state shaping."
                :path "src/reviewdesk/client.cljs"}]}
   {:id "packet-102"
    :title "Prepare release notes"
    :owner "Aya"
    :status :ready
    :summary "Summarize what changed after a clean formsmith pass."
    :tags ["docs" "release"]
    :findings []}])

(defn store []
  (atom {:cards initial-cards
         :next-card 103
         :next-finding 3}))

(defn dashboard [state]
  (let [cards (:cards state)
        counts (frequencies (map :status cards))]
    {:cards cards
     :summary {:total (count cards)
               :blocked (get counts :blocked 0)
               :reviewing (get counts :reviewing 0)
               :ready (get counts :ready 0)}}))

(defn create-card [state attrs]
  (let [id (str "packet-" (:next-card state))
        title (str/trim (:title attrs))
        owner (str/trim (:owner attrs))
        card {:id id
              :title title
              :owner owner
              :status :reviewing
              :summary (str/trim (:summary attrs))
              :tags (vec (:tags attrs))
              :findings []}]
    (-> state
        (update :cards conj card)
        (update :next-card inc))))

(defn update-status [state id status]
  (update state :cards
          (fn [cards]
            (mapv #(if (= id (:id %))
                     (assoc % :status status)
                     %)
                  cards))))

(defn add-finding [state id finding]
  (let [finding (assoc finding :id (str "finding-" (:next-finding state)))]
    (-> state
        (update :cards
                (fn [cards]
                  (mapv #(if (= id (:id %))
                           (update % :findings conj finding)
                           %)
                        cards)))
        (update :next-finding inc))))
