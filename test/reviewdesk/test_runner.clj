(ns reviewdesk.test-runner
  (:require [clojure.test :as t]
            [reviewdesk.db-test]
            [reviewdesk.http-test]))

(defn -main []
  (let [{:keys [fail error]} (t/run-tests 'reviewdesk.db-test
                                          'reviewdesk.http-test)]
    (when (pos? (+ fail error))
      (System/exit 1))))
