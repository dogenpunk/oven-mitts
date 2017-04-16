(ns oven-mitts.nlp
  (:require [opennlp.nlp :as nlp]))

(def tokenize (nlp/make-tokenizer "resources/models/en-token.bin"))
(def pos-tag (nlp/make-pos-tagger "resources/models/en-pos-maxent.bin"))

(defn tag-recipe [s]
  (pos-tag (tokenize s)))

(defn extract-nouns [coll]
  (filter (comp #{"NN" "NNS" "NNP" "NNPS"} last) coll))

(defn generate-ingredient-list [s]
  (->> s
      tag-recipe
      extract-nouns
      (map first)))
