(ns reviewdesk.db-test
  (:require [clojure.test :refer [deftest is testing]]
            [reviewdesk.db :as db]))

(deftest dashboard-counts-statuses
  (testing "dashboard summary follows current cards"
    (let [state {:cards [{:status :blocked}
                         {:status :reviewing}
                         {:status :ready}
                         {:status :ready}]}]
      (is (= {:total 4 :blocked 1 :reviewing 1 :ready 2}
             (:summary (db/dashboard state)))))))

(deftest cards-can-be-created-and-updated
  (testing "review packets move through statuses"
    (let [state (-> {:cards []
                     :next-card 1
                     :next-finding 1}
                    (db/create-card {:title "  New packet  "
                                     :owner "  Kai "
                                     :summary "  Needs pass "
                                     :tags ["api"]})
                    (db/update-status "packet-1" :ready))]
      (is (= :ready (-> state :cards first :status)))
      (is (= "New packet" (-> state :cards first :title)))
      (is (= "Kai" (-> state :cards first :owner))))))
