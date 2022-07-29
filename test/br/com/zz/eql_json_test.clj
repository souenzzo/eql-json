(ns br.com.zz.eql-json-test
  (:require [br.com.zz.eql-json :as eql-json]
            [clojure.data.json :as json]
            [clojure.test :refer [deftest is]]
            [edn-query-language.core :as eql]
            [malli.core :as m]))

(defn query->json->clj
  [query]
  (-> query
    eql/query->ast
    (->> (eql/transduce-children eql-json/node-without-query))
    (json/write-str :value-fn eql-json/json-value-fn)
    json/read-str))

(defn query->json->clj-ast
  [query]
  (-> query
    eql/query->ast
    (->> (eql/transduce-children eql-json/node-without-query))
    (json/write-str :value-fn eql-json/json-value-fn)
    json/read-str
    (as-> % (m/decode
              eql-json/ast-schema
              %
              eql-json/ast-transformer))))

(deftest hello
  (is (= {"type"     "root"
          "children" [{"type"         "prop"
                       "dispatch-key" "a/b"
                       "key"          "a/b"}]}
        (-> [:a/b]
          query->json->clj
          #_(doto clojure.pprint/pprint))))
  (is (= {:type     :root
          :children [{:type         :prop
                      :dispatch-key :a/b
                      :key          :a/b}]}
        (-> (m/decode eql-json/ast-schema
              {"type"     "root"
               "children" [{"type"         "prop"
                            "dispatch-key" "a/b"
                            "key"          "a/b"}]}
              eql-json/ast-transformer)
          #_(doto clojure.pprint/pprint))))
  (is (= {:type     :root
          :children [{:type         :prop
                      :dispatch-key :a
                      :params       {:b 42}
                      :key          :a}]}
        (-> `[(:a {:b 42})]
          query->json->clj-ast
          #_(doto clojure.pprint/pprint))))
  (is (= {:children [{:children     [{:dispatch-key :b
                                      :key          :b
                                      :type         :prop}]
                      :dispatch-key :a
                      :key          :a
                      :type         :join}]
          :type     :root}
        (-> `[{:a [:b]}]
          query->json->clj-ast
          #_(doto clojure.pprint/pprint))))
  (is (= {:children [{:children     [{:dispatch-key :b
                                      :key          :b
                                      :type         :prop}]
                      :dispatch-key :a
                      :key          [:a 42]
                      :type         :join}]
          :type     :root}
        (-> `[{[:a 42] [:b]}]
          query->json->clj-ast
          #_(doto clojure.pprint/pprint)))))
