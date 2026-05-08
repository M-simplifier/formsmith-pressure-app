(ns reviewdesk.http-test
  (:require [clojure.test :refer [deftest is testing]]
            [reviewdesk.db :as db]
            [reviewdesk.http :as http]
            [ring.mock.request :as mock]))

(defn- app []
  (http/handler (db/store)))

(deftest dashboard-endpoint-returns-cards
  (testing "API exposes seeded review packets"
    (let [response ((app) (mock/request :get "/api/dashboard"))]
      (is (= 200 (:status response)))
      (is (re-find #"packet-100" (:body response))))))

(deftest card-creation-validates-payload
  (testing "invalid cards get a 422 response"
    (let [response ((app) (-> (mock/request :post "/api/cards")
                              (mock/json-body {:owner "Kai"})))]
      (is (= 422 (:status response)))))
  (testing "valid cards update the dashboard"
    (let [response ((app) (-> (mock/request :post "/api/cards")
                              (mock/json-body {:title "Route generated patch"
                                               :owner "Kai"
                                               :summary "Review generated code"
                                               :tags ["routing" "agent"]})))]
      (is (= 201 (:status response)))
      (is (re-find #"Route generated patch" (:body response))))))
