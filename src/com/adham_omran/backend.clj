(ns com.adham-omran.backend
  (:require
   [ring.adapter.jetty :as adapter]
   [cheshire.core :as json]
   [clojure.pprint :as pprint]
   [com.adham-omran.index :as index]
   com.adham-omran.saturn
   [hiccup2.core :as h]
   [muuntaja.core :as m]
   [reitit.coercion.malli]
   [reitit.dev.pretty :as pretty]
   [reitit.ring :as ring]
   [reitit.ring.coercion :as coercion]
   [reitit.ring.malli]
   [reitit.ring.middleware.parameters :as parameters]))

(comment
  (ns com.adham-omran.saturn.env)
  (create-ns 'com.adham-omran.saturn.env))

(def app
  (ring/ring-handler
   (ring/router
    [[""
      ["/" {:get {:handler (fn [_] {:body (index/index-page)})}}]
      ["/saturn" {:get {:handler (fn [_] {:body (com.adham-omran.saturn/index-page)})}}]]

     ["/api"
      #_["/slider-demo" {:post {:handler (fn [q]
                                           {:body
                                            (-> [:script
                                                 (h/raw (format "vegaEmbed(document.currentScript.parentElement,%s).catch(console.error); "
                                                                (json/generate-string
                                                                 {"data" {"values" [{"category" "A", "group" "x", "value" (-> (get (:form-params q) "v")
                                                                                                                              Integer/parseInt)}
                                                                                    {"category" "B", "group" "z", "value" 3}
                                                                                    {"category" "C", "group" "z", "value" 7}]}

                                                                  "mark" "bar",
                                                                  "encoding" {"x" {"field" "category"},
                                                                              "y" {"field" "value", "type" "quantitative"},
                                                                              "xOffset" {"field" "group"},
                                                                              "color" {"field" "group"}}})))]
                                                h/html
                                                str)})}}]
      ["/input" {:post
                 {:handler
                  (fn [{{:strs [q]} :form-params}]
                    (let [pre-eval (-> q
                                       read-string)]
                      (pprint/pprint pre-eval)
                      (if (:chart pre-eval)
                        {:body
                         (-> [:script
                              (h/raw (format "vegaEmbed(document.currentScript.parentElement,%s).catch(console.error); "
                                             (json/generate-string
                                              {"data" {"values" [{"category" "A", "group" "x", "value" (get-in pre-eval [:v :A])}
                                                                 {"category" "B", "group" "z", "value" 1.1}
                                                                 {"category" "C", "group" "z", "value" 0.2}]}

                                               "mark" "bar",
                                               "encoding" {"x" {"field" "category"},
                                                           "y" {"field" "value", "type" "quantitative"},
                                                           "xOffset" {"field" "group"},
                                                           "color" {"field" "group"}}})))]
                             h/html
                             str)}
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
                                                        "Error."]))]]))})))}}]
      ["/next-cell" {:get {:handler
                           (fn [_]
                             {:body (str (h/html
                                             [:div
                                              [:textarea
                                               {:name "q"
                                                :hx-post "/api/input"
                                                :hx-trigger "keyup changed"
                                                :hx-target "next"
                                                :hx-swap "innerHTML"}]
                                              [:div]]
                                             [:div#next]))})}}]
      ["/vega-basic" {:get {:handler
                            (fn [_]
                              {:body (str
                                      (h/html
                                          [:div
                                           [:script
                                            (h/raw
                                             (format "vegaEmbed(document.currentScript.parentElement, %s).catch(console.error);"
                                                     (json/generate-string
                                                      (binding [*ns* (create-ns 'mars.live)]
                                                        {:data {:values [{"a" "A", "b" (eval (symbol (str *ns*) "x"))}]},
                                                         :mar :bar,
                                                         :encoding
                                                         {:x {"field" "a", "type" "nominal", "axis" {"labelAngle" 0}},
                                                          :y {"field" "b", "type" "quantitative"}}}))))]]))})}}]]

     ["/public/*" (ring/create-resource-handler)]]

    {:data {:exception pretty/exception
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
