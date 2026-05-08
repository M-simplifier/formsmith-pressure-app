(ns reviewdesk.client
  (:require [ajax.core :as ajax]
            [re-frame.core :as rf]
            [reagent.dom.client :as rdom]))

(def status-label
  {:blocked "Blocked"
   :reviewing "Reviewing"
   :ready "Ready"})

(rf/reg-event-db
 :boot
 (fn [_ _]
   {:cards []
    :summary {:total 0 :blocked 0 :reviewing 0 :ready 0}
    :active-status :reviewing
    :draft {:title "" :owner "" :summary "" :tags ""}}))

(rf/reg-event-fx
 :load-dashboard
 (fn [_ _]
   {:http-xhrio {:method :get
                 :uri "/api/dashboard"
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:dashboard-loaded]
                 :on-failure [:request-failed]}}))

(rf/reg-event-db
 :dashboard-loaded
 (fn [db [_ payload]]
   (merge db payload)))

(rf/reg-event-db
 :request-failed
 (fn [db [_ response]]
   (assoc db :error (or (:status-text response) "Request failed"))))

(rf/reg-event-db
 :select-status
 (fn [db [_ status]]
   (assoc db :active-status status)))

(rf/reg-event-db
 :edit-draft
 (fn [db [_ field value]]
   (assoc-in db [:draft field] value)))

(rf/reg-event-fx
 :create-card
 (fn [{:keys [db]} _]
   (let [draft (:draft db)]
     {:http-xhrio {:method :post
                   :uri "/api/cards"
                   :params (update draft :tags #(js->clj (.split % ",")))
                   :format (ajax/json-request-format)
                   :response-format (ajax/json-response-format {:keywords? true})
                   :on-success [:card-created]
                   :on-failure [:request-failed]}})))

(rf/reg-event-db
 :card-created
 (fn [db [_ payload]]
   (-> db
       (merge payload)
       (assoc :draft {:title "" :owner "" :summary "" :tags ""}))))

(rf/reg-event-fx
 :set-status
 (fn [_ [_ id status]]
   {:http-xhrio {:method :patch
                 :uri (str "/api/cards/" id "/status")
                 :params {:status status}
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:dashboard-loaded]
                 :on-failure [:request-failed]}}))

(rf/reg-sub :cards (fn [db _] (:cards db)))
(rf/reg-sub :summary (fn [db _] (:summary db)))
(rf/reg-sub :active-status (fn [db _] (:active-status db)))
(rf/reg-sub :draft (fn [db _] (:draft db)))

(defn status-nav []
  (let [active @(rf/subscribe [:active-status])
        summary @(rf/subscribe [:summary])]
    [:ul.nav-list
     (for [status [:blocked :reviewing :ready]]
       ^{:key status}
       [:li
        [:button {:class (when (= status active) "active")
                  :on-click #(rf/dispatch [:select-status status])}
         (str (status-label status) " "
              (get summary status 0))]])]))

(defn signal-list [findings]
  [:ul.signals
   (if-let [findings (not-empty findings)] (for [{:keys [id rule-id severity message path]} findings] [:li [:strong rule-id] [:span.muted (str " · " (name severity) " · " path)] [:div message]]) [:li.muted "No current formsmith findings."])])

(defn review-card [{:keys [id title owner status summary tags findings]}]
  [:li.card
   [:header
    [:div
     [:h2 title]
     [:div.muted (str owner " · " (clojure.string/join ", " tags))]]
    [:select.status {:class (name status)
                     :value (name status)
                     :on-change #(rf/dispatch [:set-status id (keyword (.. % -target -value))])}
     (for [status [:blocked :reviewing :ready]]
       ^{:key status}
       [:option {:value (name status)} (status-label status)])]]
   [:p summary]
   [signal-list findings]])

(defn draft-form []
  (let [draft @(rf/subscribe [:draft])]
    [:form.form-grid
     {:on-submit (fn [event]
                   (.preventDefault event)
                   (rf/dispatch [:create-card]))}
     [:label "Title"
      [:input {:value (:title draft)
               :on-change #(rf/dispatch [:edit-draft :title (.. % -target -value)])}]]
     [:label "Owner"
      [:input {:value (:owner draft)
               :on-change #(rf/dispatch [:edit-draft :owner (.. % -target -value)])}]]
     [:label "Tags"
      [:input {:value (:tags draft)
               :placeholder "api, cljs, agent"
               :on-change #(rf/dispatch [:edit-draft :tags (.. % -target -value)])}]]
     [:label "Summary"
      [:textarea {:value (:summary draft)
                  :on-change #(rf/dispatch [:edit-draft :summary (.. % -target -value)])}]]
     [:button.primary {:type "submit"} "Add packet"]]))

(defn board []
  (let [cards @(rf/subscribe [:cards])
        active @(rf/subscribe [:active-status])
        visible (filter #(= active (:status %)) cards)]
    [:div.shell
     [:aside.sidebar
      [:div.brand
       [:div.mark "F"]
       [:div
        [:strong "ReviewDesk"]
        [:div.muted "Formsmith pressure app"]]]
      [status-nav]]
     [:main.main
      [:div.toolbar
       [:h1 "Review packets"]
       [:button.primary {:on-click #(rf/dispatch [:load-dashboard])} "Refresh"]]
      [:div.grid
       [:section.panel
        [:ul.cards
         (for [card visible]
           ^{:key (:id card)}
           [review-card card])]]
       [:section.panel
        [:h2 "New packet"]
        [draft-form]]]]]))

(defn init []
  (rf/dispatch-sync [:boot])
  (rf/dispatch [:load-dashboard])
  (rdom/render (rdom/create-root (.getElementById js/document "app"))
               [board]))
