(ns com.adham-omran.saturn.pages
  (:require
   [hiccup.page :as page]))

(defn index-page
  []
  (page/html5 {:ng-app "myApp" :lang "en"}
              [:head
               [:meta {:charset "UTF-8"}]
               [:title "Saturn Page"]
               [:link {:rel "icon"
                       :href "data:;base64,iVBORw0KGgo="}]
               [:body
                (page/include-js "./public/htmx.min.js")
                #_(page/include-js "https://cdn.jsdelivr.net/npm/vega@5")
                #_(page/include-js "https://cdn.jsdelivr.net/npm/vega-lite@5")
                #_(page/include-js "https://cdn.jsdelivr.net/npm/vega-embed@6")
                [:div
                 {:style {:display "flex"
                          :justify-content "center"
                          :justify-items "center"}}
                 [:div
                  [:form
                   {:style {:display "flex"
                            :flex-direction "column"
                            :justify-content "center"}}
                   [:textarea
                    {:name "q"}]
                   [:button
                    {:hx-get "/api/next-cell"
                     :hx-target "#next"
                     :hx-swap "outerHTML"}
                    "Add Cell"]
                   [:button
                    {:hx-post "/api/submit-code"
                     :hx-swap "innerHTML"
                     :hx-target "next"}
                    "Submit Code"]
                   [:div {:style
                          {:text-align "center"}}]]]]
                [:div#next]]]))
