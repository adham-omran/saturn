(ns com.adham-omran.saturn.backend
  (:require
   [cheshire.core :as json]
   [clojure.pprint :as pprint]
   [com.adham-omran.saturn.pages :as pages]
   [hiccup2.core :as h]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.malli]
   [reitit.ring.middleware.parameters :as parameters]
   [ring.adapter.jetty :as adapter]))

(comment
  ;; (ns com.adham-omran.saturn.env)
  (create-ns 'com.adham-omran.saturn.env))

(def app
  (ring/ring-handler
   (ring/router
    [["/" {:get {:handler (fn [_] {:body (pages/index-page)})}}]
     ["/api"
      ["/submit-code" {:post
                       {:handler
                        (fn [{{:strs [q]} :form-params}]
                          (let [pre-eval (-> q
                                             read-string)]
                            (pprint/pprint pre-eval)
                            {:body (str (h/html [:div [:p
                                                       (try
                                                         (binding [*ns* (create-ns 'com.adham-omran.saturn.env)]
                                                           (-> q
                                                               read-string
                                                               eval
                                                               h/raw))
                                                         (catch RuntimeException _
                                                           [:p
                                                            {:color "red"}
                                                            "Error."]))]]))}))}}]
      ["/next-cell" {:get {:handler
                           (fn [_]
                             {:body (str (h/html
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
                                          [:div#next]))})}}]]

     ["/public/*" (ring/create-resource-handler)]]

    {:data {#_:exception #_pretty/exception
            :coercion reitit.coercion.malli/coercion
            :muuntaja m/instance
            #_:conflicts #_(constantly nil)
            :middleware [parameters/parameters-middleware
                         coercion/coerce-request-middleware]}})
   (ring/routes (ring/create-default-handler))))

(comment
  (def server
    (adapter/run-jetty #'app {:port 8090
                              :join? false}))
  (.stop server))
