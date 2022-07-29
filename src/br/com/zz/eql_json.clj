(ns br.com.zz.eql-json
  (:require [malli.transform :as mt]))


(defn json-value-fn
  [k v]
  (cond
    (ident? v) (str
                 (some-> v namespace (str "/"))
                 (name v))
    :else v))


(def node-without-query
  (map (fn [x] (dissoc x :query))))

(def ast-schema
  [:schema {:registry {::node [:map
                               [:type [:enum
                                       {:decode/json keyword}
                                       :root :prop :join]]
                               [:key [:or
                                      [:keyword
                                       {:decode/json keyword}]
                                      [:tuple
                                       [:keyword {:decode/json keyword}]
                                       :any]]]
                               [:params :map]
                               [:dispatch-key [:keyword
                                               {:decode/json keyword}]]
                               [:children [:sequential [:ref ::node]]]]}}
   ::node])

(def ast-transformer
  (mt/transformer
    (mt/key-transformer {:decode keyword})
    (mt/json-transformer)
    #_{:name :eql-json}))
